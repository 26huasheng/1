package core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 本地文件系统实现类。
 * <p>
 * 使用 java.nio.file.Files 实现真实的磁盘文件读写操作，
 * 统一使用 UTF-8 编码。所有 IOException 均被捕获后
 * 包装为 EditorException 抛出。
 * </p>
 */
public class LocalFileSystem implements IFileSystem {

    @Override
    public List<String> readLines(String path) {
        try {
            Path filePath = Paths.get(path);
            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EditorException("文件操作失败: " + e.getMessage());
        }
    }

    @Override
    public void writeLines(String path, List<String> lines) {
        try {
            Path filePath = Paths.get(path);
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EditorException("文件操作失败: " + e.getMessage());
        }
    }

    @Override
    public void appendLine(String path, String line) {
        try {
            Path filePath = Paths.get(path);
            Files.write(filePath, (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new EditorException("文件操作失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            Path filePath = Paths.get(path);
            return Files.exists(filePath);
        } catch (Exception e) {
            throw new EditorException("文件操作失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> listDirectory(String path) {
        try {
            Path dirPath = Paths.get(path);
            try (Stream<Path> stream = Files.list(dirPath)) {
                return stream.map(Path::getFileName)
                        .map(Path::toString)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new EditorException("文件操作失败: " + e.getMessage());
        }
    }
}
