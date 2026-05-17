package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

/**
 * 保存当前文件、指定文件或全部文件。
 */
public class SaveCommand implements Command {
    private final Workspace workspace;
    private final String target;

    /**
     * 构造保存命令。
     *
     * @param workspace 工作区
     * @param target 保存目标，可为 null、文件名或 all
     */
    public SaveCommand(Workspace workspace, String target) {
        this.workspace = workspace;
        this.target = target;
    }

    /**
     * 执行保存逻辑。
     */
    @Override
    public void execute() {
        if ("all".equalsIgnoreCase(target)) {
            workspace.saveAll();
        } else {
            workspace.saveFile(target);
        }
    }

    /**
     * 保存命令不支持撤销。
     */
    @Override
    public void undo() {
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return target == null ? "save" : "save " + target;
    }

    /**
     * @return 保存命令不进入撤销栈
     */
    @Override
    public boolean isUndoable() {
        return false;
    }

    /**
     * 保存动作本身会单独发布 FILE_SAVED 事件，这里不再重复发布命令事件。
     *
     * @return false
     */
    @Override
    public boolean shouldPublishCommandEvent() {
        return false;
    }
}
