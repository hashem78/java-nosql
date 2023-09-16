package me.hashemalayan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class JsonDirectoryIterator implements Iterator<JsonIteratorResult>, Iterable<JsonIteratorResult> {
    private final Path startPath;
    private final Queue<Path> fileQueue = new LinkedList<>();
    private final ObjectMapper mapper;

    @Inject
    public JsonDirectoryIterator(ObjectMapper mapper, @Assisted Path path) throws IOException {
        this.startPath = path;
        this.mapper = mapper;
        init();
    }

    private void init() throws IOException {

        if(!Files.exists(startPath))
            Files.createDirectories(startPath);

        try (var directoryStream = Files.newDirectoryStream(startPath)) {
            for (var directory : directoryStream) {
                fileQueue.offer(directory);
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !fileQueue.isEmpty();
    }

    @Override
    public JsonIteratorResult next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        var path = fileQueue.poll();
        assert path != null;

        try {
            return new JsonIteratorResult(
                    path.getFileName().toString(),
                    mapper.readTree(Files.readAllBytes(path))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<JsonIteratorResult> iterator() {
        return this;
    }
}