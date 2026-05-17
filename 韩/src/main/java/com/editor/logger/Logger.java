package com.editor.logger;

import com.editor.core.Editor;
import com.editor.core.Workspace;
import com.editor.event.EditorEvent;
import com.editor.event.EventListener;
import com.editor.event.EventType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志监听器。
 * 监听命令执行和文件保存事件，并按文件写入对应日志。
 */
public class Logger implements EventListener {
    private final Workspace workspace;
    private final Map<String, PrintWriter> writers = new HashMap<>();
    private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    /**
     * 创建日志监听器。
     *
     * @param workspace 当前工作区
     */
    public Logger(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * 处理工作区事件，仅对命令执行和保存事件进行记录。
     *
     * @param event 事件对象
     */
    @Override
    public void onEvent(EditorEvent event) {
        if (event.getType() != EventType.COMMAND_EXECUTED && event.getType() != EventType.FILE_SAVED) {
            return;
        }
        String fileName = event.getFileName();
        if (fileName == null) {
            return;
        }
        Editor editor = workspace.getEditorByFileName(fileName);
        // 保存事件发生时，对应编辑器可能已经被关闭，因此只要路径存在即可写日志。
        if (event.getType() == EventType.FILE_SAVED && editor == null) {
            // 关闭/退出时按路径记录保存，不要求 editor 仍存在。
        } else if (editor == null || !editor.isLogEnabled()) {
            return;
        }

        PrintWriter writer = getOrCreateWriter(fileName);
        if (writer == null) {
            return;
        }

        String timestamp = format.format(new Date(event.getTimestamp()));
        if (event.getType() == EventType.FILE_SAVED) {
            writer.println(timestamp + " save");
        } else {
            writer.println(timestamp + " " + event.getCommandName());
        }
        writer.flush();
    }

    /**
     * 关闭所有已打开的日志写入器。
     */
    public void closeAllWriters() {
        for (PrintWriter writer : writers.values()) {
            writer.close();
        }
        writers.clear();
    }

    /**
     * 获取某个文件对应的日志写入器；若不存在则按需创建。
     *
     * @param fileName 源文件名
     * @return 对应写入器，创建失败时返回 null
     */
    private PrintWriter getOrCreateWriter(String fileName) {
        if (writers.containsKey(fileName)) {
            return writers.get(fileName);
        }
        Path logPath = workspace.getLogPath(fileName);
        File logFile = logPath.toFile();
        try {
            boolean isNew = !logFile.exists();
            File parent = logFile.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
            // 首次创建日志文件时补一条 session start 记录。
            if (isNew) {
                writer.println("session start at " + format.format(new Date()));
                writer.flush();
            }
            writers.put(fileName, writer);
            return writer;
        } catch (IOException e) {
            System.err.println("Warning: Cannot open log file " + logPath);
            return null;
        }
    }
}
