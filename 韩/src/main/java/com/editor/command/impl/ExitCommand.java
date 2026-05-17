package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * 退出程序命令。
 */
public class ExitCommand implements Command {
    private final Workspace workspace;
    private final List<String> savedFiles = new ArrayList<>();

    /**
     * 构造退出命令。
     *
     * @param workspace 工作区
     */
    public ExitCommand(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * 对所有未保存文件执行确认保存，然后向工作区发出退出请求。
     */
    @Override
    public void execute() {
        for (String file : workspace.getModifiedFiles()) {
            if (workspace.confirmSave(file)) {
                workspace.saveFile(file);
                savedFiles.add(file);
            }
        }
        workspace.requestExit();
    }

    /**
     * 退出命令不支持撤销。
     */
    @Override
    public void undo() {
        throw new UnsupportedOperationException("exit 不支持撤销");
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return "exit";
    }

    /**
     * @return 退出命令不进入撤销栈
     */
    @Override
    public boolean isUndoable() {
        return false;
    }

    /**
     * @return 退出命令属于工作区级命令
     */
    @Override
    public boolean useWorkspaceInvoker() {
        return true;
    }
}
