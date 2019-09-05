package lt.iz.service;

import com.google.common.base.Strings;
import lt.iz.tracker.TimeTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDateTime.now;

public class WinServiceManager implements ServiceManager {

    private String eventstoreServiceName;
    private String mongoServiceName;

    private static final String SERVICE_STATUS_RUNNING = "SERVICE_RUNNING";
    private static final String SERVICE_STATUS_STOPPED = "SERVICE_STOPPED";

    private static final Integer SERVICE_TIMEOUT_IN_SECONDS = 10;

    public WinServiceManager(String eventstoreServiceName, String mongoServiceName) {
        if (Strings.isNullOrEmpty(eventstoreServiceName) || Strings.isNullOrEmpty(mongoServiceName))
            throw new IllegalArgumentException("Invalid database windows service name.");
        this.eventstoreServiceName = eventstoreServiceName;
        this.mongoServiceName = mongoServiceName;
    }

    @Override
    public void startEventStoreService() throws InterruptedException, IOException {
        TimeTracker timeTracker = TimeTracker.start();
        startService(eventstoreServiceName);
        log("EventStore service " + eventstoreServiceName + " started.", timeTracker);
    }

    @Override
    public void stopEventStoreService() throws InterruptedException, IOException {
        TimeTracker timeTracker = TimeTracker.start();
        stopService(eventstoreServiceName);
        log("EventStore service " + eventstoreServiceName + " stopped.", timeTracker);
    }

    @Override
    public void startMongoService() throws InterruptedException, IOException {
        TimeTracker timeTracker = TimeTracker.start();
        startService(mongoServiceName);
        log("Mongo service " + mongoServiceName + " started.", timeTracker);
    }

    @Override
    public void stopMongoService() throws InterruptedException, IOException {
        TimeTracker timeTracker = TimeTracker.start();
        stopService(mongoServiceName);
        log("Mongo service " + mongoServiceName + " stopped.", timeTracker);
    }

    private Boolean isServiceRunning(String serviceName) throws IOException {
        return SERVICE_STATUS_RUNNING.equals(getServiceStatus(serviceName));
    }

    private static Boolean isServiceStopped(String serviceName) throws IOException {
        return SERVICE_STATUS_STOPPED.equals(getServiceStatus(serviceName));
    }

    private void stopService(String serviceName) throws InterruptedException, IOException {
        if (!isServiceRunning(serviceName)) return;

        invoke("nssm stop " + serviceName);
        LocalDateTime timeout = getServiceTimeout();

        while (!isServiceStopped(serviceName) && now().isBefore(timeout)) {
            TimeUnit.SECONDS.sleep(1);
        }

        if (!isServiceStopped(serviceName))
            throw new RuntimeException(String.format("Could not stop service %s for %s seconds. Current service status %s.",
                serviceName, SERVICE_TIMEOUT_IN_SECONDS, getServiceStatus(serviceName)));
    }

    private void startService(String serviceName) throws InterruptedException, IOException {
        if (!isServiceStopped(serviceName)) return;

        invoke("nssm start " + serviceName);
        LocalDateTime timeout = getServiceTimeout();

        while (!isServiceRunning(serviceName) && now().isBefore(timeout)) {
            TimeUnit.SECONDS.sleep(1);
        }

        if (!isServiceRunning(serviceName))
            throw new RuntimeException(String.format("Could not start service %s for %s seconds. Current service status %s.",
                serviceName, SERVICE_TIMEOUT_IN_SECONDS, getServiceStatus(serviceName)));
    }

    private static String getServiceStatus(String serviceName) throws IOException {
        return invoke("nssm status " + serviceName);
    }

    private static String invoke(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        String value = sb.toString();
        if (value.endsWith("\n")) value = value.substring(0, value.lastIndexOf("\n"));

        return value;
    }

    private static LocalDateTime getServiceTimeout() {
        return now().plusSeconds(SERVICE_TIMEOUT_IN_SECONDS);
    }

    private static void log(String message) {
        System.out.println("/SERVICE/ - " + message);
    }

    private static void log(String message, TimeTracker timeTracker) {
        log(message + " (" + timeTracker.finish() + " sec.)");
    }

}
