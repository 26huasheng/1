package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 关闭指定文件或当前活动文件的日志。
 */
public class LogOffCommand implements Command {
    private final Workspace workspace;
    private final String filePath;
    private boolean previousState;
    private String resolvedFile;

    /**
     * 构造关闭日志命令。
     *
     * @param workspace 工作区
     * @param filePath 目标文件，可为空
     */
    public LogOffCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    /**
     * 执行关闭日志逻辑。
     */
    @Override
    public void execute() {
        resolvedFile = filePath == null ? workspace.getActiveFileName() : workspace.normalizePath(filePath);
        previousState = workspace.getEditorByFileName(resolvedFile).isLogEnabled();
        workspace.setLogEnabled(resolvedFile, false);
    }

    /**
     * 撤销日志关闭，恢复到之前的开关状态。
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
        return filePath == null ? "log-off" : "log-off " + filePath;
    }
}
