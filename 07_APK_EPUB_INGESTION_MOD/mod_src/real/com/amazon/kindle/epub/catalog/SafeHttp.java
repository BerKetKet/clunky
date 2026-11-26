package com.amazon.kindle.epub.catalog;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * HTTP/HTTPS client with automatic DNS-over-HTTPS (1.1.1.1 / 8.8.8.8) fallback.
 * If Android's system DNS fails (UnknownHostException), resolves the IPv4 address via
 * Cloudflare/Google DoH directly by IP and connects with proper TLS SNI + Host header.
 */
public final class SafeHttp {
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 Folio/3.3";
    private static final Map<String, String> DNS_CACHE = new ConcurrentHashMap<String, String>();

    public static String getString(String urlStr) throws Exception {
        long ttl = DiskCache.ttlForUrl(urlStr);
        String cached = DiskCache.getText(urlStr, ttl, false);
        if (cached != null) {
            return cached;
        }
        try {
            InputStream is = openStream(urlStr);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                String res = sb.toString();
                if (!res.isEmpty()) {
                    DiskCache.putText(urlStr, res);
                }
                return res;
            } finally {
                try { is.close(); } catch (Throwable ignored) {}
            }
        } catch (Exception netErr) {
            // Stale-on-error fallback (keeps Folio working smoothly during Gutenberg/OpenLibrary maintenance!)
            String stale = DiskCache.getText(urlStr, ttl, true);
            if (stale != null) {
                android.util.Log.w("SafeHttp", "Serving stale cache for " + urlStr + " after error: " + netErr.getMessage());
                return stale;
            }
            throw netErr;
        }
    }

    public static final int MAX_GET_BYTES = 2 * 1024 * 1024;
    public static final long OPDS_COVER_MAX_BYTES = 8L * 1024L * 1024L;

    public static byte[] getBytes(String urlStr) throws Exception {
        return getBytes(urlStr, MAX_GET_BYTES);
    }

    public static byte[] getBytes(String urlStr, long maxBytes) throws Exception {
        long cap = maxBytes > 0L ? maxBytes : MAX_GET_BYTES;
        InputStream is = openStream(urlStr);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            long total = 0;
            int n;
            while ((n = is.read(buf)) > 0) {
                total += n;
                if (total > cap) {
                    throw new IOException("Response exceeds " + cap + " bytes limit");
                }
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        } finally {
            try { is.close(); } catch (Throwable ignored) {}
        }
    }

    public static InputStream openStream(String urlStr) throws Exception {
        return openStreamInternal(urlStr, 0);
    }

    private static InputStream openConnectedStream(final HttpURLConnection conn) throws IOException {
        try {
            final InputStream raw = conn.getInputStream();
            return new java.io.FilterInputStream(raw) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        try { conn.disconnect(); } catch (Throwable ignored) {}
                    }
                }
            };
        } catch (IOException ioe) {
            try { conn.disconnect(); } catch (Throwable ignored) {}
            throw ioe;
        } catch (RuntimeException re) {
            try { conn.disconnect(); } catch (Throwable ignored) {}
            throw re;
        }
    }

    private static InputStream openStreamInternal(String urlStr, int depth) throws Exception {
        if (depth > 6) {
            throw new IOException("Too many redirects");
        }
        URL url = new URL(urlStr);
        String host = url.getHost();
        HttpURLConnection conn;
        try {
            conn = openDirect(url);
            int code = conn.getResponseCode();
            if (isRedirect(code)) {
                String loc = conn.getHeaderField("Location");
                conn.disconnect();
                if (loc != null && !loc.isEmpty()) {
                    URL next = new URL(url, loc);
                    return openStreamInternal(next.toString(), depth + 1);
                }
            }
            return openConnectedStream(conn);
        } catch (UnknownHostException uhe) {
            String ip = resolveViaDoH(host);
            if (ip == null || ip.isEmpty()) {
                throw uhe;
            }
            conn = openWithIpAndSni(url, host, ip);
            int code = conn.getResponseCode();
            if (isRedirect(code)) {
                String loc = conn.getHeaderField("Location");
                conn.disconnect();
                if (loc != null && !loc.isEmpty()) {
                    URL next = new URL(url, loc);
                    return openStreamInternal(next.toString(), depth + 1);
                }
            }
            return openConnectedStream(conn);
        }
    }

    private static boolean isRedirect(int code) {
        return code == 301 || code == 302 || code == 303 || code == 307 || code == 308;
    }

    private static HttpURLConnection openDirect(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(45000);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        return conn;
    }

    private static HttpURLConnection openWithIpAndSni(URL origUrl, final String origHost, String ip) throws IOException {
        String protocol = origUrl.getProtocol();
        int port = origUrl.getPort();
        String file = origUrl.getFile();
        URL ipUrl = (port > 0) ? new URL(protocol, ip, port, file) : new URL(protocol, ip, file);
        HttpURLConnection conn = (HttpURLConnection) ipUrl.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(45000);
        conn.setRequestProperty("Host", origHost);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection https = (HttpsURLConnection) conn;
            https.setSSLSocketFactory(new SniSSLSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), origHost));
            https.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return HttpsURLConnection.getDefaultHostnameVerifier().verify(origHost, session);
                }
            });
        }
        return conn;
    }

    public static String resolveViaDoH(String host) {
        if (host == null || host.isEmpty()) return null;
        String cached = DNS_CACHE.get(host);
        if (cached != null) return cached;
        String[] endpoints = new String[]{
            "https://1.1.1.1/dns-query?name=" + urlEnc(host) + "&type=A",
            "https://8.8.8.8/resolve?name=" + urlEnc(host) + "&type=A"
        };
        for (String ep : endpoints) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(ep).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Accept", "application/dns-json");
                conn.setRequestProperty("User-Agent", USER_AGENT);
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                JSONObject root = new JSONObject(sb.toString());
                JSONArray answers = root.optJSONArray("Answer");
                if (answers != null) {
                    for (int i = 0; i < answers.length(); i++) {
                        JSONObject ans = answers.optJSONObject(i);
                        if (ans != null && ans.optInt("type") == 1) {
                            String ip = ans.optString("data", "").trim();
                            if (!ip.isEmpty()) {
                                DNS_CACHE.put(host, ip);
                                return ip;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        return null;
    }

    private static String urlEnc(String s) {
        try { return URLEncoder.encode(s, "UTF-8"); } catch (Throwable t) { return s; }
    }

    private static final class SniSSLSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory delegate;
        private final String sniHost;

        SniSSLSocketFactory(SSLSocketFactory delegate, String sniHost) {
            this.delegate = delegate;
            this.sniHost = sniHost;
        }

        private Socket configureSni(Socket s) {
            if (s instanceof SSLSocket) {
                SSLSocket ssl = (SSLSocket) s;
                SSLParameters params = ssl.getSSLParameters();
                params.setServerNames(Collections.<SNIServerName>singletonList(new SNIHostName(sniHost)));
                ssl.setSSLParameters(params);
            }
            return s;
        }

        @Override public String[] getDefaultCipherSuites() { return delegate.getDefaultCipherSuites(); }
        @Override public String[] getSupportedCipherSuites() { return delegate.getSupportedCipherSuites(); }
        @Override public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return configureSni(delegate.createSocket(s, sniHost, port, autoClose));
        }
        @Override public Socket createSocket(String host, int port) throws IOException {
            return configureSni(delegate.createSocket(host, port));
        }
        @Override public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return configureSni(delegate.createSocket(host, port, localHost, localPort));
        }
        @Override public Socket createSocket(InetAddress host, int port) throws IOException {
            return configureSni(delegate.createSocket(host, port));
        }
        @Override public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return configureSni(delegate.createSocket(address, port, localAddress, localPort));
        }
    }
}
