package sketch.util;

import java.nio.ByteBuffer;

import org.lwjgl.bgfx.BGFX;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_COLOR0;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_NORMAL;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_POSITION;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TEXCOORD0;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_FLOAT;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_UINT8;
import static org.lwjgl.bgfx.BGFX.BGFX_BUFFER_NONE;
import org.lwjgl.bgfx.BGFXMemory;
import org.lwjgl.bgfx.BGFXVertexLayout;

public class BGFXUtil {
//   public static BGFXMemory makeRef(IntBuffer buffer) {
//     if (buffer == null) {
//       return null;
//     }

//     return BGFX.bgfx_make_ref(buffer);
//   }

//   protected static IntBuffer arrayToBuffer(int[] array) {
//     if (array == null) {
//       return null;
//     }

//     IntBuffer buffer = IntBuffer.allocate(array.length);
//     buffer.put(array);
//     buffer.flip();
//     return buffer;
//   }

//   public static BGFXMemory makeRef(int[] array) {
//     if (array == null) {
//       return null;
//     }

//     return BGFX.bgfx_make_ref(arrayToBuffer(array));
//   }

  public enum VertexLayoutType {
    XYC
  }

  public static int byteSizeOf(VertexLayoutType type, int count) {
    switch (type) {
      case XYC:
        return (2 * 4 + 4) * count;
      default:
        throw new RuntimeException("Invalid vertex layout type");
    }
  }

  public static BGFXVertexLayout createVertexLayout2D(boolean withNormals, boolean withColor, int numUVs) {

        BGFXVertexLayout layout = BGFXVertexLayout.calloc();

        int renderer = BGFX.bgfx_get_renderer_type();
        BGFX.bgfx_vertex_layout_begin(layout, renderer);

        BGFX.bgfx_vertex_layout_add(layout,
                BGFX_ATTRIB_POSITION,
                2,
                BGFX_ATTRIB_TYPE_FLOAT,
                false,
                false);

        if (withNormals) {
            BGFX.bgfx_vertex_layout_add(layout,
                    BGFX_ATTRIB_NORMAL,
                    3,
                    BGFX_ATTRIB_TYPE_FLOAT,
                    false,
                    false);
        }

        if (withColor) {
            BGFX.bgfx_vertex_layout_add(layout,
                    BGFX_ATTRIB_COLOR0,
                    4,
                    BGFX_ATTRIB_TYPE_UINT8,
                    true,
                    false);
        }

        if (numUVs > 0) {
            BGFX.bgfx_vertex_layout_add(layout,
                    BGFX_ATTRIB_TEXCOORD0,
                    2,
                    BGFX_ATTRIB_TYPE_FLOAT,
                    false,
                    false);
        }

        BGFX.bgfx_vertex_layout_end(layout);

        return layout;
    }

  // begin (partial) ref BGFXDemoUtil: https://github.com/LWJGL/lwjgl3-demos/blob/cd4a70daa3dad50c6c4a0d95e559d1bb7a349135/src/org/lwjgl/demo/bgfx/BGFXDemoUtil.java

  public static BGFXVertexLayout createVertexLayout3D(boolean withNormals, boolean withColor, int numUVs) {

        BGFXVertexLayout layout = BGFXVertexLayout.calloc();

        int renderer = BGFX.bgfx_get_renderer_type();
        BGFX.bgfx_vertex_layout_begin(layout, renderer);

        BGFX.bgfx_vertex_layout_add(layout,
                BGFX_ATTRIB_POSITION,
                3,
                BGFX_ATTRIB_TYPE_FLOAT,
                false,
                false);

        if (withNormals) {
            BGFX.bgfx_vertex_layout_add(layout,
                    BGFX_ATTRIB_NORMAL,
                    3,
                    BGFX_ATTRIB_TYPE_FLOAT,
                    false,
                    false);
        }

        if (withColor) {
            BGFX.bgfx_vertex_layout_add(layout,
                    BGFX_ATTRIB_COLOR0,
                    4,
                    BGFX_ATTRIB_TYPE_UINT8,
                    true,
                    false);
        }

        if (numUVs > 0) {
            BGFX.bgfx_vertex_layout_add(layout,
                    BGFX_ATTRIB_TEXCOORD0,
                    2,
                    BGFX_ATTRIB_TYPE_FLOAT,
                    false,
                    false);
        }

        BGFX.bgfx_vertex_layout_end(layout);

        return layout;
    }

    public static short createVertexBuffer(ByteBuffer buffer, BGFXVertexLayout layout, Object[][] vertices) {

        for (Object[] vtx : vertices) {
            for (Object attr : vtx) {
                if (attr instanceof Float) {
                    buffer.putFloat((float) attr);
                } else if (attr instanceof Integer) {
                    buffer.putInt((int) attr);
                } else {
                    throw new RuntimeException("Invalid parameter type");
                }
            }
        }

        if (buffer.remaining() != 0) {
            throw new RuntimeException("ByteBuffer size and number of arguments do not match");
        }

        buffer.flip();

        return createVertexBuffer(buffer, layout);
    }

    public static short createVertexBuffer(int byte_size, BGFXVertexLayout layout, Object[][] vertices){
        ByteBuffer buffer = ByteBuffer.allocateDirect(byte_size);
        return createVertexBuffer(buffer, layout, vertices);
    }

    public static short createVertexBuffer(ByteBuffer buffer, BGFXVertexLayout layout) {
        BGFXMemory vbhMem = BGFX.bgfx_make_ref(buffer);
        return BGFX.bgfx_create_vertex_buffer(vbhMem, layout, BGFX_BUFFER_NONE);
    }

    public static short createIndexBuffer(int[] indices) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(indices.length * 2);
        return createIndexBuffer(buffer, indices);
    }

    public static short createIndexBuffer(ByteBuffer buffer, int[] indices) {

        for (int idx : indices) {
            buffer.putShort((short) idx);
        }

        if (buffer.remaining() != 0) {
            throw new RuntimeException("ByteBuffer size and number of arguments do not match");
        }

        buffer.flip();

        BGFXMemory ibhMem = BGFX.bgfx_make_ref(buffer);

        return BGFX.bgfx_create_index_buffer(ibhMem, BGFX_BUFFER_NONE);
    }

    // end of (partial) ref BGFXDemoUtil
}