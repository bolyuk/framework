package bl0.bjs.common.base;

import java.util.List;

public interface IStorage {

    public String read(String local_path);

    public void write(String local_path, String content);

    public void delete(String local_path);

    public boolean exists(String local_path);

    public void dir(String local_path);

    public List<String> getFolders(String local_path);

    public List<String> getFiles(String local_path);

    public IStorage deepInFolder(String folder_in);
}
