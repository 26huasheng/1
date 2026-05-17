package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 关闭当前活动文件或指定文件。
 */
public class CloseCommand implements Command {
    private final Workspace workspace;
    private final String filePath;
    private Workspace.EditorState closedState;
    private String previousActiveFile;
    private boolean wasActive;

    /**
     * 构造关闭命令。
     *
     * @param workspace 工作区
     * @param filePath 待关闭文件，可为空
     */
    public CloseCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    /**
     * 执行关闭逻辑；若文件已修改，会先走确认回调决定是否保存。
     */
    @Override
    public void execute() {
        String target = filePath == null ? workspace.getActiveFileName() : workspace.normalizePath(filePath);
        if (target == null) {
            throw new IllegalStateException("当前没有活动文件");
        }
        previousActiveFile = workspace.getActiveFileName();
        wasActive = target.equals(previousActiveFile);
        if (workspace.getEditorByFileName(target).isModified() && workspace.confirmSave(target)) {
            workspace.saveFile(target);
        }
        closedState = workspace.closeFile(target);
    }

    /**
     * 通过之前记录的编辑器快照恢复被关闭的文件。
     */
    @Override
    public void undo() {
        workspace.restoreEditorState(closedState, wasActive);
        if (!wasActive && previousActiveFile != null && workspace.isOpen(previousActiveFile)) {
            workspace.setActiveFile(previousActiveFile);
        }
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return filePath == null ? "close" : "close " + filePath;
    }

    /**
     * @return 关闭命令属于工作区级命令
     */
    @Override
    public boolean useWorkspaceInvoker() {
        return true;
    }
}
