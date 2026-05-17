package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 初始化一个新的缓冲区文件。
 */
public class InitCommand implements Command {
    private final Workspace workspace;
    private final String filePath;
    private final boolean withLog;
    private String previousActiveFile;

    /**
     * 构造初始化命令。
     *
     * @param workspace 工作区
     * @param filePath 文件路径
     * @param withLog 是否带日志头
     */
    public InitCommand(Workspace workspace, String filePath, boolean withLog) {
        this.workspace = workspace;
        this.filePath = filePath;
        this.withLog = withLog;
    }

    /**
     * 创建新缓冲区并切换为活动文件。
     */
    @Override
    public void execute() {
        previousActiveFile = workspace.getActiveFileName();
        workspace.initFile(filePath, withLog);
    }

    /**
     * 撤销初始化操作，即关闭刚创建的缓冲区。
     */
    @Override
    public void undo() {
        workspace.closeFile(filePath);
        if (previousActiveFile != null && workspace.isOpen(previousActiveFile)) {
            workspace.setActiveFile(previousActiveFile);
        }
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return withLog ? "init " + filePath + " with-log" : "init " + filePath;
    }

    /**
     * @return 初始化命令属于工作区级命令
     */
    @Override
    public boolean useWorkspaceInvoker() {
        return true;
    }
}
