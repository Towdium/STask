package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.widgets.WCluster;
import me.towdium.stask.client.widgets.WContainer;
import me.towdium.stask.client.widgets.WList;
import me.towdium.stask.logic.Cluster;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 29/07/2019
 */
@ParametersAreNonnullByDefault
public class PTest extends Page.Impl {


    Content content = new Content();

    @Override
    protected void onLayout(int x, int y) {
        put(content, (x - Content.WIDTH) / 2, (y - Content.HEIGHT) / 2);
    }

    static class Content extends WContainer {
        public static final int HEIGHT = 450;
        public static final int WIDTH = 1200;

        WList clusters = new WList(Cluster.list(), 150, HEIGHT);
        WContainer cluster = new WContainer();

        public Content() {
            put(clusters, 200, 0);
            int c = (HEIGHT - WCluster.HEIGHT) / 2;
            put(cluster, 20, c);
            put(new WCluster(null), 20, c);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            p.drawRect(0, 0, 173, HEIGHT, 2);
        }
    }
}
