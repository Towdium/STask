package me.towdium.stask.utils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
@ParametersAreNonnullByDefault
public class Log {
    static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static Log client = new Log("CLIENT");
    public static Log network = new Log("NETWORK");
    Priority level;
    String name;

    public Log(String name) {
        this(name, Priority.INFO);
    }

    public Log(String name, Priority level) {
        this.name = name;
        this.level = level;
    }

    public void setLevel(Priority level) {
        this.level = level;
    }

    public void trace(String s) {
        log(s, Priority.TRACE);
    }

    public void debug(String s) {
        log(s, Priority.DEBUG);
    }

    public void info(String s) {
        log(s, Priority.INFO);
    }

    public void warn(String s) {
        log(s, Priority.WARN);
    }

    public void error(String s) {
        log(s, Priority.ERROR);
    }

    public void log(String s, Priority p) {
        if (p.ordinal() < level.ordinal()) return;
        StringBuilder sb = new StringBuilder();
        String date = FORMAT.format(new Date());
        sb.append('[').append(name).append("][").append(p).append("][").append(date).append("][")
                .append(Thread.currentThread().getName()).append("] ").append(s);
        if (p.ordinal() > 2) System.err.println(sb.toString());
        else System.out.println(sb.toString());
    }

    public enum Priority {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
