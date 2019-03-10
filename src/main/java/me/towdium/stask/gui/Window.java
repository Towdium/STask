package me.towdium.stask.gui;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class Window {
    static long id;
    static IWidget root;
    public static int windowHeight, windowWidth;

    public static void run(IWidget root, Runnable update) {
        Window.root = root;
        try {
            init();
            while (!GLFW.glfwWindowShouldClose(id)) {
                GLFW.glfwPollEvents();
                GL30C.glStencilMask(0xFF);
                GL30C.glClear(GL30C.GL_COLOR_BUFFER_BIT | GL30C.GL_STENCIL_BUFFER_BIT);
                GL30C.glStencilMask(0);
                update.run();
                root.onDraw(mouse());
                GLFW.glfwSwapBuffers(id);
            }
        } finally {
            try {
                destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static Vector2i mouse() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer x = stack.mallocDouble(1);
            DoubleBuffer y = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(id, x, y);
            return new Vector2i((int) x.get(0), (int) y.get(0));
        }
    }

    static void destroy() {
        GL.setCapabilities(null);
        Callbacks.glfwFreeCallbacks(id);
        GLFW.glfwDestroyWindow(id);
        GLFW.glfwTerminate();
        GLFWErrorCallback ec = GLFW.glfwSetErrorCallback(null);
        if (ec != null) ec.free();
    }

    static void init() {
        GLFWErrorCallback.createPrint().set();
        if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
        GLFWVidMode vm = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (vm == null) throw new IllegalStateException("No display found");
        windowWidth = vm.width() / 2;
        windowHeight = vm.height() / 2;

        id = GLFW.glfwCreateWindow(windowWidth, windowHeight, "STask", NULL, NULL);
        if (id == NULL) throw new RuntimeException("Failed to create the GLFW window");

        GLFW.glfwMakeContextCurrent(id);
        GLFW.glfwSetWindowPos(id, (vm.width() - windowWidth) / 2, (vm.height() - windowHeight) / 2);
        GL.createCapabilities();
        GLFW.glfwSetMouseButtonCallback(id, (window, button, action, mods) ->
                root.onMouse(mouse(), button, action == GLFW.GLFW_PRESS));

        GL30C.glEnable(GL30C.GL_STENCIL_TEST);
        GL30C.glStencilFunc(GL30C.GL_NOTEQUAL, 1, 0xFF);
        GL30C.glStencilOp(GL30C.GL_KEEP, GL30C.GL_REPLACE, GL30C.GL_REPLACE);
        GL30C.glEnable(GL30C.GL_MULTISAMPLE);
        GL30C.glEnable(GL30C.GL_CLIP_DISTANCE0);
        GL30C.glEnable(GL30C.GL_CLIP_DISTANCE1);
        GL30C.glEnable(GL30C.GL_CLIP_DISTANCE2);
        GL30C.glEnable(GL30C.GL_CLIP_DISTANCE3);
        GL30C.glEnable(GL30C.GL_CLIP_DISTANCE4);
        GL30C.glEnable(GL30C.GL_CLIP_DISTANCE5);

        GL30C.glEnable(GL30C.GL_TEXTURE_2D);
        GL30C.glEnable(GL30C.GL_BLEND);
        GL30C.glBlendFunc(GL30C.GL_SRC_ALPHA, GL30C.GL_ONE_MINUS_SRC_ALPHA);
        GL30C.glClearColor(43f / 255f, 43f / 255f, 43f / 255f, 0f);

        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(id);
    }
}
