package test;

import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_COLOR;
import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_DEPTH;
import static org.lwjgl.bgfx.BGFX.BGFX_DEBUG_TEXT;
import static org.lwjgl.bgfx.BGFX.bgfx_dbg_text_clear;
import static org.lwjgl.bgfx.BGFX.bgfx_dbg_text_printf;
import static org.lwjgl.bgfx.BGFX.bgfx_set_debug;
import static org.lwjgl.bgfx.BGFX.bgfx_set_view_clear;

import sketch.App;
import sketch.Sketch;

public class HelloSketch extends Sketch {
    @Override
    public void setup() {
        // Enable debug text.
        bgfx_set_debug(BGFX_DEBUG_TEXT);

        bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x303030ff, 1.0f, 0);
    }

    @Override
    public void draw() {
        // Use debug font to print information about this example.
        bgfx_dbg_text_clear(0, false);
        bgfx_dbg_text_printf(0, 0, 0x0f, "Simplest");
    }

    public static void main(String[] args) {
        App.main("test.HelloSketch", 640, 480);
    }
}