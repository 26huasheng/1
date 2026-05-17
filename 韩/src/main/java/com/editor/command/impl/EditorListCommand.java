package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 列出当前工作区中所有已打开文件及其状态。
 */
public class EditorListCommand implements Command {
    private final Workspace workspace;

    /**
     * 构造编辑器列表命令。
     *
     * @param workspace 工作区
     */
    public EditorListCommand(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * 打印编辑器列表。
     */
    @Override
    public void execute() {
        for (Workspace.EditorInfo info : workspace.getEditorInfoList()) {
            String prefix = info.active ? "* " : "  ";
            String modified = info.modified ? " [modified]" : "";
            System.out.println(prefix + info.fileName + modified);
        }
    }

    /**
     * 列表展示命令不支持撤销。
     */
    @Override
    public void undo() {
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return "editor-list";
    }

    /**
     * @return 展示类命令不进入撤销栈
     */
    @Override
    public boolean isUndoable() {
        return false;
    }
}
