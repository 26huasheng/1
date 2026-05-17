package log;

/**
 * 主题接口（Observer 模式）。
 * <p>
 * 定义观察者的注册、移除和通知机制，
 * 用于解耦日志系统与命令执行逻辑。
 * </p>
 */
public interface ISubject {

    /**
     * 注册一个观察者。
     *
     * @param o 要注册的观察者实例
     */
    void attachObserver(ICommandObserver o);

    /**
     * 移除一个已注册的观察者。
     *
     * @param o 要移除的观察者实例
     */
    void detachObserver(ICommandObserver o);

    /**
     * 通知所有已注册的观察者。
     *
     * @param msg 要通知的消息字符串
     */
    void notifyObservers(String msg);
}
