package com.editor.command.impl;

import com.editor.command.AbstractTextChangeCommand;
import com.editor.core.TextEditor;
import com.editor.core.Workspace;

/**
 * 替换指定位置开始的一段文本。
 */
public class ReplaceCommand extends AbstractTextChangeCommand {
    private final int line;
    private final int col;
    private final int len;
    private final String text;

    /**
     * 构造替换命令。
     *
     * @param workspace 工作区
     * @param line 行号
     * @param col 列号
     * @param len 被替换长度
     * @param text 替换后的文本
     */
    public ReplaceCommand(Workspace workspace, int line, int col, int len, String text) {
        super(workspace);
        this.line = line;
        this.col = col;
        this.len = len;
        this.text = text;
    }

    /**
     * 执行替换逻辑。
     *
     * @param editor 当前文本编辑器
     */
    @Override
    protected void apply(TextEditor editor) {
        editor.replace(line, col, len, text);
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return "replace " + line + ":" + col + " " + len + " \"" + text + "\"";
    }
}
