package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.widgets.*;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: Towdium
 * Date: 29/07/2019
 */
@ParametersAreNonnullByDefault
public class PTest extends Page.Impl {
    PWrapper root;
    Page parent;
    Content content = new Content();
    WButton continew = new WButtonText(250, 40, "Continue");
    WButton back = new WButtonText(250, 40, "Back").setListener(w -> root.display(() -> parent));

    public PTest(PWrapper root, Page parent) {
        this.root = root;
        this.parent = parent;
        refresh();
    }

    @Override
    protected void onLayout(int x, int y) {
        put(content, (x - Content.WIDTH) / 2, (y - Content.HEIGHT) / 2 - 20);
        put(continew, x - 300, y - 80);
        put(back, x - 580, y - 80);
    }

    private void continew() {
        Cluster c = new Cluster(content.clusters.get(content.clusters.getSelect()));
        List<Graph> gs = content.graphs.stream().filter(i -> content.states.get(i))
                .map(Graph::new).collect(Collectors.toList());
        root.display(() -> new PGame(root, parent, new Game(c, gs), true));
    }

    private void refresh() {
        content.available.setStrs(content.graphs.stream()
                .filter(i -> !content.states.get(i))
                .collect(Collectors.toList()));
        content.selected.setStrs(content.graphs.stream()
                .filter(i -> content.states.get(i))
                .collect(Collectors.toList()));
        boolean c = content.states.values().stream().anyMatch(i -> i)
                && content.clusters.getSelect() != -1;
        continew.setListener(c ? w -> continew() : null);
    }

    class Content extends WContainer {
        public static final int HEIGHT = 450;
        public static final int WIDTH = 1200;

        WList clusters = new WList(Cluster.list(), 150, HEIGHT);
        WList selected = new WList(new ArrayList<>(), 150, HEIGHT, true);
        WList available = new WList(new ArrayList<>(), 150, HEIGHT, true);
        WContainer cluster = new WContainer();
        WContainer graph = new WContainer();
        List<String> graphs;
        Map<String, Boolean> states = new HashMap<>();

        public Content() {
            put(clusters, 200, 0);
            int y = (HEIGHT - WCluster.HEIGHT) / 2;
            put(new WCluster(null), 20, y);
            put(cluster, 20, y);
            put(graph, 430, 0);
            graphs = Graph.list();
            graphs.forEach(i -> states.put(i, false));
            put(available, 845, 0);
            put(selected, 1030, 0);
            put(new WDrag.Impl(150, HEIGHT) {
                @Override
                public void onReceived(Object o) {
                    super.onReceived(o);
                    states.put((String) o, false);
                    refresh();
                }

                @Override
                public boolean onAttempt(Object o, Vector2i mouse) {
                    return o instanceof String;
                }
            }, 845, 0);
            put(new WDrag.Impl(150, HEIGHT) {
                @Override
                public void onReceived(Object o) {
                    super.onReceived(o);
                    states.put((String) o, true);
                    refresh();
                }

                @Override
                public boolean onAttempt(Object o, Vector2i mouse) {
                    return o instanceof String;
                }
            }, 1030, 0);
            clusters.setListener((w, o, n) -> {
                cluster.clear();
                if (n == -1) return;
                Cluster c = new Cluster(w.get(n));
                Game g = new Game(c, new ArrayList<>());
                cluster.put(new WCluster(g), 0, 0);
            });
            available.setListener((w, o, n) -> {
                graph.clear();
                if (n == -1) return;
                Graph r = new Graph(w.get(n));
                Cluster c = new Cluster("1-1");
                List<Graph> gs = new ArrayList<>();
                gs.add(r);
                Game g = new Game(c, gs);
                WGraph a = new WGraph(HEIGHT, g, r) {
                    @Override
                    public boolean onClick(@Nullable Vector2i mouse, boolean left) {
                        return false;
                    }

                    @Override
                    public boolean onDrag(@Nullable Vector2i mouse, boolean left) {
                        return false;
                    }
                };
                graph.put(a, (385 - a.getWidth()) / 2, 0);
            });
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            p.drawRect(0, 0, 173, HEIGHT, 2);
            p.drawRect(430, 0, 385, HEIGHT, 2);
            p.drawText("Cluster:", 0, -20);
            p.drawText("Graphs:", 430, -20);
            p.drawText("Available:", 845, -20);
            p.drawText("Selected:", 1030, -20);
        }
    }
}
