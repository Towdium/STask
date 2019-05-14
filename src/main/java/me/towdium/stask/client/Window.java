package me.towdium.stask.client;

import me.towdium.stask.utils.Closeable;
import me.towdium.stask.utils.Tickable;
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
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class Window extends Closeable implements Tickable {
    static int counter;
    static GLFWVidMode display;
    long id;
    Page root;
    Painter painter;

    public Window(String title, Page root) {
        this(title, root, true);
    }

    public Window(String title, Page root, boolean resizable) {
        if (counter == 0) {
            GLFWErrorCallback.createPrint().set();
            if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
            display = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        }

        counter++;
        this.root = root;
        if (display == null) throw new IllegalStateException("No display found");
        id = GLFW.glfwCreateWindow(640, 360, title, NULL, NULL);
        if (id == NULL) throw new RuntimeException("Failed to create the GLFW window");
        GLFW.glfwMakeContextCurrent(id);
        GL.createCapabilities();
        GLFW.glfwSetMouseButtonCallback(id, (window, button, action, mods) ->
                this.root.onMouse(mouse(), button, action == GLFW.GLFW_PRESS));
        GLFW.glfwSetWindowSizeCallback(id, (window, width, height) ->
                this.root.onResize(width, height));
        GLFW.glfwSetFramebufferSizeCallback(id, (window, width, height) -> {
            GLFW.glfwMakeContextCurrent(id);
            GL30C.glViewport(0, 0, width, height);
            painter.onResize(width, height);
        });

        GL30C.glEnable(GL30C.GL_STENCIL_TEST);
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

        GLFW.glfwSwapInterval(0);
        painter = new Painter(this);
    }

    void bind() {
        GLFW.glfwMakeContextCurrent(id);
    }

    public void display() {
        Vector2i size = getSize();
        GLFW.glfwShowWindow(id);
        GLFW.glfwSetWindowPos(id, (display.width() - size.x) / 2, (display.height() - size.y) / 2);
        GLFW.glfwMakeContextCurrent(id);
        root.onResize(size.x, size.y);
        painter.onResize(size.x, size.y);
    }

    public Vector2i getSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(id, x, y);
            return new Vector2i(x.get(0), y.get(0));
        }
    }

    public boolean isFinished() {
        return GLFW.glfwWindowShouldClose(id);
    }

    @Override
    public void tick() {
        if (closed) return;
        GLFW.glfwMakeContextCurrent(id);
        GLFW.glfwPollEvents();
        GL30C.glClear(GL30C.GL_COLOR_BUFFER_BIT | GL30C.GL_STENCIL_BUFFER_BIT);
        root.onDraw(painter, mouse());
        GLFW.glfwSwapBuffers(id);
    }

    Vector2i mouse() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer x = stack.mallocDouble(1);
            DoubleBuffer y = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(id, x, y);
            return new Vector2i((int) x.get(0), (int) y.get(0));
        }
    }

    @Override
    public void close() {
        super.close();
        Callbacks.glfwFreeCallbacks(id);
        GLFW.glfwDestroyWindow(id);
        counter--;
        if (counter == 0) {
            GL.setCapabilities(null);
            GLFW.glfwTerminate();
            GLFWErrorCallback ec = GLFW.glfwSetErrorCallback(null);
            if (ec != null) ec.free();
        }
    }
}
