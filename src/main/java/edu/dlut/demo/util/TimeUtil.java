package edu.dlut.demo.util;

public class TimeUtil {
    private TimeUtil() {
    }

    public static Long getGmtCreate() {
        return System.currentTimeMillis() / 1000;
    }
}
