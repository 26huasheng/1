package editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import core.EditorException;

/**
 * 文本编辑器核心实现类。
 * <p>
 * 使用 List&lt;String&gt; 作为底层数据结构存储文本行，
 * 所有行号和列号均从 1 开始计数。提供严格的边界校验，
 * 确保所有编辑操作的合法性和安全性。
 * </p>
 */
public class TextEditor implements IEditor {

    /**
     * 存储文本行的列表，每个元素代表一行文本。
     */
    private List<String> lines = new ArrayList<>();

    /**
     * 当前编辑器关联的文件路径。
     */
    private String filePath;

    /**
     * 标记编辑器内容是否已被修改。
     */
    private boolean modified = false;

    /**
     * 构造一个空的 TextEditor 实例。
     */
    public TextEditor() {
    }

    /**
     * 构造一个关联指定文件路径的 TextEditor 实例。
     *
     * @param filePath 文件路径
     */
    public TextEditor(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void append(String text) {
        if (text.contains("\n")) {
            String[] splitLines = text.split("\n", -1);
            for (String line : splitLines) {
                lines.add(line);
            }
        } else {
            lines.add(text);
        }
        setModified(true);
    }

    @Override
    public void insert(int line, int col, String text) {
        if (lines.isEmpty()) {
            if (line != 1 || col != 1) {
                throw new EditorException("空文件只能在1:1位置插入");
            }
            if (text.contains("\n")) {
                String[] splitLines = text.split("\n", -1);
                for (String splitLine : splitLines) {
                    lines.add(splitLine);
                }
            } else {
                lines.add(text);
            }
            setModified(true);
            return;
        }

        if (line < 1 || line > lines.size() + 1) {
            throw new EditorException("行号或列号越界");
        }

        if (line == lines.size() + 1) {
            lines.add("");
        }

        int currentLineLength = lines.get(line - 1).length();
        if (col < 1 || col > currentLineLength + 1) {
            throw new EditorException("行号或列号越界");
        }

        if (text.contains("\n")) {
            String[] splitLines = text.split("\n", -1);
            String currentLine = lines.get(line - 1);
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1);

            lines.remove(line - 1);

            lines.add(line - 1, before + splitLines[0]);
            for (int i = 1; i < splitLines.length; i++) {
                lines.add(line - 1 + i, splitLines[i]);
            }

            int lastInsertedIndex = line - 1 + splitLines.length - 1;
            String lastInsertedLine = lines.get(lastInsertedIndex);
            lines.set(lastInsertedIndex, lastInsertedLine + after);
        } else {
            String currentLine = lines.get(line - 1);
            String before = currentLine.substring(0, col - 1);
            String after = currentLine.substring(col - 1);
            lines.set(line - 1, before + text + after);
        }

        setModified(true);
    }

    @Override
    public String delete(int line, int col, int len) {
        if (lines.isEmpty()) {
            throw new EditorException("行号或列号越界");
        }

        if (line < 1 || line > lines.size()) {
            throw new EditorException("行号或列号越界");
        }

        String currentLine = lines.get(line - 1);
        int currentLineLength = currentLine.length();

        if (col < 1 || col > currentLineLength + 1) {
            throw new EditorException("行号或列号越界");
        }

        if (col - 1 + len > currentLineLength) {
            throw new EditorException("删除长度超出行尾");
        }

        String deleted = currentLine.substring(col - 1, col - 1 + len);
        String newLine = currentLine.substring(0, col - 1) + currentLine.substring(col - 1 + len);

        if (newLine.isEmpty()) {
            lines.remove(line - 1);
        } else {
            lines.set(line - 1, newLine);
        }

        setModified(true);
        return deleted;
    }

    @Override
    public String replace(int line, int col, int len, String text) {
        String oldText = delete(line, col, len);
        insert(line, col, text);
        return oldText;
    }

    @Override
    // public Iterator<String> getLineIterator(int startLine, int endLine) {
    //     if (startLine < 1 || endLine > lines.size() || startLine > endLine) {
    //         throw new EditorException("行范围越界");
    //     }
    //     return new LineIterator(startLine, endLine);
    // }
    public Iterator<String> getLineIterator(int startLine, int endLine) {
    // 1. 如果文件是空的，直接返回空迭代器，不报错
    if (lines.isEmpty()) {
        return java.util.Collections.emptyIterator();
    }
    
    // 2. 自动修正越界的数字（宽容模式）
    int actualStart = Math.max(1, startLine);
    int actualEnd = Math.min(lines.size(), endLine);

    // 3. 如果修正后起始还是大于结束，才属于真正的逻辑错误
    if (actualStart > actualEnd) {
        throw new EditorException("行范围越界");
    }
    
    // 返回安全的子列表视图
    return lines.subList(actualStart - 1, actualEnd).iterator();
}

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    /**
     * 行迭代器内部实现类。
     * <p>
     * 隐藏内部 List 实现细节，提供安全的行范围迭代访问。
     * </p>
     */
    private class LineIterator implements Iterator<String> {

        private final int endLine;
        private int currentIndex;

        /**
         * 构造一个行迭代器实例。
         *
         * @param startLine 起始行号（从 1 开始）
         * @param endLine   结束行号（从 1 开始）
         */
        public LineIterator(int startLine, int endLine) {
            this.currentIndex = startLine - 1;
            this.endLine = endLine - 1;
        }

        @Override
        public boolean hasNext() {
            return currentIndex <= endLine;
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new EditorException("迭代器越界");
            }
            return lines.get(currentIndex++);
        }
    }
}
