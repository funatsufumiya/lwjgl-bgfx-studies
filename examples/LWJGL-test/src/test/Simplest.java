/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package test;

import java.util.Objects;

import static org.lwjgl.bgfx.BGFX.*;
import org.lwjgl.bgfx.BGFXInit;
import org.lwjgl.bgfx.BGFXPlatform;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
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

public final class Simplest {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    private Simplest() { }

    public static void main(String[] args) {
        int width  = 1024;
        int height = 480;

        System.setProperty("org.lwjgl.util.Debug","true");

        System.out.println("setting glfw_async");

        if (Platform.get() == Platform.MACOSX) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
        }

        System.out.println("glfw init");

        GLFWErrorCallback.createThrow().set();
        if (!glfwInit()) {
            throw new RuntimeException("Error initializing GLFW");
        }

        // the client (renderer) API is managed by bgfx
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        if (glfwGetPlatform() == GLFW_PLATFORM_COCOA) {
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        }
        
        System.out.println("glfw create window");

        long window = glfwCreateWindow(width, height, "Test", 0, 0);
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

        // Call bgfx::renderFrame before bgfx::init to signal to bgfx not to create a render thread.
	    // Most graphics APIs must be used on the same thread that created the window.
        BGFXPlatform.bgfx_render_frame(-1);

        try (MemoryStack stack = stackPush()) {
            BGFXInit init = BGFXInit.malloc(stack);
            bgfx_init_ctor(init);
            init
                .resolution(it -> it
                    .width(width)
                    .height(height)
                    .reset(BGFX_RESET_VSYNC));

            switch (Platform.get()) {
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

            System.out.println("bgfx init...");

            if (!bgfx_init(init)) {
                throw new RuntimeException("Error initializing bgfx renderer");
            }
        }

        System.out.println("bgfx renderer: " + bgfx_get_renderer_name(bgfx_get_renderer_type()));

        // Enable debug text.
        bgfx_set_debug(BGFX_DEBUG_TEXT);

        bgfx_set_view_clear(0, BGFX_CLEAR_COLOR | BGFX_CLEAR_DEPTH, 0x303030ff, 1.0f, 0);

        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();

            // Set view 0 default viewport.
            bgfx_set_view_rect(0, 0, 0, width, height);

            // This dummy draw call is here to make sure that view 0 is cleared
            // if no other draw calls are submitted to view 0.
            bgfx_touch(0);

            // Use debug font to print information about this example.
            bgfx_dbg_text_clear(0, false);
            bgfx_dbg_text_printf(0, 0, 0x0f, "Simplest");

            // Advance to next frame. Rendering thread will be kicked to
            // process submitted rendering primitives.
            bgfx_frame(false);
        }

        bgfx_shutdown();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

}