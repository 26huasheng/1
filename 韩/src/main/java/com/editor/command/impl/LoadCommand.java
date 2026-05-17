package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 将文件加载到工作区中。
 */
public class LoadCommand implements Command {
    private final Workspace workspace;
    private final String filePath;
    private String previousActiveFile;
    private boolean alreadyOpen;

    /**
     * 构造加载命令。
     *
     * @param workspace 工作区
     * @param filePath 文件路径
     */
    public LoadCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    /**
     * 执行加载逻辑。
     */
    @Override
    public void execute() {
        previousActiveFile = workspace.getActiveFileName();
        alreadyOpen = workspace.isOpen(filePath);
        workspace.loadFile(filePath);
    }

    /**
     * 如果该文件原先并未打开，则撤销时关闭它，并恢复之前的活动文件。
     */
    @Override
    public void undo() {
        if (!alreadyOpen) {
            workspace.closeFile(filePath);
        }
        if (previousActiveFile != null && workspace.isOpen(previousActiveFile)) {
            workspace.setActiveFile(previousActiveFile);
        }
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return "load " + filePath;
    }

    /**
     * @return 加载命令属于工作区级命令
     */
    @Override
    public boolean useWorkspaceInvoker() {
        return true;
    }
}
