package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.TextEditor;
import com.editor.core.Workspace;

/**
 * 显示当前文本编辑器的全部内容或指定行范围。
 */
public class ShowCommand implements Command {
    private final Workspace workspace;
    private final Integer startLine;
    private final Integer endLine;

    /**
     * 构造显示命令。
     *
     * @param workspace 工作区
     * @param startLine 起始行，可为空
     * @param endLine 结束行，可为空
     */
    public ShowCommand(Workspace workspace, Integer startLine, Integer endLine) {
        this.workspace = workspace;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    /**
     * 打印显示结果。
     */
    @Override
    public void execute() {
        if (!(workspace.getActiveEditor() instanceof TextEditor editor)) {
            throw new IllegalStateException("当前活动文件不是文本编辑器");
        }
        System.out.println(editor.show(startLine, endLine));
    }

    /**
     * 展示类命令不支持撤销。
     */
    @Override
    public void undo() {
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        if (startLine == null) {
            return "show";
        }
        return "show " + startLine + ":" + endLine;
    }

    /**
     * @return 展示类命令不进入撤销栈
     */
    @Override
    public boolean isUndoable() {
        return false;
    }
}
