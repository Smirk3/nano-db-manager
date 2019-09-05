package lt.iz.file;

import java.io.IOException;

public interface BackupManager {

    void backup() throws IOException;

    void restoreLatest() throws IOException;

}
