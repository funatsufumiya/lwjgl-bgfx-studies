package test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
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
import static org.lwjgl.bgfx.BGFX.bgfx_set_view_transform;
import org.lwjgl.bgfx.BGFXVertexLayout;
import org.lwjgl.system.MemoryUtil;

import sketch.App;
import sketch.Sketch;
import static sketch.core.PGraphics.background;
import sketch.util.BGFXUtil;
import static sketch.util.BGFXUtil.VertexLayoutType.XYC;
import static sketch.util.BGFXUtil.byteSizeOf;

public class RandomSketch extends Sketch {
    static int width = 640;
    static int height = 480;

    private BGFXVertexLayout layout;

    private static final Object[][] kTriangleVertices = {
        {-0.5f, -0.5f, 0x339933FF},
        {0.5f, -0.5f, 0x993333FF},
        {0.0f, 0.5f, 0x333399FF}
    };

    int kTriangleIndices[] = {0, 1, 2};

    Matrix4f proj = new Matrix4f();
    Matrix4x3f view = new Matrix4x3f();
    Matrix4x3f model = new Matrix4x3f();
    FloatBuffer proj_buffer;
    FloatBuffer view_buffer;
    FloatBuffer model_buffer;

    ByteBuffer vertices;
    ByteBuffer indices;

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

        proj_buffer = MemoryUtil.memAllocFloat(16);
        view_buffer = MemoryUtil.memAllocFloat(16);
        model_buffer = MemoryUtil.memAllocFloat(16);

        layout = BGFXUtil.createVertexLayout2D(false, true, 0);

        vertices = MemoryUtil.memAlloc(byteSizeOf(XYC, 3));
        indices = MemoryUtil.memAlloc(kTriangleIndices.length * 2);

        vertex_buffer = BGFXUtil.createVertexBuffer(vertices, layout, kTriangleVertices);
        index_buffer = BGFXUtil.createIndexBuffer(indices, kTriangleIndices);
        try {
            // program = BGFXUtil.createBasicShaderProgram();
            program = BGFXUtil.createRandomShaderProgram();
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

        BGFXUtil.lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, -35.0f), view);
        BGFXUtil.perspective(60.0f, width, height, 0.1f, 100.0f, proj);

        bgfx_set_view_transform(0, view.get4x4(view_buffer), proj.get(proj_buffer));

        long encoder = bgfx_encoder_begin(false);

        // draw triangle

        float t = elapsedTimeSeconds();
        float s = (float) Math.sin(t) * 100.0f;

        BGFX.bgfx_encoder_set_transform(encoder,
            // model.identity().rotateY(elapsedTimeSeconds()).get4x4(model_buffer)
            model.translation(s, s, 0.0f).rotateY(t).get4x4(model_buffer)
        );

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
        bgfx_dbg_text_printf(0, 0, 0x0f, "Random");
    }

    @Override
    public void exit() {
        MemoryUtil.memFree(proj_buffer);
        MemoryUtil.memFree(view_buffer);
        MemoryUtil.memFree(model_buffer);

        bgfx_destroy_program(program);
        bgfx_destroy_index_buffer(index_buffer);
        MemoryUtil.memFree(indices);
        bgfx_destroy_vertex_buffer(vertex_buffer);
        MemoryUtil.memFree(vertices);

        layout.free();
    }

    public static void main(String[] args) {
        App.main("test.RandomSketch", width, height);
    }
}