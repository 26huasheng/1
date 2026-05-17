package com.editor.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 负责文件系统层面的读取与写入。
 */
public class FileManager {
    /**
     * 按 UTF-8 读取文本文件。
     *
     * @param path 文件路径
     * @return 文件内容
     * @throws IOException 读取失败时抛出
     */
    public String readFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * 按 UTF-8 写入文本文件，必要时自动创建父目录。
     *
     * @param path 文件路径
     * @param content 待写入内容
     * @throws IOException 写入失败时抛出
     */
    public void writeFile(Path path, String content) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
    }
}
