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

    public static void delete(Path path) throws IOException {
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

    public static void removeCurrent() {
        File path = new File("C:\\Users\\Vishnu Kant Gupta\\Documents\\nse_data\\cache\\"+LocalDate.now());
        if(new File(path, "apietfGET.txt").delete()) System.out.println("deleted apietfGET.txt");
        if(new File(path, "apiequitystockIndicesGETindexNIFTY50.txt").delete()) System.out.println("deleted apiequitystockIndicesGETindexNIFTY50.txt");
    }

    private static void clean(File path, String postFix) {

        final File[] files = path.listFiles((f, name) -> name.endsWith(postFix));
        assert files != null;
        for (File file : files) {
            var f = file.delete();
            if(f)
                System.out.println("deleted file " + file);
        }
        if(new File(path, "apietfGET.txt").delete()) System.out.println("deleted apietfGET.txt");
        if(new File(path, "apiequitystockIndicesGETindexNIFTY50.txt").delete()) System.out.println("deleted apiequitystockIndicesGETindexNIFTY50.txt");
    }

    public static void main(String[] args) {
        String date = "2024-06-17";
        String toDate = "from01042024to17062024.txt";
        File p = new File("C:\\Users\\Vishnu Kant Gupta\\Documents\\nse_data\\cache\\"+date);
        clean(p, toDate);
        var flag = p.renameTo(new File(p.getParent(), LocalDate.now().toString()));
        if(flag) {
            System.out.println("Renamed folder " + date + " to " + LocalDate.now());
        }
    }

}
