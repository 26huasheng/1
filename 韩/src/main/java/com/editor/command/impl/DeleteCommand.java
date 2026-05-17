package com.editor.command.impl;

import com.editor.command.AbstractTextChangeCommand;
import com.editor.core.TextEditor;
import com.editor.core.Workspace;

/**
 * 删除指定位置开始的一段文本。
 */
public class DeleteCommand extends AbstractTextChangeCommand {
    private final int line;
    private final int col;
    private final int len;

    /**
     * 构造删除命令。
     *
     * @param workspace 工作区
     * @param line 行号
     * @param col 列号
     * @param len 删除长度
     */
    public DeleteCommand(Workspace workspace, int line, int col, int len) {
        super(workspace);
        this.line = line;
        this.col = col;
        this.len = len;
    }

    /**
     * 执行删除逻辑。
     *
     * @param editor 当前文本编辑器
     */
    @Override
    protected void apply(TextEditor editor) {
        editor.delete(line, col, len);
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return "delete " + line + ":" + col + " " + len;
    }
}
