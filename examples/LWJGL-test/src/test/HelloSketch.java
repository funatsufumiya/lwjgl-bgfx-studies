package test;

import static org.lwjgl.bgfx.BGFX.*;

import sketch.App;
import sketch.Sketch;

public class HelloSketch extends Sketch {
    public void setup() {
        // Enable debug text.
        bgfx_set_debug(BGFX_DEBUG_TEXT);

        bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x303030ff, 1.0f, 0);
    }

    public void draw() {
        // This dummy draw call is here to make sure that view 0 is cleared
        // if no other draw calls are submitted to view 0.
        bgfx_touch(0);

        // Use debug font to print information about this example.
        bgfx_dbg_text_clear(0, false);
        bgfx_dbg_text_printf(0, 0, 0x0f, "Simplest");

        // Advance to next frame. Rendering thread will be kicked to
        // process submitted rendering primitives.
        bgfx_frame(false);
    }

    public static void main(String[] args) {
        App.main("test.HelloSketch", 1280, 720);
    }
}