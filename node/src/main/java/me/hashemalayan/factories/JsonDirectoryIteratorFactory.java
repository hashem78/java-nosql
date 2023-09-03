package me.hashemalayan.factories;

import me.hashemalayan.util.JsonDirectoryIterator;

import java.nio.file.Path;

public interface JsonDirectoryIteratorFactory {
    JsonDirectoryIterator create(Path startPath);
}
