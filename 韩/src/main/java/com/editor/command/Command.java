package com.editor.command;

/**
 * 所有用户命令的统一抽象。
 */
public interface Command {
    /**
     * 执行命令的主要逻辑。
     */
    void execute();

    /**
     * 撤销当前命令造成的状态变更。
     */
    void undo();

    /**
     * 返回命令的可读名称，主要用于日志记录和调试输出。
     *
     * @return 命令名称
     */
    String getName();

    /**
     * 标记当前命令是否应该进入撤销栈。
     *
     * @return 可撤销返回 true
     */
    default boolean isUndoable() {
        return true;
    }

    /**
     * 标记当前命令是否应交由工作区级 invoker 执行。
     *
     * @return 作用于工作区时返回 true
     */
    default boolean useWorkspaceInvoker() {
        return false;
    }

    /**
     * 标记命令执行后是否需要发布“命令已执行”事件。
     *
     * @return 需要发布事件时返回 true
     */
    default boolean shouldPublishCommandEvent() {
        return true;
    }
}
