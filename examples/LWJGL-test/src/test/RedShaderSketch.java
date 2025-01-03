package test;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.bgfx.BGFX;
import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_COLOR;
import static org.lwjgl.bgfx.BGFX.BGFX_CLEAR_DEPTH;
import static org.lwjgl.bgfx.BGFX.BGFX_DEBUG_TEXT;
import static org.lwjgl.bgfx.BGFX.BGFX_DEBUG_WIREFRAME;
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
import org.lwjgl.system.MemoryUtil;

import sketch.App;
import sketch.Sketch;
import static sketch.core.PGraphics.background;
import sketch.util.BGFXUtil;
import static sketch.util.BGFXUtil.VertexLayoutType.XYC;
import static sketch.util.BGFXUtil.byteSizeOf;

public class RedShaderSketch extends Sketch {
    static int width = 640;
    static int height = 480;

    private BGFXVertexLayout layout;

    private static final Object[][] kRectVertices = {
        {-1.0f, -1.0f, 0x339933FF},
        {0.0f, -1.0f, 0x993333FF},
        {0.0f, 1.0f, 0x333399FF},
        {-1.0f, 1.0f, 0x333399FF}
    };
    // private static final Object[][] kRectVertices = {
    //     {-100.0f, -100.0f, 0x339933FF},
    //     {0.0f, -100.0f, 0x993333FF},
    //     {0.0f, 100.0f, 0x333399FF},
    //     {-100.0f, 100.0f, 0x333399FF}
    // };

    int kRectIndices[] = {0, 1, 2, 0, 2, 3};

    Matrix4f proj = new Matrix4f();
    FloatBuffer proj_buffer;

    short vertex_buffer = -1;
    short index_buffer = -1;
    short program = -1;

    @Override
    public void setup() {
        // Enable debug text.
        // bgfx_set_debug(BGFX_DEBUG_TEXT);
        bgfx_set_debug(BGFX_DEBUG_TEXT | BGFX_DEBUG_WIREFRAME);
        // bgfx_set_debug(BGFX_DEBUG_TEXT | BGFX_DEBUG_STATS);
        bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x303030ff, 1.0f, 0);

        // // -- proj.setOrtho2D(left, right, bottom, top);
        proj.setOrtho2D(0.0f, width, height, 0.0f);

        App.logInfo("proj: \n" + proj.toString());

        proj_buffer = MemoryUtil.memAllocFloat(16);
        // proj_buffer = FloatBuffer.allocate(16);

        layout = BGFXUtil.createVertexLayout2D(false, true, 0);

        vertex_buffer = BGFXUtil.createVertexBuffer(byteSizeOf(XYC, 4), layout, kRectVertices);
        index_buffer = BGFXUtil.createIndexBuffer(kRectIndices);
        try {
            program = BGFXUtil.createRedShaderProgram();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to create shader program");
        }

        // check program is valid
        if (BGFX.BGFX_HANDLE_IS_VALID(program)) {
            App.logInfo("program handle is valid: " + program);
        } else {
            App.logWarning("program handle is invalid: " + program);
        }
    }

    @Override
    public void draw() {
        // background(0);
        background(125);
        // background(255);

        long encoder = bgfx_encoder_begin(false);

        // draw triangle

        float t = elapsedTimeSeconds();
        float s = 2000f * (float)Math.sin(t);

        // BGFX.bgfx_encoder_set_transform(encoder, proj.get(proj_buffer));
        BGFX.bgfx_encoder_set_transform(encoder,
            proj.scale(s, s, 1.0f).get(proj_buffer));

        bgfx_encoder_set_vertex_buffer(encoder, 0, vertex_buffer, 0, 4);
        bgfx_encoder_set_index_buffer(encoder, index_buffer, 0, 6);

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
        bgfx_dbg_text_printf(0, 0, 0x0f, "RedRect");
    }

    @Override
    public void exit() {
        MemoryUtil.memFree(proj_buffer);
        bgfx_destroy_program(program);
        bgfx_destroy_index_buffer(index_buffer);
        bgfx_destroy_vertex_buffer(vertex_buffer);
        layout.free();
    }

    public static void main(String[] args) {
        App.main("test.RedShaderSketch", width, height);
    }
}