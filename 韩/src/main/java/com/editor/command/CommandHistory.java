package com.editor.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 维护命令的撤销栈和重做栈。
 */
public class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * 将一条已执行成功的命令压入撤销栈，并清空重做栈。
     *
     * @param command 已执行完成的命令
     */
    public void record(Command command) {
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * 撤销最近一次命令。
     *
     * @return 撤销成功返回 true
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return true;
    }

    /**
     * 重做最近一次被撤销的命令。
     *
     * @return 重做成功返回 true
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return true;
    }

    /**
     * 清空历史记录。
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * @return 当前是否存在可撤销命令
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * @return 当前是否存在可重做命令
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
