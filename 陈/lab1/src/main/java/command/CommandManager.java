package command;

import core.EditorException;

import java.util.Stack;
import java.util.function.Consumer;

/**
 * 命令调度器（Invoker）。
 * <p>
 * 管理命令的撤销栈和重做栈，提供执行、撤销、重做功能。
 * 执行新命令时强制清空重做栈，确保命令历史的一致性。
 * </p>
 */
public class CommandManager {

    /**
     * 撤销命令栈，存储已执行但可撤销的命令。
     */
    private Stack<ICommand> undoStack = new Stack<>();

    /**
     * 重做命令栈，存储已撤销但可重做的命令。
     */
    private Stack<ICommand> redoStack = new Stack<>();

    /**
     * 执行命令并记录日志。
     * <p>
     * 调用命令的 execute 方法，将命令压入撤销栈，
     * 强制清空重做栈，并通过回调触发日志记录。
     * </p>
     *
     * @param cmd         要执行的命令
     * @param logCallback 日志回调函数，接收命令日志字符串
     */
    public void executeCommand(ICommand cmd, Consumer<String> logCallback) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        logCallback.accept(cmd.getCommandLog());
    }

    /**
     * 撤销最近执行的命令。
     * <p>
     * 若撤销栈为空则抛出 EditorException("无可撤销")。
     * 弹栈执行 undo 方法，并将命令压入重做栈。
     * </p>
     *
     * @throws EditorException 当无可撤销命令时抛出
     */
    public void undo() {
        if (undoStack.isEmpty()) {
            throw new EditorException("无可撤销");
        }
        ICommand cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
    }

    /**
     * 重做最近撤销的命令。
     * <p>
     * 若重做栈为空则抛出 EditorException("无可重做")。
     * 弹栈执行 execute 方法，并将命令压入撤销栈。
     * </p>
     *
     * @throws EditorException 当无可重做命令时抛出
     */
    public void redo() {
        if (redoStack.isEmpty()) {
            throw new EditorException("无可重做");
        }
        ICommand cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
    }
}
