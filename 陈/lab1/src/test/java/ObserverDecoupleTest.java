

import command.AppendCommand;
import core.IFileSystem;
import core.MockFileSystem;
import log.FileLogger;
import workspace.Workspace;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 日志解耦验证测试类（Observer 模式）。
 * <p>
 * 验证 Workspace 的 init 操作能正确触发观察者自动挂载，
 * 且命令执行后日志能通过 MockFileSystem 的内存 Map 正确记录，
 * 包含 append 动作记录和时间戳。全程无物理磁盘读写。
 * </p>
 */
public class ObserverDecoupleTest {

    /**
     * 模拟文件系统实例。
     */
    private MockFileSystem fs;

    /**
     * 工作区实例。
     */
    private Workspace ws;

    @Before
    public void setUp() {
        fs = new MockFileSystem();
        ws = new Workspace(fs);
    }

    /**
     * 测试 init 触发观察者自动挂载及日志记录。
     * <p>
     * 执行 ws.init("test.txt", true) 后，执行一条 AppendCommand，
     * 从 fs 内部的 memoryDisk Map 中取出 "test.txt.log" 键对应的值，
     * 断言其中不仅生成了文件，且包含了 session start 标记和 append 动作记录。
     * </p>
     */
    @Test
    public void testInitWithLogAndObserverTrigger() {
        ws.init("test.txt", true);

        ws.executeEditorCommand(new AppendCommand(ws.getActiveEditor(), "Hello World"));

        Map<String, List<String>> memoryDisk = fs.getMemoryDisk();

        assertTrue("日志文件应被创建", memoryDisk.containsKey("test.txt.log"));

        List<String> logLines = memoryDisk.get("test.txt.log");

        assertFalse("日志文件不应为空", logLines.isEmpty());

        boolean hasSessionStart = false;
        boolean hasAppendAction = false;
        boolean hasTimestamp = false;

        for (String line : logLines) {
            if (line.contains("session start at")) {
                hasSessionStart = true;
            }
            if (line.contains("append")) {
                hasAppendAction = true;
            }
            if (line.matches("\\d{8} \\d{2}:\\d{2}:\\d{2}.*")) {
                hasTimestamp = true;
            }
        }

        assertTrue("日志应包含 session start 标记", hasSessionStart);
        assertTrue("日志应包含 append 动作记录", hasAppendAction);
        assertTrue("日志应包含时间戳", hasTimestamp);
    }

    /**
     * 测试无日志模式 init 不应创建日志文件。
     */
    @Test
    public void testInitWithoutLog() {
        ws.init("test.txt", false);

        Map<String, List<String>> memoryDisk = fs.getMemoryDisk();

        assertFalse("无日志模式不应创建日志文件", memoryDisk.containsKey("test.txt.log"));
    }

    /**
     * 测试手动 log-on 后命令应被记录到日志。
     */
    @Test
    public void testManualLogOn() {
        ws.init("test.txt", false);

        ws.executeEditorCommand(new AppendCommand(ws.getActiveEditor(), "First line"));

        assertFalse("未开启日志时不应有日志文件", fs.getMemoryDisk().containsKey("test.txt.log"));

        ws.executeEditorCommand(new command.AppendCommand(ws.getActiveEditor(), "Second line"));

        FileLogger logger = new FileLogger(fs, "test.txt.log");
        ws.attachObserver(logger);

        ws.executeEditorCommand(new command.AppendCommand(ws.getActiveEditor(), "Third line"));

        assertTrue("手动开启日志后应创建日志文件", fs.getMemoryDisk().containsKey("test.txt.log"));
    }
}
