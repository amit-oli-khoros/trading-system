package org.amit.common;

public class Logger {
    public static void log(String message) {
        System.out.println(Thread.currentThread() + " " + message);
    }
}
