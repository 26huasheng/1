package core;

/**
 * 自定义全局异常类。
 * <p>
 * 业务逻辑中的任何越界、错误均通过此异常抛出，
 * 继承自 RuntimeException 以支持 unchecked 异常处理机制。
 * </p>
 */
public class EditorException extends RuntimeException {

    /**
     * 构造一个新的 EditorException 实例。
     *
     * @param message 异常描述信息
     */
    public EditorException(String message) {
        super(message);
    }
}
