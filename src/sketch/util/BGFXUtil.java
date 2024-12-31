package sketch.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.lwjgl.bgfx.BGFX;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_COLOR0;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_NORMAL;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_POSITION;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TEXCOORD0;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_FLOAT;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_UINT8;
import static org.lwjgl.bgfx.BGFX.BGFX_BUFFER_NONE;
import static org.lwjgl.bgfx.BGFX.BGFX_RENDERER_TYPE_DIRECT3D11;
import static org.lwjgl.bgfx.BGFX.BGFX_RENDERER_TYPE_DIRECT3D12;
import static org.lwjgl.bgfx.BGFX.BGFX_RENDERER_TYPE_METAL;
import static org.lwjgl.bgfx.BGFX.BGFX_RENDERER_TYPE_OPENGL;
import static org.lwjgl.bgfx.BGFX.BGFX_RENDERER_TYPE_VULKAN;
import static org.lwjgl.bgfx.BGFX.BGFX_TEXTURE_NONE;
import static org.lwjgl.bgfx.BGFX.bgfx_create_shader;
import static org.lwjgl.bgfx.BGFX.bgfx_create_texture;
import static org.lwjgl.bgfx.BGFX.bgfx_get_renderer_name;
import static org.lwjgl.bgfx.BGFX.bgfx_make_ref_release;
import org.lwjgl.bgfx.BGFXMemory;
import org.lwjgl.bgfx.BGFXReleaseFunctionCallback;
import org.lwjgl.bgfx.BGFXVertexLayout;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import sketch.App;

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

        // int renderer = BGFX.bgfx_get_renderer_type();
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

  private static BGFXReleaseFunctionCallback releaseMemoryCb = BGFXReleaseFunctionCallback.create(
    (_ptr, _userData) -> {
        App.logVerbose("BGFXUtil: releasing memory at " + _ptr);
        nmemFree(_ptr);
    });

    public static void dispose() {
        App.logVerbose("BGFXUtil: dispose()");
        releaseMemoryCb.free();
    }

  private static final String SHADER_RESOURCE_PATH = "/res/shaders/";
  private static final String TEXTURE_RESOURCE_PATH = "/res/textures/";

  private static ByteBuffer loadResource(String resourcePath, String name) throws IOException {

        String pwd = System.getProperty("user.dir");

        // URL url = BGFXUtil.class.getResource(resourcePath + name);
        // Path path = Paths.get(resourcePath + name);
        Path path = Paths.get(pwd + resourcePath + name);

        // if (url == null) {
        if (!path.toFile().exists()) {
            App.logWarning("Resource not found: " + path);
            App.logWarning("(Please compile shaders or textures before running.)");
            throw new IOException("Resource not found: " + path);
        }

        // int resourceSize = url.openConnection().getContentLength();
        int resourceSize = (int) path.toFile().length();

        // App.logInfo("bgfx: loading resource '" + url.getFile() + "' (" + resourceSize + " bytes)");
        App.logInfo("bgfx: loading resource '" + path + "' (" + resourceSize + " bytes)");

        ByteBuffer resource = memAlloc(resourceSize);
        // ByteBuffer resource = memAlloc(resourceSize + 3); // arbitrary add abc as debug

        // try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
        try (BufferedInputStream bis = new BufferedInputStream(path.toUri().toURL().openStream())) {
            int b;
            do {
                b = bis.read();
                if (b != -1) {
                    resource.put((byte) b);
                }
            } while (b != -1);
        }

        // debug (arbitrary add abc)
        // resource.put((byte) 'a');
        // resource.put((byte) 'b');
        // resource.put((byte) 'c');

        resource.flip();

        return resource;
    }

  public static short loadShader(String name) throws IOException {

        String resourcePath = SHADER_RESOURCE_PATH;
        // int renderer = bgfx_get_renderer_type();

        switch (renderer) {
            case BGFX_RENDERER_TYPE_DIRECT3D11:
            case BGFX_RENDERER_TYPE_DIRECT3D12:
                resourcePath += "dx11/";
                break;

            case BGFX_RENDERER_TYPE_OPENGL:
                resourcePath += "glsl/";
                break;

            case BGFX_RENDERER_TYPE_METAL:
                resourcePath += "metal/";
                break;
            
            case BGFX_RENDERER_TYPE_VULKAN:
                resourcePath += "spirv/";
                break;

            default:
                throw new IOException("No demo shaders supported for " + bgfx_get_renderer_name(renderer) + " renderer");
        }

        ByteBuffer shaderCode = loadResource(resourcePath, name + ".bin");

        return bgfx_create_shader(bgfx_make_ref_release(shaderCode, releaseMemoryCb, NULL));
    }

    public static short loadShader(char[] shaderCodeGLSL, char[] shaderCodeSPIRV, char[] shaderCodeD3D11, char[] shaderCodeMtl) throws IOException {
        char[] sc;

        // int renderer = bgfx_get_renderer_type();
        switch (renderer) {
            case BGFX_RENDERER_TYPE_DIRECT3D11:
            case BGFX_RENDERER_TYPE_DIRECT3D12:
                sc = shaderCodeD3D11;
                break;

            case BGFX_RENDERER_TYPE_OPENGL:
                sc = shaderCodeGLSL;
                break;

            case BGFX_RENDERER_TYPE_METAL:
                sc = shaderCodeMtl;
                break;

            case BGFX_RENDERER_TYPE_VULKAN:
                sc = shaderCodeSPIRV;
                break;

            default:
                throw new IOException("No demo shaders supported for " + bgfx_get_renderer_name(renderer) + " renderer");
        }

        ByteBuffer shaderCode = memAlloc(sc.length);
        // ByteBuffer shaderCode = memAlloc(sc.length + 3); // arbitrary add abc as debug
        // ByteBuffer shaderCode = ByteBuffer.allocateDirect(sc.length);

        for (char c : sc) {
            shaderCode.put((byte) c);
        }

        // debug (arbitrary add abc)
        // shaderCode.put((byte) 'a');
        // shaderCode.put((byte) 'b');
        // shaderCode.put((byte) 'c');

        shaderCode.flip();

        return bgfx_create_shader(bgfx_make_ref_release(shaderCode, releaseMemoryCb, NULL));
    }

    public static short loadTexture(String fileName) throws IOException {

        ByteBuffer textureData = loadResource(TEXTURE_RESOURCE_PATH, fileName);

        BGFXMemory textureMemory = bgfx_make_ref_release(textureData, releaseMemoryCb, NULL);

        return bgfx_create_texture(textureMemory, BGFX_TEXTURE_NONE, 0, null);
    }

  public static BGFXVertexLayout createVertexLayout3D(boolean withNormals, boolean withColor, int numUVs) {

        BGFXVertexLayout layout = BGFXVertexLayout.calloc();

        // int renderer = BGFX.bgfx_get_renderer_type();
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

    private static int renderer = -1;
    private static boolean zZeroToOne;

    public static void _configure() {
        BGFXUtil.renderer = BGFX.bgfx_get_renderer_type();
        BGFXUtil.zZeroToOne = !BGFX.bgfx_get_caps().homogeneousDepth();
    }

    public static void lookAt(Vector3f at, Vector3f eye, Matrix4x3f dest) {
        dest.setLookAtLH(eye.x, eye.y, eye.z, at.x, at.y, at.z, 0.0f, 1.0f, 0.0f);
    }

    public static void perspective(float fov, int width, int height, float near, float far, Matrix4f dest) {
        float fovRadians = fov * (float) Math.PI / 180.0f;
        float aspect = width / (float) height;
        dest.setPerspectiveLH(fovRadians, aspect, near, far, zZeroToOne);
    }

    public static void ortho(float left, float right, float bottom, float top, float zNear, float zFar, Matrix4x3f dest) {
        dest.setOrthoLH(left, right, bottom, top, zNear, zFar, zZeroToOne);
    }

    // end of (partial) ref BGFXDemoUtil
    
    // begin (partial) ref big2-stack/examples/src/triangle.cpp: https://github.com/Paper-Cranes-Ltd/big2-stack/blob/a2d01c42a8f66d120e1621a3600ef60d1aee7a30/examples/src/triangle.cpp

    public static short createShaderProgram(String vs_name, String fs_name) throws IOException {
        return BGFX.bgfx_create_program(
                BGFXUtil.loadShader(vs_name),
                BGFXUtil.loadShader(fs_name),
                true);
    }

    public static short createBasicShaderProgram() throws IOException {
        return createShaderProgram("vs_basic", "fs_basic");
    }

    public static short createRedShaderProgram() throws IOException {
        return createShaderProgram("vs_red", "fs_red");
    }

    // end of (partial) ref big2-stack/examples/src/triangle.cpp
}