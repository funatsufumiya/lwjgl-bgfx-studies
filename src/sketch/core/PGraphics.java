package sketch.core;

import org.lwjgl.bgfx.BGFX;
import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_COLOR;

public class PGraphics {
    public static void background_hex(int rgba) {
        BGFX.bgfx_set_view_clear(0, BGFX_CLEAR_COLOR, rgba, 0.0f, 0);
    }

    public static void background(float r, float g, float b, float a) {
        int rgba = ((int)(r) << 24) | ((int)(g) << 16) | ((int)(b) << 8) | ((int)(a));
        background_hex(rgba);
    }

    public static void background(float r, float g, float b) {
        background(r, g, b, 255);
    }

    public static void background(float gray, float alpha) {
        background(gray, gray, gray, alpha);
    }

    public static void background(float gray) {
        background(gray, gray, gray, 255);
    }

    /// @param h hue in [0, 360)
    /// @param s saturation in [0, 255]
    /// @param b brightness in [0, 255]
    /// @return an integer representing the color in RGBA format
    public static int hsb2rgb(float h, float s, float b) {
        int c = java.awt.Color.HSBtoRGB(h / 360.0f, s / 255.0f, b / 255.0f);
        return (c & 0xffffff) | 0xff000000;
    }

    public static void rect(float x, float y, float w, float h) {
        // TODO
    }

    public static int getRendererType() {
        return BGFX.bgfx_get_renderer_type();
    }
}