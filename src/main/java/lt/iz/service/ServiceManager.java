package lt.iz.service;

import java.io.IOException;

public interface ServiceManager {

    void startEventStoreService() throws InterruptedException, IOException;

    void stopEventStoreService() throws InterruptedException, IOException;

    void startMongoService() throws InterruptedException, IOException;

    void stopMongoService() throws InterruptedException, IOException;

}
