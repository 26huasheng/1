package command;

/**
 * 命令接口。
 * <p>
 * 定义命令模式的基本操作，包括执行、撤销和获取日志。
 * 所有具体命令类必须实现此接口以支持撤销/重做机制。
 * </p>
 */
public interface ICommand {

    /**
     * 执行命令操作。
     */
    void execute();

    /**
     * 撤销命令操作，恢复到执行前的状态。
     */
    void undo();

    /**
     * 获取命令的日志字符串。
     * <p>
     * 格式示例：{@code "20251024 09:41:40 append \"text\""}，
     * 时间戳必须是命令创建时的真实时间。
     * </p>
     *
     * @return 格式化的日志字符串
     */
    String getCommandLog();
}
