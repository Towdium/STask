package me.towdium.stask.render;

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
public abstract class Window {
    long id;

    public void run() {
        try {
            init();
            while (!glfwWindowShouldClose(id)) {
                glfwPollEvents();
                loop();
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

    void destroy() {
        GL.setCapabilities(null);
        glfwFreeCallbacks(id);
        glfwDestroyWindow(id);
        glfwTerminate();
        GLFWErrorCallback ec = glfwSetErrorCallback(null);
        if (ec != null) ec.free();
    }

    void init() {
        GLFWErrorCallback.createPrint().set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vm == null) throw new IllegalStateException("No display found");

        int w = vm.width() / 2;
        int h = vm.height() / 2;

        this.id = glfwCreateWindow(w, h, "STB HelloWorld Demo", NULL, NULL);
        if (id == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(id);
        glfwSetWindowPos(id, (vm.width() - w) / 2, (vm.height() - h) / 2);
        // glfwSetWindowRefreshCallback(window, window -> render());
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

    public abstract void loop();
}
