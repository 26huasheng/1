package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 切换当前活动文件。
 */
public class EditCommand implements Command {
    private final Workspace workspace;
    private final String filePath;
    private String previousActiveFile;

    /**
     * 构造切换命令。
     *
     * @param workspace 工作区
     * @param filePath 目标文件
     */
    public EditCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    /**
     * 执行切换活动文件。
     */
    @Override
    public void execute() {
        previousActiveFile = workspace.getActiveFileName();
        workspace.setActiveFile(filePath);
    }

    /**
     * 撤销切换，恢复到之前的活动文件。
     */
    @Override
    public void undo() {
        if (previousActiveFile != null) {
            workspace.setActiveFile(previousActiveFile);
        }
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return "edit " + filePath;
    }

    /**
     * @return 切换活动文件属于工作区级命令
     */
    @Override
    public boolean useWorkspaceInvoker() {
        return true;
    }
}
