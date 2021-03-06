package lt.iz.file;

import lt.iz.tracker.TimeTracker;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static lt.iz.Logger.log;

public class WinBackupManager implements BackupManager {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final String BACKUP_DIR_NAME_PREFIX = "nano-db-";

    private File backupDir;
    private File eventStoreDatabaseDir;
    private File mongoDatabaseDir;
    private File workingDir;

    public WinBackupManager(File backupDir, File eventStoreDatabaseDir, File mongoDatabaseDir) {
        this.backupDir = backupDir;
        this.eventStoreDatabaseDir = eventStoreDatabaseDir;
        this.mongoDatabaseDir = mongoDatabaseDir;

    }

    @Override
    public void backup() throws IOException {
        if (!backupDir.exists()) backupDir.mkdir();
        workingDir = resolveCurrentBackupDirPath(backupDir);

        File eventStoreDatabaseBackupDir = resolveDatabaseBackupDirPath(eventStoreDatabaseDir);
        TimeTracker esTimeTracker = TimeTracker.start();
        copyFolder(eventStoreDatabaseDir, eventStoreDatabaseBackupDir, eventStoreDatabaseDir + File.separator + "log");
        logFile("EventStore database directory " + eventStoreDatabaseDir + " copied to " + eventStoreDatabaseBackupDir, esTimeTracker);

        File mongoDatabaseBackupDir = resolveDatabaseBackupDirPath(mongoDatabaseDir);
        TimeTracker mongoTimeTracker = TimeTracker.start();
        copyFolder(mongoDatabaseDir, mongoDatabaseBackupDir);
        logFile("Mongo database directory " + mongoDatabaseDir + " copied to " + mongoDatabaseBackupDir, mongoTimeTracker);

        TimeTracker compressTimeTracker = TimeTracker.start();
        Archiver.compress(workingDir);
        logFile("Working directory " + workingDir + " compressed.", compressTimeTracker);

        TimeTracker deleteTimeTracker = TimeTracker.start();
        delete(workingDir);
        logFile("Working directory " + workingDir + " deleted.", deleteTimeTracker);
    }

    @Override
    public void restoreLatest() throws IOException {
        TimeTracker findTimeTracker = TimeTracker.start();
        File archive = resolveLatestBackupArchive();
        logFile("Found latest buckup archive: " + archive, findTimeTracker);

        if (archive == null) throw new RuntimeException("Backup archive not found.");

        workingDir = new File(archive.toString().substring(0, archive.toString().indexOf(".")));
        logFile("Working directory " + workingDir);
        if (workingDir.exists()) delete(workingDir);

        TimeTracker timeTracker = TimeTracker.start();
        Archiver.extract(archive, backupDir);
        logFile("Backup archive " + archive + " extracted.", timeTracker);

        delete(mongoDatabaseDir);
        logFile("Mongo database directory " + mongoDatabaseDir + " deleted.");
        delete(eventStoreDatabaseDir);
        logFile("EventStore database directory " + eventStoreDatabaseDir + " deleted.");

        mongoDatabaseDir.mkdir();
        eventStoreDatabaseDir.mkdir();

        File eventStoreDatabaseBackupDir = resolveDatabaseBackupDirPath(eventStoreDatabaseDir);
        timeTracker = TimeTracker.start();
        FileUtils.copyDirectory(eventStoreDatabaseBackupDir, eventStoreDatabaseDir);
        logFile("EventStore database backup directory " + eventStoreDatabaseBackupDir + " restored to " + eventStoreDatabaseDir, timeTracker);

        File mongoDatabaseBackupDir = resolveDatabaseBackupDirPath(mongoDatabaseDir);
        timeTracker = TimeTracker.start();
        FileUtils.copyDirectory(mongoDatabaseBackupDir, mongoDatabaseDir);
        logFile("Mongo database backup directory " + mongoDatabaseBackupDir + " restored to " + mongoDatabaseDir, timeTracker);

        timeTracker = TimeTracker.start();
        delete(workingDir);
        logFile("Working directory " + workingDir + " deleted.", timeTracker);
    }

    private File resolveLatestBackupArchive() {
        List<File> files = Arrays.asList(backupDir.listFiles()).stream()
            .filter(f -> f.toString().endsWith(".zip"))
            .sorted(Comparator.comparing(f -> f.getName())).collect(Collectors.toList());
        if (files.size() == 0) return null;
        return files.get(files.size() - 1);
    }

    private File resolveDatabaseBackupDirPath(File value) {
        String databaseDirName = getDeepestDirName(value);
        return new File(workingDir + File.separator + databaseDirName);
    }

    private static String getDeepestDirName(File value) {
        String path = value.toString();
        return path.substring(path.lastIndexOf(File.separator));
    }

    private static File resolveCurrentBackupDirPath(File backupDir) {
        File dir = new File(backupDir + File.separator + BACKUP_DIR_NAME_PREFIX + now().format(DATE_FORMATTER));
        dir.mkdir();
        logFile("Working directory " + dir + " created.");
        return dir;
    }

    private static void copyFolder(File src, File dest) throws IOException {
        copyFolder(src, dest, null);
    }

    private static void copyFolder(File src, File dest, String skipDirectory) throws IOException {

        if (src.isDirectory()) {
            if (skipDirectory != null && src.toString().startsWith(skipDirectory)) return;

            if (!dest.exists()) {
                dest.mkdir();
                //System.out.println("Directory copied from " + src + "  to " + dest);
            }

            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);

                copyFolder(srcFile, destFile, skipDirectory);
            }

        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
            //System.out.println("File copied from " + src + " to " + dest);
        }
    }

    private static void delete(File file) {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                file.delete();
                //System.out.println("Directory is deleted : " + file.getAbsolutePath());
            } else {
                String files[] = file.list();
                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }

                if (file.list().length == 0) {
                    file.delete();
                    //System.out.println("Directory is deleted : " + file.getAbsolutePath());
                }
            }

        } else {
            file.delete();
            //System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

    private static void logFile(String message) {
        log("/ FILE  / - " + message);
    }

    private static void logFile(String message, TimeTracker timeTracker) {
        log("/ FILE  / - " + message, timeTracker);
    }
}
