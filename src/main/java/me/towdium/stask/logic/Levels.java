package me.towdium.stask.logic;

import com.google.gson.Gson;
import me.towdium.stask.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
public class Levels {
    public static List<Section> sections;

    static {
        sections = new ArrayList<>();
        Gson gson = new Gson();
        Pojo.Levels l = gson.fromJson(Utilities.readString("/general/levels.json"), Pojo.Levels.class);
        for (Pojo.Section s : l.sections) {
            Section tmp = new Section();
            tmp.levels.addAll(s.levels);
            tmp.desc = s.desc;
            sections.add(tmp);
        }
    }

    public static class Section {
        public List<String> levels = new ArrayList<>();
        public String desc;
    }
}
