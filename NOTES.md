# Kindle Android v8.159.0.100 - Deployment & Restoration Notes

## Status
- **Current State**: Restored to stock. The original unmodified `com.amazon.kindle` APK is restored and running on-device.
- **Target Device**: Samsung Galaxy M32 (Android 13 / API 33, arm64-v8a, Magisk root, Enforcing SELinux).
- **Installed APK Path**: `/data/app/~~9iOBGmZzWJG8kWDshVWsEQ==/com.amazon.kindle-_UswMejwLkmSwfKZB0sDPA==/base.apk`

## Verification of Restored State
1. **Binary Checksum**:
   - Device `base.apk` SHA-256: `d0c1f890e325ec3696342248a37219e68f3e41c27f39bfdf21de05c6bacba1fd`
   - Size: `135,279,828` bytes (100% bit-for-bit match with official release APK in `handoff_assets/com.amazon.kindle_v8.159.0.100_base_original.apk`).
2. **Permissions & Security Attributes**:
   - Owner: `system:system`
   - Mode: `0644` (`-rw-r--r--`)
   - SELinux Context: `u:object_r:apk_data_file:s0`
3. **Cache Cleanup**:
   - Directory `/data/data/com.amazon.kindle/files/epub_cache` removed.
   - Temporary staging files in `/data/local/tmp` cleaned up.
4. **Compilation & Execution**:
   - ART speed-profile recompiled cleanly (`cmd package compile -m speed-profile -f com.amazon.kindle` -> `Success`).
   - Launched stock entrypoint: `com.amazon.kindle/com.amazon.kcp.oob.MainActivity` running normally (`dumpsys window` confirmed `mFocusedApp=... com.amazon.kindle/com.amazon.kcp.oob.MainActivity`).
