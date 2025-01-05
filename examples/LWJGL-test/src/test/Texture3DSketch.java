package test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import static org.lwjgl.bgfx.BGFX.*;
import org.lwjgl.bgfx.BGFXVertexLayout;
import org.lwjgl.system.MemoryUtil;

import sketch.App;
import sketch.Sketch;
import sketch.util.BGFXUtil;
import static sketch.util.BGFXUtil.VertexLayoutType.XYZC;
import static sketch.util.BGFXUtil.byteSizeOf;

public class Texture3DSketch extends Sketch {
    private BGFXVertexLayout layout;

    Object[][] kRect3DVertices = {
        // x, y, color, u, v
        {-0.5f, -0.5f, 1.0f, 0x339933FF, 0.0f, 1.0f},
        {0.5f, -0.5f, 1.0f, 0x993333FF, 1.0f, 1.0f},
        {0.5f, 0.5f, 1.0f, 0x333399FF, 1.0f, 0.0f},
        {-0.5f, 0.5f, 1.0f, 0x999999FF, 0.0f, 0.0f}
    };

    static int width = 640;
    static int height = 480;

    int kRect3DIndices[] = {0, 1, 2, 0, 2, 3};

    ByteBuffer vertices;
    ByteBuffer indices;

    short vertex_buffer = -1;
    short index_buffer = -1;
    short program = -1;

    short texture = -1;
    short sampler = -1;

    Matrix4x3f view = new Matrix4x3f();
    FloatBuffer viewBuf;
    Matrix4f proj = new Matrix4f();
    FloatBuffer projBuf;
    Matrix4x3f model = new Matrix4x3f();
    FloatBuffer modelBuf;

    @Override
    public void setup() throws IOException {
        // Enable debug text.
        bgfx_set_debug(BGFX_DEBUG_TEXT);
        bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x303030ff, 1.0f, 0);
        // bgfx_set_view_clear(0, BGFX_CLEAR_COLOR, 0x303030ff, 1.0f, 0);

        layout = BGFXUtil.createVertexLayout3D(false, true, 1);

        // vertex_buffer = BGFXUtil.createVertexBuffer(byteSizeOf(XYZC, 4), layout, kRect3DVertices);
        // index_buffer = BGFXUtil.createIndexBuffer(kRect3DIndices);
        vertices = MemoryUtil.memAlloc(byteSizeOf(XYZC, 4, 1));
        indices = MemoryUtil.memAlloc(kRect3DIndices.length * 2);
        vertex_buffer = BGFXUtil.createVertexBuffer(vertices, layout, kRect3DVertices);
        index_buffer = BGFXUtil.createIndexBuffer(indices, kRect3DIndices);

        viewBuf = MemoryUtil.memAllocFloat(16);
        projBuf = MemoryUtil.memAllocFloat(16);
        modelBuf = MemoryUtil.memAllocFloat(16);

        program = BGFXUtil.createShaderProgram("vs_tex3d", "fs_tex3d");

        sampler = bgfx_create_uniform("s_texColor", BGFX_UNIFORM_TYPE_SAMPLER, 1);
        texture = BGFXUtil.loadTexture("bgfx_logo.ktx");

        BGFXUtil.assertHandle(program, "Failed to create shader program");
        BGFXUtil.assertHandle(sampler, "Failed to create sampler");
        BGFXUtil.assertHandle(texture, "Failed to load texture");
    }

    @Override
    public void draw() {
        // background(0);
        // background(125);
        // background(255);

        // bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x000000ff, 100.0f, 0);

        BGFXUtil.lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, -35.0f), view);
        BGFXUtil.perspective(60.0f, width, height, 0.1f, 100.0f, proj);

        bgfx_set_view_transform(0, view.get4x4(viewBuf), proj.get(projBuf));

        long encoder = bgfx_encoder_begin(false);

        // draw rect with texture

        bgfx_encoder_set_texture(encoder, 0, sampler, texture,
            BGFX_SAMPLER_U_CLAMP
            | BGFX_SAMPLER_V_CLAMP
            // | BGFX_SAMPLER_U_MIRROR
            // | BGFX_SAMPLER_V_MIRROR
        );

        float t = elapsedTimeSeconds();
        float s = (float) Math.sin(t);
        bgfx_encoder_set_transform(encoder,
            model.translation(
                        -15.0f + s * 10.0f,
                        -15.0f + s * 10.0f,
                        s * 10.0f)
                        // 0.0f)
                    .rotateXYZ(
                        t + s * 0.21f,
                        t + s * 0.37f,
                        0.0f)
                    .scale(20.0f, 20.0f, 20.0f)
                    .get4x4(modelBuf));

        bgfx_encoder_set_vertex_buffer(encoder, 0, vertex_buffer, 0, 4);
        bgfx_encoder_set_index_buffer(encoder, index_buffer, 0, 6);

        bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT, 0);
        // bgfx_encoder_set_state(encoder,
        //     BGFX_STATE_WRITE_RGB
        //     | BGFX_STATE_WRITE_A
        //     | BGFX_STATE_WRITE_Z
        //     | BGFX_STATE_MSAA,
        //     0);

        bgfx_encoder_submit(encoder, 0, program, 0, 0);

        bgfx_encoder_end(encoder);

        // Use debug font to print information about this example.
        bgfx_dbg_text_clear(0, false);
        bgfx_dbg_text_printf(0, 0, 0x0f, "Texture3D");
    }

    @Override
    public void exit() {
        bgfx_destroy_program(program);
        bgfx_destroy_index_buffer(index_buffer);
        bgfx_destroy_vertex_buffer(vertex_buffer);
        MemoryUtil.memFree(viewBuf);
        MemoryUtil.memFree(projBuf);
        MemoryUtil.memFree(modelBuf);
        layout.free();

        MemoryUtil.memFree(vertices);
        MemoryUtil.memFree(indices);

        bgfx_destroy_texture(texture);
        bgfx_destroy_uniform(sampler);
    }

    public static void main(String[] args) {
        App.main("test.Texture3DSketch", width, height);
    }
}