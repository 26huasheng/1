package com.editor.persistence;

import com.editor.core.Workspace;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * 负责将工作区状态保存到磁盘，并在启动时恢复。
 */
public class Persistence {
    private static final String DEFAULT_STORAGE = ".editor_workspace.properties";

    private final Workspace workspace;
    private final Path storagePath;

    /**
     * 使用默认路径创建持久化管理器。
     *
     * @param workspace 工作区对象
     */
    public Persistence(Workspace workspace) {
        this(workspace, DEFAULT_STORAGE);
    }

    /**
     * 指定持久化文件路径创建持久化管理器。
     *
     * @param workspace 工作区对象
     * @param storagePath 状态文件路径
     */
    public Persistence(Workspace workspace, String storagePath) {
        this.workspace = workspace;
        this.storagePath = Paths.get(storagePath).toAbsolutePath().normalize();
    }

    /**
     * 将当前工作区快照写入属性文件。
     */
    public void save() {
        Properties properties = new Properties();
        Workspace.WorkspaceSnapshot snapshot = workspace.createSnapshot();
        properties.setProperty("activeFile", snapshot.activeFile == null ? "" : snapshot.activeFile);
        properties.setProperty("openFiles", String.join("|", snapshot.openFiles));
        for (String file : snapshot.openFiles) {
            properties.setProperty("modified." + file, String.valueOf(snapshot.modifiedStatus.getOrDefault(file, false)));
            properties.setProperty("log." + file, String.valueOf(snapshot.logEnabledStatus.getOrDefault(file, false)));
        }
        try {
            Path parent = storagePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream outputStream = Files.newOutputStream(storagePath)) {
                properties.store(outputStream, "editor workspace");
            }
        } catch (IOException e) {
            System.err.println("Warning: Failed to save workspace state: " + e.getMessage());
        }
    }

    /**
     * 从属性文件恢复工作区状态。
     */
    public void restore() {
        if (!Files.exists(storagePath)) {
            return;
        }
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(storagePath)) {
            properties.load(inputStream);
            Workspace.WorkspaceSnapshot snapshot = new Workspace.WorkspaceSnapshot();
            String openFiles = properties.getProperty("openFiles", "");
            if (!openFiles.isBlank()) {
                snapshot.openFiles = List.of(openFiles.split("\\|"));
            }
            snapshot.activeFile = blankToNull(properties.getProperty("activeFile"));
            for (String file : snapshot.openFiles) {
                snapshot.modifiedStatus.put(file,
                        Boolean.parseBoolean(properties.getProperty("modified." + file, "false")));
                snapshot.logEnabledStatus.put(file,
                        Boolean.parseBoolean(properties.getProperty("log." + file, "false")));
            }
            workspace.restoreSnapshot(snapshot);
        } catch (IOException e) {
            System.err.println("Warning: Failed to restore workspace state: " + e.getMessage());
        }
    }

    /**
     * 将空字符串视为 null，避免把空活动文件名恢复成无意义字符串。
     *
     * @param value 原始值
     * @return 归一化后的值
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
