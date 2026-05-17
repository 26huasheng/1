package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 开启指定文件或当前活动文件的日志。
 */
public class LogOnCommand implements Command {
    private final Workspace workspace;
    private final String filePath;
    private boolean previousState;
    private String resolvedFile;

    /**
     * 构造开启日志命令。
     *
     * @param workspace 工作区
     * @param filePath 目标文件，可为空
     */
    public LogOnCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    /**
     * 执行开启日志逻辑。
     */
    @Override
    public void execute() {
        resolvedFile = filePath == null ? workspace.getActiveFileName() : workspace.normalizePath(filePath);
        previousState = workspace.getEditorByFileName(resolvedFile).isLogEnabled();
        workspace.setLogEnabled(resolvedFile, true);
    }

    /**
     * 撤销日志开启，恢复之前的开关状态。
     */
    @Override
    public void undo() {
        workspace.setLogEnabled(resolvedFile, previousState);
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return filePath == null ? "log-on" : "log-on " + filePath;
    }
}
