package sketch;

import java.util.Objects;

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
import org.lwjgl.system.Platform;

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

            try (MemoryStack stack = stackPush()) {
                BGFXInit init = BGFXInit.malloc(stack);
                bgfx_init_ctor(init);
                init
                    .resolution(it -> it
                        .width(width)
                        .height(height)
                        .reset(BGFX_RESET_VSYNC));

                switch (Platform.get()) {
                    case FREEBSD:
                    case LINUX:
                        if (glfwGetPlatform() == GLFW_PLATFORM_WAYLAND) {
                            init.platformData()
                                .ndt(GLFWNativeWayland.glfwGetWaylandDisplay())
                                .nwh(GLFWNativeWayland.glfwGetWaylandWindow(window))
                                .type(BGFX_NATIVE_WINDOW_HANDLE_TYPE_WAYLAND);
                        } else {
                            init.platformData()
                                .ndt(GLFWNativeX11.glfwGetX11Display())
                                .nwh(GLFWNativeX11.glfwGetX11Window(window));
                        }
                        break;
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

            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            glfwTerminate();
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();

            while (true) {
                sketch.draw();
            }

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + clazz + " not found");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Class " + clazz + " could not be instantiated");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class " + clazz + " could not be accessed");
        }
    }

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