package bl0.bjs.framework.files;

import bl0.bjs.common.base.IStorage;
import bl0.bjs.framework.utils.FilesUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LocalStorage implements IStorage {
    private final Path homePath;

    public LocalStorage(String path) {
        homePath = Paths.get(path);
    }

    public Path getHomePath() {
        return homePath;
    }

    public String read(String local_path) {
        return FilesUtils.readFromFile(homePath.resolve(local_path));
    }

    public void write(String local_path, String content) {
        FilesUtils.writeToFile(homePath.resolve(local_path), content);
    }

    public void delete(String local_path) {
        FilesUtils.delete(homePath.resolve(local_path));
    }

    public boolean exists(String local_path) {
        return FilesUtils.exists(homePath.resolve(local_path));
    }

    public void dir(String local_path) {
        FilesUtils.makeDir(homePath.resolve(local_path));
    }

    public List<String> getFolders(String local_path) {
        Path targetPath = homePath.resolve(local_path).normalize();

        List<String> folders = FilesUtils.getEntities(targetPath, FilesUtils.Type.Folder);
        return folders.stream()
                .map(folder -> homePath.relativize(Paths.get(folder)).toString())
                .toList();
    }

    public List<String> getFiles(String local_path) {
        Path targetPath = homePath.resolve(local_path).normalize();

        List<String> files = FilesUtils.getEntities(targetPath, FilesUtils.Type.File);
        return files.stream()
                .map(file -> homePath.relativize(Paths.get(file)).toString())
                .toList();
    }

    @Override
    public IStorage deepInFolder(String folder_in) {
        String sanitizedFolder = folder_in.startsWith("/") || folder_in.startsWith("\\")
                ? folder_in.substring(1)
                : folder_in;

        return new LocalStorage(homePath.resolve(sanitizedFolder).toString());
    }
}
