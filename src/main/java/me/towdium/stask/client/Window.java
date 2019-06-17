package me.towdium.stask.client;

import me.towdium.stask.utils.Closeable;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.Tickable;
import me.towdium.stask.utils.time.Counter;
import me.towdium.stask.utils.time.Timer;
import org.joml.Vector2i;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@ParametersAreNonnullByDefault
public class Window extends Closeable implements Tickable {
    static int counter;
    static GLFWVidMode display;
    long id;
    boolean debug = false;
    Timer timer;
    Counter fps;
    Page root;
    Painter painter;
    Vector2i mouse;
    public static boolean pause = false;

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
            GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 8);
            display = Objects.requireNonNull(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()), "No display");
        }

        counter++;
        this.root = root;
        if (display == null) throw new IllegalStateException("No display found");
        id = GLFW.glfwCreateWindow(640, 360, title, NULL, NULL);
        if (id == NULL) throw new RuntimeException("Failed to create the GLFW window");
        timer = new Timer(1f / display.refreshRate(), i -> {
            if (i != 0 && debug) Log.client.trace("Dropping " + i + " frames");
            fps.tick();
            loop();
        });
        fps = new Counter(1f, i -> {
            if (debug) Log.client.trace("FPS: " + i);
        });
        GLFW.glfwMakeContextCurrent(id);
        GL.createCapabilities();
        GLFW.glfwSetMouseButtonCallback(id, (window, button, action, mods) -> {
            Vector2i m = mouse();
            this.root.onClick(m, button == GLFW.GLFW_MOUSE_BUTTON_1, action == GLFW.GLFW_PRESS);
            this.root.onMove(m);
        });
        GLFW.glfwSetWindowSizeCallback(id, (window, width, height) -> {
            this.root.onResize(width, height);
            loop();
        });
        GLFW.glfwSetKeyCallback(id, (window, key, code, action, mods) -> {
            if (key == GLFW.GLFW_KEY_GRAVE_ACCENT) pause = action != GLFW.GLFW_RELEASE;
        });
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
        GL30C.glClearColor(22f / 255f, 22f / 255f, 22f / 255f, 0f);

        GLFW.glfwSwapInterval(0);
        painter = new Painter(this);
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    public int getFps() {
        return fps.stored();
    }

    public void display() {
        Vector2i size = getSize();
        GLFW.glfwSetWindowPos(id, (display.width() - size.x) / 2, (display.height() - size.y) / 2);
        GLFW.glfwShowWindow(id);
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

    private void update(Vector2i m) {
        if (!m.equals(mouse)) {
            mouse = m;
            root.onMove(m);
        }
    }

    private void loop() {
        GLFW.glfwMakeContextCurrent(id);
        GL30C.glClear(GL30C.GL_COLOR_BUFFER_BIT | GL30C.GL_STENCIL_BUFFER_BIT);
        Vector2i m = mouse();
        update(m);
        root.onRefresh();
        root.onDraw(painter, m);
        GLFW.glfwSwapBuffers(id);
    }

    public boolean isFinished() {
        return GLFW.glfwWindowShouldClose(id);
    }

    @Override
    public void tick() {
        if (closed) return;
        GLFW.glfwWaitEventsTimeout(1 / 1000f);
        timer.tick();
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

    public enum Mouse {
        LEFT, RIGHT, MOVE;

        static Mouse get(int i) {
            if (i == GLFW.GLFW_MOUSE_BUTTON_1) return LEFT;
            else if (i == GLFW.GLFW_MOUSE_BUTTON_2) return RIGHT;
            else return MOVE;
        }
    }
}
