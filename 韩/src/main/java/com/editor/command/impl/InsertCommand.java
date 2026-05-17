package com.editor.command.impl;

import com.editor.command.AbstractTextChangeCommand;
import com.editor.core.TextEditor;
import com.editor.core.Workspace;

/**
 * 在指定行列位置插入文本。
 */
public class InsertCommand extends AbstractTextChangeCommand {
    private final int line;
    private final int col;
    private final String text;

    /**
     * 构造插入命令。
     *
     * @param workspace 工作区
     * @param line 行号
     * @param col 列号
     * @param text 文本内容
     */
    public InsertCommand(Workspace workspace, int line, int col, String text) {
        super(workspace);
        this.line = line;
        this.col = col;
        this.text = text;
    }

    /**
     * 执行插入逻辑。
     *
     * @param editor 当前文本编辑器
     */
    @Override
    protected void apply(TextEditor editor) {
        editor.insert(line, col, text);
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return "insert " + line + ":" + col + " \"" + text + "\"";
    }
}
