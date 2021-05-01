package org.example.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public class CommonUtil {

    public static boolean isDirectoryEmpty(Path visitedFile) throws IOException {
        Stream<Path> walkResult = Files.walk(visitedFile, 1);
        Iterator<Path> iterator = walkResult.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().equals(visitedFile)) {
                return false;
            }
        }
        return true;
    }
}
