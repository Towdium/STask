package me.towdium.stask.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class Window {
    static long id;

    public static void run(IWidget root, Runnable update) {
        try {
            init();
            while (!glfwWindowShouldClose(id)) {
                glfwPollEvents();
                glClear(GL_COLOR_BUFFER_BIT);
                update.run();
                root.onDraw();
                glfwSwapBuffers(id);
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
        glfwFreeCallbacks(id);
        glfwDestroyWindow(id);
        glfwTerminate();
        GLFWErrorCallback ec = glfwSetErrorCallback(null);
        if (ec != null) ec.free();
    }

    static void init() {
        GLFWErrorCallback.createPrint().set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vm == null) throw new IllegalStateException("No display found");

        int w = vm.width() / 2;
        int h = vm.height() / 2;

        id = glfwCreateWindow(w, h, "STask", NULL, NULL);
        if (id == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(id);
        glfwSetWindowPos(id, (vm.width() - w) / 2, (vm.height() - h) / 2);
        GL.createCapabilities();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, w, h, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(43f / 255f, 43f / 255f, 43f / 255f, 0f);

        glfwSwapInterval(1);
        glfwShowWindow(id);
    }
}
