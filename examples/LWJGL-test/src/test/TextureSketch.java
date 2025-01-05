package test;

import java.io.IOException;

import org.lwjgl.bgfx.BGFX;
import static org.lwjgl.bgfx.BGFX.*;
import org.lwjgl.bgfx.BGFXVertexLayout;

import sketch.App;
import sketch.Sketch;
import static sketch.core.PGraphics.background;
import sketch.util.BGFXUtil;
import static sketch.util.BGFXUtil.VertexLayoutType.XYC;
import static sketch.util.BGFXUtil.byteSizeOf;

public class TextureSketch extends Sketch {
    private BGFXVertexLayout layout;

    private static final Object[][] kRectVertices = {
        {-0.5f, -0.5f, 0.0f, 0.0f, 0x339933FF}, // x, y, u, v, color
        { 0.5f, -0.5f, 1.0f, 0.0f, 0x993333FF},
        { 0.5f,  0.5f, 1.0f, 1.0f, 0x333399FF},
        {-0.5f,  0.5f, 0.0f, 1.0f, 0x999999FF}

        // {-1.0f, -1.0f, 0.0f, 0.0f, 0x339933FF}, // x, y, u, v, color
        // { 1.0f, -1.0f, 1.0f, 0.0f, 0x993333FF},
        // { 1.0f,  1.0f, 1.0f, 1.0f, 0x333399FF},
        // {-1.0f,  1.0f, 0.0f, 1.0f, 0x999999FF}
    };

    int kRectIndices[] = {0, 1, 2, 0, 2, 3};

    short vertex_buffer = -1;
    short index_buffer = -1;
    short program = -1;

    short texture = -1;
    short sampler = -1;

    @Override
    public void setup() throws IOException {
        // Enable debug text.
        bgfx_set_debug(BGFX_DEBUG_TEXT);
        bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x303030ff, 1.0f, 0);

        layout = BGFXUtil.createVertexLayout2D(false, true, 1);

        vertex_buffer = BGFXUtil.createVertexBuffer(byteSizeOf(XYC, 4, 1), layout, kRectVertices);
        index_buffer = BGFXUtil.createIndexBuffer(kRectIndices);
        program = BGFXUtil.createShaderProgram("vs_tex2d", "fs_tex2d");

        sampler = bgfx_create_uniform("s_texColor", BGFX_UNIFORM_TYPE_SAMPLER, 1);
        texture = BGFXUtil.loadTexture("bgfx_logo.ktx");

        BGFXUtil.assertHandle(program, "Failed to create shader program");
        BGFXUtil.assertHandle(sampler, "Failed to create sampler");
        BGFXUtil.assertHandle(texture, "Failed to load texture");
    }

    @Override
    public void draw() {
        // background(0);
        background(125);
        // background(255);

        long encoder = bgfx_encoder_begin(false);

        // draw rect with texture

        bgfx_encoder_set_texture(encoder, 0, sampler, texture, BGFX_SAMPLER_U_CLAMP | BGFX_SAMPLER_V_CLAMP);

        bgfx_encoder_set_vertex_buffer(encoder, 0, vertex_buffer, 0, 4);
        bgfx_encoder_set_index_buffer(encoder, index_buffer, 0, 6);

        // bgfx_encoder_set_state(encoder, BGFX_STATE_DEFAULT, 0);
        bgfx_encoder_set_state(encoder,
            BGFX_STATE_WRITE_RGB
                | BGFX_STATE_WRITE_A
                | BGFX_STATE_BLEND_ALPHA,
            0
        );

        bgfx_encoder_submit(encoder, 0, program, 0, 0);

        bgfx_encoder_end(encoder);

        // Use debug font to print information about this example.
        bgfx_dbg_text_clear(0, false);
        bgfx_dbg_text_printf(0, 0, 0x0f, "Rect");
    }

    @Override
    public void exit() {
        bgfx_destroy_program(program);
        bgfx_destroy_index_buffer(index_buffer);
        bgfx_destroy_vertex_buffer(vertex_buffer);
        bgfx_destroy_texture(texture);
        bgfx_destroy_uniform(sampler);
        layout.free();
    }

    public static void main(String[] args) {
        App.main("test.TextureSketch", 640, 480);
    }
}