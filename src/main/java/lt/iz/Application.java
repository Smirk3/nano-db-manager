package lt.iz;

import com.google.common.base.Strings;
import lt.iz.file.BackupManager;
import lt.iz.file.WinBackupManager;
import lt.iz.service.ServiceManager;
import lt.iz.service.WinServiceManager;
import lt.iz.tracker.TimeTracker;

import java.io.File;
import java.io.IOException;

import static lt.iz.Logger.log;

public class Application {

    /*private static Params params = new Params(Action.BACKUP, new File("c:\\development\\backup-nano-db"),
        new Database("nano-es", new File("c:\\development\\es")),
        new Database("nano-mongo", new File("c:\\development\\mongo"))
    );*/

    public static void main(String[] args) {

        Params params = parseArguments(args);
        try {
            ServiceManager serviceManager = new WinServiceManager(params.eventStore.serviceName, params.mongo.serviceName);
            BackupManager backupManager = new WinBackupManager(params.backupDir, params.eventStore.directory, params.mongo.directory);

            TimeTracker processTimeTracker = TimeTracker.start();

            serviceManager.stopMongoService();
            serviceManager.stopEventStoreService();


            if (Action.BACKUP.equals(params.action)) {
                backupManager.backup();
            } else if (Action.RESTORE.equals(params.action)) {
                backupManager.restoreLatest();
            } else {
                throw new RuntimeException("Unsupported action: " + params.action);
            }

            serviceManager.startEventStoreService();
            serviceManager.startMongoService();

            log("*** SUCCESS ***", processTimeTracker);

        } catch (IOException | InterruptedException e) {
            log("*** FAILURE: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static Params parseArguments(String[] args) {
        if (args.length != 6)
            throw new IllegalArgumentException("Illegal input arguments. 6 arguments must be passed.");
        Action action = Action.valueOf(args[0].toUpperCase());

        if (Strings.isNullOrEmpty(args[1]))
            throw new IllegalArgumentException("Illegal 2 argument: eventStore service name not specified.");
        if (Strings.isNullOrEmpty(args[3]))
            throw new IllegalArgumentException("Illegal 4 argument: eventStore database directory not specified.");
        if (!(new File(args[3]).exists()))
            throw new IllegalArgumentException("Illegal 4 argument: eventStore database directory does not exists.");
        Database eventStore = new Database(args[1], new File(args[3]));

        if (Strings.isNullOrEmpty(args[2]))
            throw new IllegalArgumentException("Illegal 3 argument: mongo service name not specified.");
        if (Strings.isNullOrEmpty(args[4]))
            throw new IllegalArgumentException("Illegal 5 argument: mongo database directory not specified.");
        if (!(new File(args[4]).exists()))
            throw new IllegalArgumentException("Illegal 5 argument: mongo database directory does not exists.");
        Database mongo = new Database(args[2], new File(args[4]));

        if (Strings.isNullOrEmpty(args[5]))
            throw new IllegalArgumentException("Illegal 6 argument: backup directory not specified.");
        if (!(new File(args[5]).exists()))
            throw new IllegalArgumentException("Illegal 6 argument: backup store directory does not exists.");

        return new Params(action, new File(args[5]), eventStore, mongo);
    }

}
