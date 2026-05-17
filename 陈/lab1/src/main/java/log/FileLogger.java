package log;

import core.EditorException;
import core.IFileSystem;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件日志记录器（Observer 模式中的具体观察者）。
 * <p>
 * 实现 ICommandObserver 接口，接收命令执行后的日志消息，
 * 并通过 IFileSystem 接口追加写入到日志文件中。
 * 实例化后的第一条日志会先写入 session start 标记。
 * </p>
 */
public class FileLogger implements ICommandObserver {

    /**
     * 文件系统抽象接口引用。
     */
    private final IFileSystem fs;

    /**
     * 日志文件路径。
     */
    private final String logFilePath;

    /**
     * 标记是否已写入 session start 标记。
     */
    private boolean isFirstLog = true;

    /**
     * 构造一个文件日志记录器实例。
     *
     * @param fs         文件系统抽象接口
     * @param logFilePath 日志文件路径
     */
    public FileLogger(IFileSystem fs, String logFilePath) {
        this.fs = fs;
        this.logFilePath = logFilePath;
    }

    @Override
    public void onCommandExecuted(String logMessage) {
        if (isFirstLog) {
            String sessionStart = "session start at " + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
            try {
                fs.appendLine(logFilePath, sessionStart);
            } catch (EditorException e) {
                System.err.println("日志写入失败: " + e.getMessage());
            }
            isFirstLog = false;
        }

        try {
            fs.appendLine(logFilePath, logMessage);
        } catch (EditorException e) {
            System.err.println("日志写入失败: " + e.getMessage());
        }
    }
}
