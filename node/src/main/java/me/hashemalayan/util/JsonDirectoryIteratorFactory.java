package me.hashemalayan.util;

import java.nio.file.Path;

public interface JsonDirectoryIteratorFactory {
    JsonDirectoryIterator create(Path startPath);
}
