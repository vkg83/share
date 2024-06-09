package com.vkg.finance.share.stock.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtil {

    public static <T> T loadFromFile(Path path, Class<T> cls) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(path.toFile(), cls);
    }

    public static String loadFromFile(Path path) throws IOException {
        return Files.readString(path);
    }


    public static void saveToFile(Path path, Object content) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        saveToFile(path, mapper.writeValueAsString(content));
    }

    public static void saveToFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    public static void clean(Path path) throws IOException {
        if(Files.notExists(path)) return;

        Files.walkFileTree(path, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }

    private static void rename(Path path) {

        final File[] files = path.toFile().listFiles((f, name) -> name.contains("from01042024to08062024.txt"));
        assert files != null;
        for (File file : files) {
            String newName = file.getName();
            newName = newName.replace('8', '9');
            var f = file.renameTo(new File(file.getParent(), newName));
            if(f)
                System.out.println("Renamed file " + file);
        }
    }

    public static void main(String[] args) {
        Path p = Paths.get("C:\\Users\\Vishnu Kant Gupta\\Documents\\nse_data\\cache\\2024-06-09");
        rename(p);
    }

}
