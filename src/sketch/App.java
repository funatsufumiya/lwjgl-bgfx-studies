package sketch;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.bgfx.BGFX.BGFX_FATAL_DEBUG_CHECK;
import static org.lwjgl.bgfx.BGFX.BGFX_NATIVE_WINDOW_HANDLE_TYPE_WAYLAND;
import static org.lwjgl.bgfx.BGFX.BGFX_RESET_VSYNC;
import static org.lwjgl.bgfx.BGFX.bgfx_frame;
import static org.lwjgl.bgfx.BGFX.bgfx_get_renderer_name;
import static org.lwjgl.bgfx.BGFX.bgfx_get_renderer_type;
import static org.lwjgl.bgfx.BGFX.bgfx_init;
import static org.lwjgl.bgfx.BGFX.bgfx_init_ctor;
import static org.lwjgl.bgfx.BGFX.bgfx_set_view_rect;
import static org.lwjgl.bgfx.BGFX.bgfx_shutdown;
import static org.lwjgl.bgfx.BGFX.bgfx_touch;
import org.lwjgl.bgfx.BGFXCallbackInterface;
import org.lwjgl.bgfx.BGFXCallbackVtbl;
import org.lwjgl.bgfx.BGFXInit;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_NO_API;
import static org.lwjgl.glfw.GLFW.GLFW_PLATFORM_COCOA;
import static org.lwjgl.glfw.GLFW.GLFW_PLATFORM_WAYLAND;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPlatform;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWayland;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memASCII;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import org.lwjgl.system.Platform;
import org.lwjgl.system.libc.LibCStdio;

import sketch.util.BGFXUtil;

public class App {
    static {
        System.setProperty("java.awt.headless", "true");
    }

    public enum LogLevel {
        ERROR(0),
        WARN(1),
        INFO(2),
        DEBUG(3),
        VERBOSE(4);
        ;
        public final int value;
        private LogLevel(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    // private static LogLevel loglevel = LogLevel.INFO;
    private static LogLevel loglevel = LogLevel.DEBUG;

    public static void setLogLevel(LogLevel level) {
        loglevel = level;
    }

    public static boolean isDebug() {
        return loglevel.getValue() >= LogLevel.DEBUG.getValue();
    }

    public static boolean isInfo() {
        return loglevel.getValue() >= LogLevel.INFO.getValue();
    }

    public static boolean isVerbose() {
        return loglevel.getValue() >= LogLevel.VERBOSE.getValue();
    }

    public static boolean isWarning() {
        return loglevel.getValue() >= LogLevel.WARN.getValue();
    }

    public static void logWarning(String message) {
        System.out.println("[Warn] " + message);
    }

    public static void logInfo(String message) {
        System.out.println("[Info] " + message);
    }

    public static void logDebug(String message) {
        if(isDebug()) {
            System.out.println("[Debug] " + message);
        }
    }

    public static void logVerbose(String message) {
        if(isVerbose()) {
            System.out.println("[Verbose] " + message);
        }
    }

    public static void main(String clazz, int width, int height) {
        String title = clazz;
        mainImpl(clazz, new WindowInitSetting(width, height, title), new String[0]);
    }

    public static void main(String clazz, int width, int height, String title) {
        mainImpl(clazz, new WindowInitSetting(width, height, title), new String[0]);
    }

    public static void main(String clazz, int width, int height, String[] args) {
        String title = clazz;
        mainImpl(clazz, new WindowInitSetting(width, height, title), args);
    }

    public static void main(String clazz, int width, int height, String title, String[] args) {
        mainImpl(clazz, new WindowInitSetting(width, height, title), args);
    }

    public static void main(String clazz, WindowInitSetting windowInitSetting, String[] args) {
        mainImpl(clazz, windowInitSetting, args);
    }

    public static void mainImpl(String clazz, WindowInitSetting windowInitSetting, String[] args) {
        try {
            Class<?> c = Class.forName(clazz);
            Object o = c.newInstance();
            if (!(o instanceof Sketch)) {
                throw new IllegalArgumentException("Class " + clazz + " does not extend Sketch");
            }

            Sketch sketch = (Sketch) o;

            if (isDebug()) {
                System.setProperty("org.lwjgl.util.Debug","true");
            }

            if (isVerbose()) {
                System.out.println("setting glfw_async");
            }

            if (Platform.get() == Platform.MACOSX) {
                Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
            }

            if (isVerbose()) {
                System.out.println("glfw init");
            }

            GLFWErrorCallback.createThrow().set();
            if (!glfwInit()) {
                throw new RuntimeException("Error initializing GLFW");
            }

            // the client (renderer) API is managed by bgfx
            glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
            if (glfwGetPlatform() == GLFW_PLATFORM_COCOA) {
                glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
            }
            
            if (isVerbose()) {
                System.out.println("glfw create window");
            }

            int width = windowInitSetting.getWidth();
            int height = windowInitSetting.getHeight();
            String title = windowInitSetting.getTitle();
            long window = glfwCreateWindow(width, height, title, 0, 0);

            if (window == NULL) {
                throw new RuntimeException("Error creating GLFW window");
            }

            glfwSetKeyCallback(window, (windowHnd, key, scancode, action, mods) -> {
                if (action != GLFW_RELEASE) {
                    return;
                }

                switch (key) {
                    case GLFW_KEY_ESCAPE:
                        glfwSetWindowShouldClose(windowHnd, true);
                        break;
                }
            });

            boolean useCallbacks = true;

            try (MemoryStack stack = stackPush()) {
                BGFXInit init = BGFXInit.malloc(stack);
                bgfx_init_ctor(init);
                init
                    .callback(useCallbacks ? createCallbacks(stack) : null)
                    .resolution(it -> it
                        .width(width)
                        .height(height)
                        .reset(BGFX_RESET_VSYNC));

                switch (Platform.get()) {
                    case FREEBSD:
                    case LINUX:

                        init.platformData()
                            .ndt(GLFWNativeX11.glfwGetX11Display())
                            .nwh(GLFWNativeX11.glfwGetX11Window(window));
                        break;

                        // if (glfwGetPlatform() == GLFW_PLATFORM_WAYLAND) {
                        //     App.logInfo("Using Wayland Window");
                        //     init.platformData()
                        //         .ndt(GLFWNativeWayland.glfwGetWaylandDisplay())
                        //         .nwh(GLFWNativeWayland.glfwGetWaylandWindow(window))
                        //         .type(BGFX_NATIVE_WINDOW_HANDLE_TYPE_WAYLAND);
                        // } else {
                        //     App.logInfo("Using X11 Window");
                        //     init.platformData()
                        //         .ndt(GLFWNativeX11.glfwGetX11Display())
                        //         .nwh(GLFWNativeX11.glfwGetX11Window(window));
                        // }
                        // break;
                    case MACOSX:
                        init.platformData()
                            .nwh(GLFWNativeCocoa.glfwGetCocoaWindow(window));
                        break;
                    case WINDOWS:
                        init.platformData()
                            .nwh(GLFWNativeWin32.glfwGetWin32Window(window));
                        break;
                }

                if (isVerbose()) {
                    System.out.println("bgfx init...");
                }

                if (!bgfx_init(init)) {
                    throw new RuntimeException("Error initializing bgfx renderer");
                }
            }

            if (isInfo()) {
                System.out.println("bgfx renderer: " + bgfx_get_renderer_name(bgfx_get_renderer_type()));
            }

            BGFXUtil._configure();

            sketch._setStartTimeMillis(System.currentTimeMillis());
            sketch.setup();

            while (!glfwWindowShouldClose(window)) {
                glfwPollEvents();

                // Set view 0 default viewport.
                bgfx_set_view_rect(0, 0, 0, width, height);

                // This dummy draw call is here to make sure that view 0 is cleared
                // if no other draw calls are submitted to view 0.
                bgfx_touch(0);

                sketch.draw();

                // Advance to next frame. Rendering thread will be kicked to
                // process submitted rendering primitives.
                bgfx_frame(false);
            }

            sketch.exit();

            bgfx_shutdown();

            BGFXUtil.dispose();

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            glfwTerminate();
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + clazz + " not found");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Class " + clazz + " could not be instantiated");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class " + clazz + " could not be accessed");
        }
    }

    // begin (partial) ref BGFXDemoUtil: https://github.com/LWJGL/lwjgl3-demos/blob/cd4a70daa3dad50c6c4a0d95e559d1bb7a349135/src/org/lwjgl/demo/bgfx/BGFXDemoUtil.java
    private static BGFXCallbackInterface createCallbacks(MemoryStack stack) {
        return BGFXCallbackInterface.calloc(stack)
            .vtbl(BGFXCallbackVtbl.calloc(stack)
                .fatal((_this, _filePath, _line, _code, _str) -> {
                    if (_code == BGFX_FATAL_DEBUG_CHECK) {
                        System.out.println("BREAK"); // set debugger breakpoint
                    } else {
                        throw new RuntimeException("Fatal error " + _code + ": " + memASCII(_str));
                    }
                })
                .trace_vargs((_this, _filePath, _line, _format, _argList) -> {
                    try (MemoryStack frame = MemoryStack.stackPush()) {
                        String filePath = (_filePath != NULL) ? memUTF8(_filePath) : "[n/a]";

                        ByteBuffer buffer = frame.malloc(128); // arbitary size to store formatted message
                        int length = LibCStdio.nvsnprintf(memAddress(buffer), buffer.remaining(), _format, _argList);

                        if (length > 0) {
                            String message = memASCII(buffer, length - 1); // bgfx log messages are terminated with the newline character
                            App.logDebug("bgfx: [" + filePath + " (" + _line + ")] - " + message);
                        } else {
                            App.logDebug("bgfx: [" + filePath + " (" + _line + ")] - error: unable to format output: " + memASCII(_format));
                        }
                    }
                })
                .profiler_begin((_this, _name, _abgr, _filePath, _line) -> {

                })
                .profiler_begin_literal((_this, _name, _abgr, _filePath, _line) -> {

                })
                .profiler_end(_this -> {

                })
                .cache_read_size((_this, _id) -> 0)
                .cache_read((_this, _id, _data, _size) -> false)
                .cache_write((_this, _id, _data, _size) -> {

                })
                .screen_shot((_this, _filePath, _width, _height, _pitch, _data, _size, _yflip) -> {

                })
                .capture_begin((_this, _width, _height, _pitch, _format, _yflip) -> {

                })
                .capture_end(_this -> {

                })
                .capture_frame((_this, _data, _size) -> {

                })
            );
    }
    // end ref BGFXDemoUtil:

    public static void main(String clazz, WindowInitSetting windowInitSetting) {
        main(clazz, windowInitSetting, new String[0]);
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            throw new IllegalArgumentException("No class name provided as first argument");
        }

        String clazz = args[0];
        WindowInitSetting windowInitSetting = new WindowInitSetting(1024, 480);
        String[] restArgs;

        if(args.length > 1) {
            restArgs = new String[args.length - 1];
            System.arraycopy(args, 1, restArgs, 0, args.length - 1);
        } else {
            restArgs = new String[0];
        }
        main(clazz, windowInitSetting, restArgs);
    }
}