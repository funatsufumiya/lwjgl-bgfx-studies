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

public class TriangleSketch extends Sketch {
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

        // const bgfx::Caps* caps = bgfx::getCaps();

        // float proj[16];
        // // -- void mtxOrtho(float* _result, float _left, float _right, float _bottom, float _top, float _near, float _far, float _offset, bool _homogeneousNdc, Handedness::Enum _handedness)
        // void mtxOrtho(float* _result, float _left, float _right, float _bottom, float _top, float _near, float _far, float _offset, bool _homogeneousNdc, Handedness::Enum _handedness)
        // {
        //     const float aa = 2.0f/(_right - _left);
        //     const float bb = 2.0f/(_top - _bottom);
        //     const float cc = (_homogeneousNdc ? 2.0f : 1.0f) / (_far - _near);
        //     const float dd = (_left + _right )/(_left   - _right);
        //     const float ee = (_top  + _bottom)/(_bottom - _top  );
        //     const float ff = _homogeneousNdc
        //         ? (_near + _far)/(_near - _far)
        //         :  _near        /(_near - _far)
        //         ;

        //     memSet(_result, 0, sizeof(float)*16);
        //     _result[ 0] = aa;
        //     _result[ 5] = bb;
        //     _result[10] = Handedness::Right == _handedness ? -cc : cc;
        //     _result[12] = dd + _offset;
        //     _result[13] = ee;
        //     _result[14] = ff;
        //     _result[15] = 1.0f;
        // }

        // bx::mtxOrtho(
        //     proj
        //     , 0.0f
        //     , (float)m_screenWidth
        //     , (float)m_screenHeight
        //     , 0.0f
        //     , 0.0f
        //     , 1000.0f
        //     , 0.0f
        //     , caps->homogeneousDepth
        //     );

        // BGFXCaps caps = BGFX.bgfx_get_caps();
        // // true if [-1, 1] depth range, false if [0, 1]
        // boolean is_hd = caps.homogeneousDepth();
        // boolean zZeroToOne = !is_hd;

        // App.logInfo("is zZeroToOne: " + zZeroToOne);

        // // -- setOrtho(float left, float right, float bottom, float top, float zNear, float zFar)
        // // -- setOrtho(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne)
        // proj.setOrtho(
        //     0.0f,
        //     (float)width,
        //     (float)height,
        //     0.0f,
        //     0.0f,
        //     1000.0f,
        //     zZeroToOne);

        // // -- proj.setOrtho2D(left, right, bottom, top);
        proj.setOrtho2D(0.0f, width, height, 0.0f);

        App.logInfo("proj: \n" + proj.toString());

        proj_buffer = MemoryUtil.memAllocFloat(16);
        // proj_buffer = FloatBuffer.allocate(16);

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

        BGFX.bgfx_encoder_set_transform(encoder, proj.get(proj_buffer));

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
        MemoryUtil.memFree(proj_buffer);
        bgfx_destroy_program(program);
        bgfx_destroy_index_buffer(index_buffer);
        bgfx_destroy_vertex_buffer(vertex_buffer);
        layout.free();
    }

    public static void main(String[] args) {
        App.main("test.TriangleSketch", width, height);
    }
}