package core;

import java.util.List;

/**
 * 文件系统抽象接口。
 * <p>
 * 所有文件操作必须通过此接口进行依赖注入，
 * 禁止业务代码直接使用 java.io.File 或裸写 I/O，
 * 以满足 TDD 测试"无副作用"的铁律。
 * </p>
 */
public interface IFileSystem {

    /**
     * 从指定路径读取文件的所有行。
     *
     * @param path 文件路径
     * @return 文件内容的字符串列表，每行一个元素
     */
    List<String> readLines(String path);

    /**
     * 将字符串列表写入指定路径的文件（覆盖模式）。
     *
     * @param path  文件路径
     * @param lines 要写入的字符串列表
     */
    void writeLines(String path, List<String> lines);

    /**
     * 向指定路径的文件追加一行内容。
     *
     * @param path 文件路径
     * @param line 要追加的行内容
     */
    void appendLine(String path, String line);

    /**
     * 检查指定路径的文件或目录是否存在。
     *
     * @param path 文件或目录路径
     * @return 如果存在返回 true，否则返回 false
     */
    boolean exists(String path);

    /**
     * 列出指定目录下的所有子文件和子文件夹名称。
     *
     * @param path 目录路径
     * @return 子文件和子文件夹名称的字符串列表
     */
    List<String> listDirectory(String path);
}
