package com.editor.command;

import com.editor.core.Workspace;
import com.editor.event.EditorEvent;
import com.editor.event.EventType;

/**
 * 负责真正执行命令，并维护对应的命令历史。
 */
public class CommandInvoker {
    private final CommandHistory history = new CommandHistory();

    /**
     * 执行命令，并根据命令属性决定是否记录历史、发布事件。
     *
     * @param command 待执行的命令
     * @param workspace 当前工作区
     */
    public void executeCommand(Command command, Workspace workspace) {
        command.execute();
        if (command.isUndoable()) {
            history.record(command);
        }
        if (command.shouldPublishCommandEvent()) {
            workspace.publishEvent(new EditorEvent(
                    EventType.COMMAND_EXECUTED,
                    workspace,
                    command.getName(),
                    workspace.getActiveFileName()
            ));
        }
    }

    /**
     * 撤销上一条命令。
     *
     * @return 撤销成功返回 true
     */
    public boolean undo() {
        return history.undo();
    }

    /**
     * 重做上一条被撤销的命令。
     *
     * @return 重做成功返回 true
     */
    public boolean redo() {
        return history.redo();
    }

    /**
     * @return 是否存在可撤销命令
     */
    public boolean canUndo() {
        return history.canUndo();
    }

    /**
     * @return 是否存在可重做命令
     */
    public boolean canRedo() {
        return history.canRedo();
    }

    /**
     * 清空内部历史记录。
     */
    public void clearHistory() {
        history.clear();
    }
}
