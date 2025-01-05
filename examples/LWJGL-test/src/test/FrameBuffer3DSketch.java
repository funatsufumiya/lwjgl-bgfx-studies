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
import static sketch.util.BGFXUtil.VertexLayoutType.XYC;
import static sketch.util.BGFXUtil.VertexLayoutType.XYZC;
import static sketch.util.BGFXUtil.byteSizeOf;

public class FrameBuffer3DSketch extends Sketch {
    private BGFXVertexLayout layout2d;

    private static final Object[][] kTriangle2DVertices = {
        {-0.5f, -0.5f, 0x339933FF},
        {0.5f, -0.5f, 0x993333FF},
        {0.0f, 0.5f, 0x333399FF}
    };

    int kTriangle2DIndices[] = {0, 1, 2};

    ByteBuffer vertices_triangle2d;
    ByteBuffer indices_triangle2d;

    short triangle2d_vertex_buffer = -1;
    short triangle2d_index_buffer = -1;
    short triangle2d_program = -1;

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

    short frameBuffer = -1;
    short frameBufferTexture = -1;
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

        frameBuffer = bgfx_create_frame_buffer(width, height, BGFX_TEXTURE_FORMAT_RGBA8, BGFX_TEXTURE_NONE);
        sampler = bgfx_create_uniform("s_texColor", BGFX_UNIFORM_TYPE_SAMPLER, 1);

        frameBufferTexture = bgfx_get_texture(frameBuffer, 0);

        BGFXUtil.assertHandle(program, "Failed to create shader program");
        BGFXUtil.assertHandle(sampler, "Failed to create sampler");
        BGFXUtil.assertHandle(frameBuffer, "Failed to create frame buffer");

        // init triangle2d
        layout2d = BGFXUtil.createVertexLayout2D(false, true, 0);
        vertices_triangle2d = MemoryUtil.memAlloc(byteSizeOf(XYC, 3));
        indices_triangle2d = MemoryUtil.memAlloc(kTriangle2DIndices.length * 2);
        
        triangle2d_vertex_buffer = BGFXUtil.createVertexBuffer(vertices_triangle2d, layout2d, kTriangle2DVertices);
        triangle2d_index_buffer = BGFXUtil.createIndexBuffer(indices_triangle2d, kTriangle2DIndices);
        triangle2d_program = BGFXUtil.createShaderProgram("vs_basic", "fs_basic");
    }

    protected void update_fbo(){
        int fbo_view_id = 1;

        bgfx_set_view_frame_buffer(fbo_view_id, frameBuffer);
        bgfx_set_view_rect(fbo_view_id, 0, 0, width, height);

        // draw red background
        bgfx_set_view_clear(fbo_view_id, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0xff0000ff, 1.0f, 0);

        bgfx_touch(fbo_view_id);

        // draw triangle2d
        long encoder = bgfx_encoder_begin(false);
        bgfx_encoder_set_vertex_buffer(encoder, 0, triangle2d_vertex_buffer, 0, 3);
        bgfx_encoder_set_index_buffer(encoder, triangle2d_index_buffer, 0, 3);
        bgfx_encoder_set_state(encoder,
            BGFX_STATE_WRITE_RGB
                | BGFX_STATE_WRITE_A,
            0
        );
        bgfx_encoder_submit(encoder, fbo_view_id, triangle2d_program, 0, 0);
        bgfx_encoder_end(encoder);
    }

    @Override
    public void draw() {
        update_fbo();

        // background(0);
        // background(125);
        // background(255);

        // bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x000000ff, 100.0f, 0);

        BGFXUtil.lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, -35.0f), view);
        BGFXUtil.perspective(60.0f, width, height, 0.1f, 100.0f, proj);

        bgfx_set_view_transform(0, view.get4x4(viewBuf), proj.get(projBuf));

        long encoder = bgfx_encoder_begin(false);

        // draw rect with texture

        bgfx_encoder_set_texture(encoder, 0, sampler, frameBufferTexture,
            BGFX_SAMPLER_U_CLAMP
            | BGFX_SAMPLER_V_CLAMP
            // | BGFX_SAMPLER_MIP_POINT
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

        // bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT, 0);
        // bgfx_encoder_set_state(encoder,
        //     BGFX_STATE_WRITE_RGB
        //     | BGFX_STATE_WRITE_A
        //     | BGFX_STATE_WRITE_Z
        //     | BGFX_STATE_MSAA,
        //     0);
        bgfx_encoder_set_state(encoder, 
            BGFX_STATE_WRITE_RGB
            | BGFX_STATE_WRITE_A
            | BGFX_STATE_WRITE_Z
            | BGFX_STATE_DEPTH_TEST_LESS
            | BGFX_STATE_MSAA, 
            0);

        bgfx_encoder_submit(encoder, 0, program, 0, 0);

        bgfx_encoder_end(encoder);

        // Use debug font to print information about this example.
        bgfx_dbg_text_clear(0, false);
        bgfx_dbg_text_printf(0, 0, 0x0f, "FrameBuffer3D");
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

        // bgfx_destroy_texture(texture);
        
        bgfx_destroy_frame_buffer(frameBuffer);
        bgfx_destroy_texture(frameBufferTexture);
        bgfx_destroy_uniform(sampler);

        // delete triangle2d
        layout2d.free();
        MemoryUtil.memFree(vertices_triangle2d);
        MemoryUtil.memFree(indices_triangle2d);
        bgfx_destroy_vertex_buffer(triangle2d_vertex_buffer);
        bgfx_destroy_index_buffer(triangle2d_index_buffer);
        bgfx_destroy_program(triangle2d_program);
    }

    public static void main(String[] args) {
        App.main("test.FrameBuffer3DSketch", width, height);
    }
}