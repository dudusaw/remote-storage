package org.example.client.service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface ClientFileManagerService {
    void sendFilesToActivePath(List<File> files, Path currentPath);
}
