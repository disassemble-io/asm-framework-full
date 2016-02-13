package me.sedlar.asm.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Tyler Sedlar
 * @since 2/12/2016
 */
public class EnvPath {

    /**
     * Gets all entries on Environment.PATH
     *
     * @return All entries on Environment.PATH
     */
    public static List<String> all() {
        List<String> pathList = new ArrayList<>();
        String envPath = System.getenv("PATH");
        String[] paths = envPath.split(File.pathSeparator);
        Collections.addAll(pathList, paths);
        return pathList;
    }

    /**
     * Finds the first entry on the path that matches the given predicate.
     *
     * @param predicate The predicate to match
     * @return The first entry on the path that matches the given predicate.
     */
    public static Optional<String> find(Predicate<String> predicate) {
        return all().stream().filter(predicate).findFirst();
    }
}
