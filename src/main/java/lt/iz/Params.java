package lt.iz;

import java.io.File;

public class Params {

    public Action action;

    public File backupDir;

    public Database eventStore;

    public Database mongo;

    public Params(Action action, File backupDir, Database eventStore, Database mongo) {
        this.action = action;
        this.backupDir = backupDir;
        this.eventStore = eventStore;
        this.mongo = mongo;
    }
}
