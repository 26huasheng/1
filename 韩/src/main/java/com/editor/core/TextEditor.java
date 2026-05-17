package com.editor.core;

import com.editor.command.CommandInvoker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 纯文本编辑器实现，内部使用按行存储的结构管理文本内容。
 */
public class TextEditor implements Editor {
    private String fileName;
    private final List<String> lines = new ArrayList<>();
    private boolean modified;
    private boolean logEnabled;
    private final CommandInvoker invoker = new CommandInvoker();

    /**
     * 创建文本编辑器，并根据初始内容决定是否自动开启日志。
     *
     * @param fileName 文件名
     * @param initialContent 初始内容
     */
    public TextEditor(String fileName, String initialContent) {
        this.fileName = fileName;
        loadContentFromString(initialContent);
        this.modified = false;
        this.logEnabled = shouldAutoEnableLog();
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public CommandInvoker getCommandInvoker() {
        return invoker;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void markModified() {
        this.modified = true;
    }

    @Override
    public void markSaved() {
        this.modified = false;
    }

    @Override
    public boolean isLogEnabled() {
        return logEnabled;
    }

    @Override
    public void setLogEnabled(boolean enabled) {
        this.logEnabled = enabled;
    }

    @Override
    public String getContentForSave() {
        return String.join("\n", lines);
    }

    @Override
    public void loadContentFromString(String content) {
        lines.clear();
        if (content == null || content.isEmpty()) {
            return;
        }
        lines.addAll(Arrays.asList(content.split("\\R", -1)));
    }

    @Override
    public String getType() {
        return "text";
    }

    /**
     * @return 当前完整内容的字符串表示
     */
    public String getContentAsString() {
        return getContentForSave();
    }

    /**
     * 使用快照内容恢复编辑器状态。
     *
     * @param content 旧内容
     * @param wasModified 旧的修改标记
     */
    public void restoreFromString(String content, boolean wasModified) {
        loadContentFromString(content);
        if (wasModified) {
            markModified();
        } else {
            markSaved();
        }
    }

    /**
     * 在文件尾部追加一整行文本。
     *
     * @param text 追加内容
     */
    public void appendLine(String text) {
        if (text == null) {
            text = "";
        }
        lines.add(text);
        markModified();
    }

    /**
     * 在指定行列插入文本。
     * 当文本中包含换行符时，会将原有单行拆分成多行。
     *
     * @param line 行号，从 1 开始
     * @param col 列号，从 1 开始
     * @param text 待插入文本
     */
    public void insert(int line, int col, String text) {
        if (text == null) {
            text = "";
        }
        if (lines.isEmpty()) {
            if (line != 1 || col != 1) {
                throw new IllegalArgumentException("空文件只能在1:1位置插入");
            }
            loadInsertedIntoEmpty(text);
            markModified();
            return;
        }

        validatePosition(line, col);
        int lineIndex = line - 1;
        String currentLine = lines.get(lineIndex);
        String prefix = currentLine.substring(0, col - 1);
        String suffix = currentLine.substring(col - 1);
        String[] insertedLines = text.split("\\n", -1);

        // 多行插入时，首行接前缀，中间行为新增整行，末行接原后缀。
        if (insertedLines.length == 1) {
            lines.set(lineIndex, prefix + text + suffix);
        } else {
            lines.set(lineIndex, prefix + insertedLines[0]);
            for (int i = 1; i < insertedLines.length - 1; i++) {
                lines.add(lineIndex + i, insertedLines[i]);
            }
            lines.add(lineIndex + insertedLines.length - 1,
                    insertedLines[insertedLines.length - 1] + suffix);
        }
        markModified();
    }

    /**
     * 从指定位置开始删除固定长度的字符，删除范围不允许跨行。
     *
     * @param line 行号，从 1 开始
     * @param col 列号，从 1 开始
     * @param len 删除长度
     */
    public void delete(int line, int col, int len) {
        if (len < 0) {
            throw new IllegalArgumentException("删除长度不能为负数");
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        validatePosition(line, col);
        int lineIndex = line - 1;
        String currentLine = lines.get(lineIndex);
        int start = col - 1;
        int end = start + len;
        if (end > currentLine.length()) {
            throw new IllegalArgumentException("删除长度超出行尾");
        }
        lines.set(lineIndex, currentLine.substring(0, start) + currentLine.substring(end));
        markModified();
    }

    /**
     * 先删除再插入，从而实现替换操作。
     *
     * @param line 行号，从 1 开始
     * @param col 列号，从 1 开始
     * @param len 被替换长度
     * @param text 替换后的文本
     */
    public void replace(int line, int col, int len, String text) {
        delete(line, col, len);
        insert(line, col, text);
    }

    /**
     * 按行号范围生成展示文本。
     *
     * @param startLine 起始行号，可为 null
     * @param endLine 结束行号，可为 null
     * @return 带行号的展示字符串
     */
    public String show(Integer startLine, Integer endLine) {
        if (lines.isEmpty()) {
            return "";
        }
        int start = startLine == null ? 1 : startLine;
        int end = endLine == null ? lines.size() : endLine;
        if (start < 1 || end < start || end > lines.size()) {
            throw new IllegalArgumentException("显示范围越界");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = start; i <= end; i++) {
            builder.append(i).append(": ").append(lines.get(i - 1));
            if (i < end) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    /**
     * @return 当前文本总行数
     */
    public int getLineCount() {
        return lines.size();
    }

    /**
     * 处理空文件上的首次插入。
     *
     * @param text 要插入的文本
     */
    private void loadInsertedIntoEmpty(String text) {
        if (text.isEmpty()) {
            lines.add("");
            return;
        }
        lines.addAll(Arrays.asList(text.split("\\n", -1)));
    }

    /**
     * 校验插入、删除、替换使用的行列坐标是否合法。
     *
     * @param line 行号
     * @param col 列号
     */
    private void validatePosition(int line, int col) {
        if (line < 1 || line > lines.size()) {
            throw new IllegalArgumentException("行号或列号越界");
        }
        String target = lines.get(line - 1);
        if (col < 1 || col > target.length() + 1) {
            throw new IllegalArgumentException("行号或列号越界");
        }
    }

    /**
     * 当第一行是 # log 时，自动将日志开关初始化为开启状态。
     *
     * @return 是否应自动开启日志
     */
    private boolean shouldAutoEnableLog() {
        return !lines.isEmpty() && "# log".equals(lines.get(0));
    }
}
