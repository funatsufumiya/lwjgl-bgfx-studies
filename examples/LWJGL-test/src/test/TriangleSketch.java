package test;

import java.io.IOException;

import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_COLOR;
import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_DEPTH;
import static org.lwjgl.bgfx.BGFX.BGFX_DEBUG_TEXT;
import static org.lwjgl.bgfx.BGFX.BGFX_STATE_DEFAULT;
import static org.lwjgl.bgfx.BGFX.bgfx_dbg_text_clear;
import static org.lwjgl.bgfx.BGFX.bgfx_dbg_text_printf;
import static org.lwjgl.bgfx.BGFX.bgfx_destroy_index_buffer;
import static org.lwjgl.bgfx.BGFX.bgfx_destroy_program;
import static org.lwjgl.bgfx.BGFX.bgfx_destroy_vertex_buffer;
import static org.lwjgl.bgfx.BGFX.bgfx_encoder_begin;
import static org.lwjgl.bgfx.BGFX.bgfx_encoder_end;
import static org.lwjgl.bgfx.BGFX.bgfx_encoder_set_index_buffer;
import static org.lwjgl.bgfx.BGFX.bgfx_encoder_set_state;
import static org.lwjgl.bgfx.BGFX.bgfx_encoder_set_vertex_buffer;
import static org.lwjgl.bgfx.BGFX.bgfx_encoder_submit;
import static org.lwjgl.bgfx.BGFX.bgfx_set_debug;
import static org.lwjgl.bgfx.BGFX.bgfx_set_view_clear;
import org.lwjgl.bgfx.BGFXVertexLayout;

import sketch.App;
import sketch.Sketch;
import static sketch.core.PGraphics.background;
import sketch.util.BGFXUtil;
import static sketch.util.BGFXUtil.VertexLayoutType.XYC;
import static sketch.util.BGFXUtil.byteSizeOf;

public class TriangleSketch extends Sketch {
    private BGFXVertexLayout layout;

    private static final Object[][] kTriangleVertices = {
        {-0.5f, -0.5f, 0x339933FF},
        {0.5f, -0.5f, 0x993333FF},
        {0.0f, 0.5f, 0x333399FF}
    };

    int kTriangleIndices[] = {0, 1, 2};

    short vertex_buffer = -1;
    short index_buffer = -1;
    short program = -1;

    @Override
    public void setup() {
        // Enable debug text.
        bgfx_set_debug(BGFX_DEBUG_TEXT);
        bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x303030ff, 1.0f, 0);

        layout = BGFXUtil.createVertexLayout2D(false, true, 0);

        vertex_buffer = BGFXUtil.createVertexBuffer(byteSizeOf(XYC, 3), layout, kTriangleVertices);
        index_buffer = BGFXUtil.createIndexBuffer(kTriangleIndices);
        try {
            program = BGFXUtil.createBasicShaderProgram();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to create shader program");
        }
    }

    @Override
    public void draw() {
        // background(0);
        background(125);
        // background(255);

        long encoder = bgfx_encoder_begin(false);

        // draw triangle

        bgfx_encoder_set_vertex_buffer(encoder, 0, vertex_buffer, 0, 3);
        bgfx_encoder_set_index_buffer(encoder, index_buffer, 0, 3);

        bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT, 0);
        // bgfx_encoder_set_state(encoder,
        //     BGFX_STATE_WRITE_R
        //         | BGFX_STATE_WRITE_G
        //         | BGFX_STATE_WRITE_B
        //         | BGFX_STATE_WRITE_A,
        //     0
        // );

        bgfx_encoder_submit(encoder, 0, program, 0, 0);

        bgfx_encoder_end(encoder);

        // Use debug font to print information about this example.
        bgfx_dbg_text_clear(0, false);
        bgfx_dbg_text_printf(0, 0, 0x0f, "Triangle");
    }

    @Override
    public void exit() {
        bgfx_destroy_program(program);
        bgfx_destroy_index_buffer(index_buffer);
        bgfx_destroy_vertex_buffer(vertex_buffer);
        layout.free();
    }

    public static void main(String[] args) {
        App.main("test.TriangleSketch", 640, 480);
    }
}