package android.graphics;
import java.io.OutputStream;
public class Bitmap {
    public enum CompressFormat { JPEG, PNG, WEBP }
    public enum Config { ARGB_8888, RGB_565 }
    public int getWidth() { return 0; }
    public int getHeight() { return 0; }
    public boolean compress(CompressFormat format, int quality, OutputStream stream) { return false; }
    public static Bitmap createBitmap(int width, int height, Config config) { return null; }
    public void recycle() {}
}
