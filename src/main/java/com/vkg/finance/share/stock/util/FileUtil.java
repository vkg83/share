package com.vkg.finance.share.stock.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

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

    public static void removeCurrent(Path path) throws IOException {
        Files.deleteIfExists(path.resolve("apietfGET.txt"));
        Files.deleteIfExists(path.resolve("apiequitystockIndicesGETindexNIFTY50.txt"));
    }

    private static int clean(Path path, String postFix) throws IOException {
        final File[] files = path.toFile().listFiles((f, name) -> name.endsWith(postFix));
        assert files != null;
        for (File file : files) {
            Files.delete(file.toPath());
        }
        removeCurrent(path);
        return files.length + 2;
    }

    public static int refresh(Path basePath) throws IOException {
        var base = basePath.toFile();
        final File[] files = base.listFiles((f, name) -> f.isDirectory());
        assert files != null;
        LocalDate date = null;
        for(File f : files) {
            try {
                date = LocalDate.parse(f.getName());
            } catch(Exception ignored) {

            }
        }
        if(date == null) {
            return 0;
        }

        final LocalDate today = LocalDate.now();
        if(date.equals(today)) {
            return 0;
        }

        LocalDate from = date.with(IsoFields.DAY_OF_QUARTER, 1);
        var fmt = DateTimeFormatter.ofPattern("ddMMyyyy");
        String toDate = String.format("from%sto%s.txt", from.format(fmt), date.format(fmt));

        Path oldPath = basePath.resolve(date.toString());
        int count = clean(oldPath, toDate);

        Files.move(oldPath, basePath.resolve(today.toString()));

        return count;
    }
}
