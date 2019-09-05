package lt.iz.tracker;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;

public class TimeTracker {

    private LocalDateTime start;

    private TimeTracker(LocalDateTime start) {
        this.start = start;
    }

    public static TimeTracker start() {
        return new TimeTracker(now());
    }

    public long finish() {
        return start.until(now(), ChronoUnit.SECONDS);
    }
}
