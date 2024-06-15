package com.vkg.finance.share.stock.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;

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

    private static void rename(File path) {

        final File[] files = path.listFiles((f, name) -> name.contains("from01042024to08062024.txt"));
        assert files != null;
        for (File file : files) {
            String newName = file.getName();
            newName = newName.replace('8', '9');
            var f = file.renameTo(new File(file.getParent(), newName));
            if(f)
                System.out.println("Renamed file " + file);
        }
    }

    private static void delete(File path, String postFix) {

        final File[] files = path.listFiles((f, name) -> name.endsWith(postFix));
        assert files != null;
        for (File file : files) {
            var f = file.delete();
            if(f)
                System.out.println("deleted file " + file);
        }
        if(new File(path, "apietfGET.txt").delete()) System.out.println("deleted apietfGET.txt");
    }

    public static void main(String[] args) {
        String date = "2024-06-13";
        String toDate = "from01042024to13062024.txt";
        File p = new File("C:\\Users\\Vishnu Kant Gupta\\Documents\\nse_data\\cache\\"+date);
        delete(p, toDate);
        var flag = p.renameTo(new File(p.getParent(), LocalDate.now().toString()));
        if(flag) {
            System.out.println("Renamed folder " + date + " to " + LocalDate.now());
        }
    }

}
