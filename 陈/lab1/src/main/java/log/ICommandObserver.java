package log;

/**
 * 命令观察者接口。
 * <p>
 * 实现 Observer 模式中的观察者角色，用于接收命令执行后的日志通知。
 * </p>
 */
public interface ICommandObserver {

    /**
     * 当命令被执行时调用的回调方法。
     *
     * @param logMessage 命令的日志字符串
     */
    void onCommandExecuted(String logMessage);
}
