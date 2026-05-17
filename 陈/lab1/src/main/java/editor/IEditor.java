package editor;

import java.util.Iterator;

/**
 * 文本编辑器核心接口。
 * <p>
 * 定义了文本编辑器的基本操作，包括追加、插入、删除、替换
 * 以及行迭代器访问等功能。所有行号和列号均从 1 开始计数。
 * </p>
 */
public interface IEditor {

    /**
     * 在文本末尾追加指定文本。
     * <p>
     * 若 text 包含换行符 {@code \n}，必须拆分为多行存储。
     * </p>
     *
     * @param text 要追加的文本内容
     */
    void append(String text);

    /**
     * 在指定行和列位置插入指定文本。
     * <p>
     * 行号和列号均从 1 开始计数。若 text 包含换行符，
     * 必须将当前行切断并分割为多行插入。
     * </p>
     *
     * @param line 目标行号（从 1 开始）
     * @param col  目标列号（从 1 开始）
     * @param text 要插入的文本内容
     * @throws core.EditorException 当行号或列号越界时抛出
     */
    void insert(int line, int col, String text);

    /**
     * 从指定行和列位置删除指定长度的字符。
     * <p>
     * 不可跨行删除。返回被删除的子串，供撤销功能使用。
     * </p>
     *
     * @param line 起始行号（从 1 开始）
     * @param col  起始列号（从 1 开始）
     * @param len  要删除的字符长度
     * @return 被删除的子串
     * @throws core.EditorException 当行号、列号越界或删除长度超出行尾时抛出
     */
    String delete(int line, int col, int len);

    /**
     * 替换指定行和列位置的指定长度字符为新文本。
     * <p>
     * 内部先执行 delete 再执行 insert，返回被替换的旧文本。
     * </p>
     *
     * @param line 起始行号（从 1 开始）
     * @param col  起始列号（从 1 开始）
     * @param len  要替换的字符长度
     * @param text 新的文本内容
     * @return 被替换的旧文本
     */
    String replace(int line, int col, int len, String text);

    /**
     * 获取指定行范围的行迭代器。
     * <p>
     * 使用迭代器模式隐藏内部 List 实现细节。
     * </p>
     *
     * @param startLine 起始行号（从 1 开始）
     * @param endLine   结束行号（从 1 开始）
     * @return 指定行范围内文本行的迭代器
     */
    Iterator<String> getLineIterator(int startLine, int endLine);

    /**
     * 检查编辑器内容是否已被修改。
     *
     * @return 如果已修改返回 true，否则返回 false
     */
    boolean isModified();

    /**
     * 设置编辑器的修改状态。
     *
     * @param modified 修改状态
     */
    void setModified(boolean modified);

    /**
     * 获取当前编辑器关联的文件路径。
     *
     * @return 文件路径字符串
     */
    String getFilePath();
}
