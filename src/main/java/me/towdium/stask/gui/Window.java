package me.towdium.stask.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class Window {
    static long id;
    public static int windowHeight, windowWidth;

    public static void run(IWidget root, Runnable update) {
        try {
            init();
            while (!GLFW.glfwWindowShouldClose(id)) {
                GLFW.glfwPollEvents();
                GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
                update.run();
                root.onDraw();
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

        GL30.glEnable(GL_MULTISAMPLE);
        GL30.glEnable(GL30.GL_TEXTURE_2D);
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        GL30.glClearColor(43f / 255f, 43f / 255f, 43f / 255f, 0f);

        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(id);
    }
}
