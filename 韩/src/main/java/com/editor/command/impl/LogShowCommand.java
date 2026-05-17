package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 显示指定文件或当前活动文件对应的日志内容。
 */
public class LogShowCommand implements Command {
    private final Workspace workspace;
    private final String filePath;

    /**
     * 构造日志显示命令。
     *
     * @param workspace 工作区
     * @param filePath 目标文件，可为空
     */
    public LogShowCommand(Workspace workspace, String filePath) {
        this.workspace = workspace;
        this.filePath = filePath;
    }

    /**
     * 打印日志文件内容；若日志不存在则输出提示。
     */
    @Override
    public void execute() {
        String target = filePath == null ? workspace.getActiveFileName() : workspace.normalizePath(filePath);
        Path logPath = workspace.getLogPath(target);
        if (!Files.exists(logPath)) {
            System.out.println("(no log)");
            return;
        }
        try {
            System.out.println(Files.readString(logPath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取日志文件: " + logPath, e);
        }
    }

    /**
     * 展示类命令不支持撤销。
     */
    @Override
    public void undo() {
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return filePath == null ? "log-show" : "log-show " + filePath;
    }

    /**
     * @return 展示类命令不进入撤销栈
     */
    @Override
    public boolean isUndoable() {
        return false;
    }
}
