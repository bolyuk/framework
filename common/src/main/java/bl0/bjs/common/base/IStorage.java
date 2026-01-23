package bl0.bjs.common.base;

import java.util.List;

public interface IStorage {

    String read(String local_path);

    void write(String local_path, String content);

    void delete(String local_path);

    boolean exists(String local_path);

    void dir(String local_path);

    List<String> getFolders(String local_path);

    List<String> getFiles(String local_path);

    IStorage deepInFolder(String folder_in);
}
