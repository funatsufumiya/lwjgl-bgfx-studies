package test;

import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_COLOR;
import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_DEPTH;
import static org.lwjgl.bgfx.BGFX.BGFX_DEBUG_TEXT;
import static org.lwjgl.bgfx.BGFX.bgfx_dbg_text_clear;
import static org.lwjgl.bgfx.BGFX.bgfx_dbg_text_printf;
import static org.lwjgl.bgfx.BGFX.bgfx_frame;
import static org.lwjgl.bgfx.BGFX.bgfx_set_debug;
import static org.lwjgl.bgfx.BGFX.bgfx_set_view_clear;
import static org.lwjgl.bgfx.BGFX.bgfx_touch;

import sketch.App;
import sketch.Sketch;
import static sketch.core.PGraphics.background;
import static sketch.core.PGraphics.hsb2rgb;

public class BGSketch extends Sketch {
    @Override
    public void setup() {
        // Enable debug text.
        bgfx_set_debug(BGFX_DEBUG_TEXT);

        bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x303030ff, 1.0f, 0);
    }

    @Override
    public void draw() {
        // This dummy draw call is here to make sure that view 0 is cleared
        // if no other draw calls are submitted to view 0.
        bgfx_touch(0);

        float t = elapsedTimeSeconds();
        float h = (0.3f + 0.5f * (float) Math.sin(t)) * 360f;
        int color = hsb2rgb(h, 255, 255);

        // set BG color
        // bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0xff0000ff, 1.0f, 0);
        // background(255, 0, 0);
        background(color);

        // Use debug font to print information about this example.
        bgfx_dbg_text_clear(0, false);
        bgfx_dbg_text_printf(0, 0, 0x0f, "BG");

        // Advance to next frame. Rendering thread will be kicked to
        // process submitted rendering primitives.
        bgfx_frame(false);
    }

    public static void main(String[] args) {
        App.main("test.BGSketch", 640, 480);
    }
}