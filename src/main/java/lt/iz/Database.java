package lt.iz;

import java.io.File;

public class Database {

    public String serviceName;

    public File directory;

    public Database(String serviceName, File directory) {
        this.serviceName = serviceName;
        this.directory = directory;
    }
}
