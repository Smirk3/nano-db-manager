package lt.iz;

import lt.iz.tracker.TimeTracker;

import java.time.format.DateTimeFormatter;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;

public class Logger {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        System.out.println(now().format(formatter) + " " + message);
    }

    public static void log(String message, TimeTracker timeTracker) {
        log(format("%s (%s sec.)", message, timeTracker.finish()));
    }

}
