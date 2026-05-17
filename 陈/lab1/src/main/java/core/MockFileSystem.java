package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模拟文件系统实现类（TDD 测试桩）。
 * <p>
 * 内部维护一个内存 Map 来模拟磁盘文件系统，
 * 所有的读写、追加操作均只针对内存进行，
 * 绝对不调用真实磁盘 API，确保测试无副作用。
 * </p>
 */
public class MockFileSystem implements IFileSystem {

    /**
     * 内存磁盘映射表，key 为文件路径，value 为文件内容行列表。
     */
    private Map<String, List<String>> memoryDisk = new HashMap<>();

    @Override
    public List<String> readLines(String path) {
        if (!memoryDisk.containsKey(path)) {
            throw new EditorException("文件不存在: " + path);
        }
        return new ArrayList<>(memoryDisk.get(path));
    }

    @Override
    public void writeLines(String path, List<String> lines) {
        memoryDisk.put(path, new ArrayList<>(lines));
    }

    @Override
    public void appendLine(String path, String line) {
        memoryDisk.computeIfAbsent(path, k -> new ArrayList<>()).add(line);
    }

    @Override
    public boolean exists(String path) {
        return memoryDisk.containsKey(path);
    }

    @Override
    public List<String> listDirectory(String path) {
        String normalizedPath = path.endsWith("/") ? path : path + "/";
        return memoryDisk.keySet().stream()
                .filter(key -> key.startsWith(normalizedPath))
                .map(key -> key.substring(normalizedPath.length()))
                .map(key -> key.contains("/") ? key.substring(0, key.indexOf("/")) : key)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取内存磁盘映射表（仅供测试使用）。
     *
     * @return 内存磁盘映射表
     */
    public Map<String, List<String>> getMemoryDisk() {
        return memoryDisk;
    }
}
