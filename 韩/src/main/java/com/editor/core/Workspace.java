package com.editor.core;

import com.editor.command.CommandInvoker;
import com.editor.event.EditorEvent;
import com.editor.event.EventListener;
import com.editor.event.EventType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作区核心对象。
 * 负责管理打开的编辑器、活动文件、工作区级撤销历史、事件发布和状态快照。
 */
public class Workspace {
    private final Map<String, Editor> editors = new LinkedHashMap<>();
    private final LinkedList<String> recentFiles = new LinkedList<>();
    private final List<EventListener> listeners = new ArrayList<>();
    private final FileManager fileManager = new FileManager();
    private final CommandInvoker workspaceInvoker = new CommandInvoker();
    private ConfirmationHandler confirmationHandler = fileName -> false;
    private String activeFileName;
    private boolean exitRequested;

    /**
     * 加载一个文件到工作区。
     * 如果文件已打开，仅切换为活动文件；如果文件不存在，则创建空缓冲区。
     *
     * @param filePath 文件路径
     */
    public void loadFile(String filePath) {
        String normalized = normalizePath(filePath);
        if (editors.containsKey(normalized)) {
            setActiveFile(normalized);
            return;
        }
        try {
            TextEditor editor;
            Path path = Paths.get(normalized);
            if (path.toFile().exists()) {
                editor = new TextEditor(normalized, fileManager.readFile(path));
                editor.markSaved();
            } else {
                editor = new TextEditor(normalized, "");
                editor.markModified();
            }
            editors.put(normalized, editor);
            setActiveFile(normalized);
            publishStateChange(normalized);
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取文件: " + filePath, e);
        }
    }

    /**
     * 创建新的缓冲区文件。
     *
     * @param filePath 目标文件路径
     * @param withLog 是否写入 # log 并启用日志
     */
    public void initFile(String filePath, boolean withLog) {
        String normalized = normalizePath(filePath);
        if (editors.containsKey(normalized)) {
            throw new IllegalArgumentException("文件已打开: " + normalized);
        }
        TextEditor editor = new TextEditor(normalized, withLog ? "# log" : "");
        editor.setLogEnabled(withLog);
        editor.markModified();
        editors.put(normalized, editor);
        setActiveFile(normalized);
        publishStateChange(normalized);
    }

    /**
     * 保存指定文件；当参数为空时保存当前活动文件。
     *
     * @param filePath 目标文件路径，可为空
     */
    public void saveFile(String filePath) {
        String resolved = resolveTargetFile(filePath);
        Editor editor = requireEditor(resolved);
        try {
            fileManager.writeFile(Paths.get(resolved), editor.getContentForSave());
            editor.markSaved();
            publishEvent(new EditorEvent(EventType.FILE_SAVED, this, null, resolved));
            publishStateChange(resolved);
        } catch (IOException e) {
            throw new IllegalArgumentException("无法保存文件: " + resolved, e);
        }
    }

    /**
     * 保存当前工作区中所有已打开的文件。
     */
    public void saveAll() {
        for (String fileName : new ArrayList<>(editors.keySet())) {
            saveFile(fileName);
        }
    }

    /**
     * 关闭一个文件，并返回可用于撤销的编辑器状态快照。
     *
     * @param filePath 待关闭文件，可为空表示关闭当前活动文件
     * @return 被关闭文件的状态快照
     */
    public EditorState closeFile(String filePath) {
        String resolved = resolveTargetFile(filePath);
        Editor editor = requireEditor(resolved);
        EditorState state = captureEditorState(editor);
        editors.remove(resolved);
        recentFiles.remove(resolved);
        if (Objects.equals(activeFileName, resolved)) {
            activeFileName = recentFiles.isEmpty() ? null : recentFiles.getFirst();
        }
        publishStateChange(activeFileName);
        return state;
    }

    /**
     * 使用之前保存的状态恢复一个编辑器。
     *
     * @param state 编辑器状态快照
     * @param activate 是否将其设为活动文件
     */
    public void restoreEditorState(EditorState state, boolean activate) {
        TextEditor editor = new TextEditor(state.fileName, state.content);
        editor.setLogEnabled(state.logEnabled);
        if (state.modified) {
            editor.markModified();
        } else {
            editor.markSaved();
        }
        editors.put(state.fileName, editor);
        touchRecent(state.fileName);
        if (activate || activeFileName == null) {
            setActiveFile(state.fileName);
        }
        publishStateChange(state.fileName);
    }

    /**
     * 切换当前活动文件。
     *
     * @param filePath 已打开文件的路径
     */
    public void setActiveFile(String filePath) {
        String resolved = resolveTargetFile(filePath);
        requireEditor(resolved);
        activeFileName = resolved;
        touchRecent(resolved);
        publishStateChange(resolved);
    }

    /**
     * 将当前活动文件标记为已修改。
     */
    public void markActiveFileModified() {
        Editor editor = getActiveEditor();
        if (editor == null) {
            throw new IllegalStateException("当前没有活动文件");
        }
        editor.markModified();
        publishStateChange(activeFileName);
    }

    /**
     * 将当前活动文件标记为已保存。
     */
    public void markActiveFileSaved() {
        Editor editor = getActiveEditor();
        if (editor == null) {
            throw new IllegalStateException("当前没有活动文件");
        }
        editor.markSaved();
        publishStateChange(activeFileName);
    }

    /**
     * @return 当前活动编辑器，不存在时返回 null
     */
    public Editor getActiveEditor() {
        return activeFileName == null ? null : editors.get(activeFileName);
    }

    /**
     * @return 当前活动文件名
     */
    public String getActiveFileName() {
        return activeFileName;
    }

    /**
     * 获取当前应使用的命令调用器。
     * 若无活动文件，则退化为工作区级调用器。
     *
     * @return 命令调用器
     */
    public CommandInvoker getActiveCommandInvoker() {
        Editor editor = getActiveEditor();
        return editor == null ? workspaceInvoker : editor.getCommandInvoker();
    }

    /**
     * @return 工作区级命令调用器
     */
    public CommandInvoker getWorkspaceCommandInvoker() {
        return workspaceInvoker;
    }

    /**
     * 仅撤销当前活动编辑器中的文本操作。
     *
     * @return 是否撤销成功
     */
    public boolean undo() {
        Editor activeEditor = getActiveEditor();
        if (activeEditor == null) {
            return false;
        }
        CommandInvoker activeInvoker = activeEditor.getCommandInvoker();
        return activeInvoker.canUndo() && activeInvoker.undo();
    }

    /**
     * 仅重做当前活动编辑器中的文本操作。
     *
     * @return 是否重做成功
     */
    public boolean redo() {
        Editor activeEditor = getActiveEditor();
        if (activeEditor == null) {
            return false;
        }
        CommandInvoker activeInvoker = activeEditor.getCommandInvoker();
        return activeInvoker.canRedo() && activeInvoker.redo();
    }

    /**
     * @return 工作区中是否存在未保存文件
     */
    public boolean hasUnsavedFiles() {
        return editors.values().stream().anyMatch(Editor::isModified);
    }

    /**
     * 收集所有已修改但未保存的文件名。
     *
     * @return 未保存文件列表
     */
    public List<String> getModifiedFiles() {
        List<String> result = new ArrayList<>();
        for (Editor editor : editors.values()) {
            if (editor.isModified()) {
                result.add(editor.getFileName());
            }
        }
        return result;
    }

    /**
     * 通过当前确认回调询问是否保存文件。
     *
     * @param fileName 文件名
     * @return 用户确认结果
     */
    public boolean confirmSave(String fileName) {
        return confirmationHandler.confirm(fileName);
    }

    /**
     * 设置关闭文件、退出程序等场景下的保存确认策略。
     *
     * @param confirmationHandler 确认回调
     */
    public void setConfirmationHandler(ConfirmationHandler confirmationHandler) {
        this.confirmationHandler = confirmationHandler == null ? fileName -> false : confirmationHandler;
    }

    /**
     * 返回用于展示的编辑器信息列表。
     *
     * @return 编辑器视图对象列表
     */
    public List<EditorInfo> getEditorInfoList() {
        List<EditorInfo> infos = new ArrayList<>();
        for (Editor editor : editors.values()) {
            infos.add(new EditorInfo(
                    editor.getFileName(),
                    Objects.equals(activeFileName, editor.getFileName()),
                    editor.isModified(),
                    editor.isLogEnabled()
            ));
        }
        return infos;
    }

    /**
     * @return 当前所有打开的编辑器快照
     */
    public Collection<Editor> getAllEditors() {
        return List.copyOf(editors.values());
    }

    /**
     * 根据文件名查找已打开的编辑器。
     *
     * @param fileName 文件名
     * @return 找到的编辑器，不存在时返回 null
     */
    public Editor getEditorByFileName(String fileName) {
        return editors.get(normalizePath(fileName));
    }

    /**
     * 判断文件当前是否已打开。
     *
     * @param fileName 文件名
     * @return 已打开返回 true
     */
    public boolean isOpen(String fileName) {
        return editors.containsKey(normalizePath(fileName));
    }

    /**
     * 开启或关闭某个文件的日志功能。
     *
     * @param fileName 文件名
     * @param enabled 是否启用日志
     */
    public void setLogEnabled(String fileName, boolean enabled) {
        requireEditor(resolveTargetFile(fileName)).setLogEnabled(enabled);
        publishStateChange(resolveTargetFile(fileName));
    }

    /**
     * 创建当前工作区的可持久化快照。
     *
     * @return 工作区快照
     */
    public WorkspaceSnapshot createSnapshot() {
        WorkspaceSnapshot snapshot = new WorkspaceSnapshot();
        snapshot.activeFile = activeFileName;
        snapshot.openFiles = new ArrayList<>(editors.keySet());
        for (Editor editor : editors.values()) {
            snapshot.modifiedStatus.put(editor.getFileName(), editor.isModified());
            snapshot.logEnabledStatus.put(editor.getFileName(), editor.isLogEnabled());
        }
        return snapshot;
    }

    /**
     * 根据持久化快照恢复工作区状态。
     *
     * @param snapshot 工作区快照
     */
    public void restoreSnapshot(WorkspaceSnapshot snapshot) {
        editors.clear();
        recentFiles.clear();
        activeFileName = null;
        if (snapshot == null) {
            return;
        }
        for (String file : snapshot.openFiles) {
            loadFile(file);
            Editor editor = getEditorByFileName(file);
            if (editor != null) {
                boolean modified = snapshot.modifiedStatus.getOrDefault(file, false);
                if (modified) {
                    editor.markModified();
                } else {
                    editor.markSaved();
                }
                editor.setLogEnabled(snapshot.logEnabledStatus.getOrDefault(file, editor.isLogEnabled()));
            }
        }
        if (snapshot.activeFile != null && isOpen(snapshot.activeFile)) {
            setActiveFile(snapshot.activeFile);
        }
    }

    /**
     * 标记工作区进入退出流程。
     */
    public void requestExit() {
        this.exitRequested = true;
    }

    /**
     * @return 是否已收到退出请求
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * 注册事件监听器。
     *
     * @param listener 监听器实例
     */
    public void addEventListener(EventListener listener) {
        listeners.add(listener);
    }

    /**
     * 向所有监听器广播事件。
     *
     * @param event 待广播事件
     */
    public void publishEvent(EditorEvent event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    /**
     * 统一将文件路径转为绝对规范路径，避免同一文件被重复打开。
     *
     * @param filePath 原始路径
     * @return 规范化后的绝对路径
     */
    public String normalizePath(String filePath) {
        return Paths.get(filePath).toAbsolutePath().normalize().toString();
    }

    /**
     * 根据源文件路径推导对应日志文件路径。
     *
     * @param fileName 源文件名
     * @return 日志文件路径
     */
    public Path getLogPath(String fileName) {
        Path filePath = Paths.get(resolveTargetFile(fileName));
        String logFileName = "." + filePath.getFileName() + ".log";
        return filePath.resolveSibling(logFileName);
    }

    /**
     * 保存前确认接口，便于 CLI 注入交互逻辑，测试时注入固定策略。
     */
    public interface ConfirmationHandler {
        boolean confirm(String fileName);
    }

    /**
     * 用于列表展示的轻量级编辑器信息。
     */
    public static class EditorInfo {
        public final String fileName;
        public final boolean active;
        public final boolean modified;
        public final boolean logEnabled;

        /**
         * 构造一条编辑器展示记录。
         *
         * @param fileName 文件名
         * @param active 是否为活动文件
         * @param modified 是否已修改
         * @param logEnabled 是否启用日志
         */
        public EditorInfo(String fileName, boolean active, boolean modified, boolean logEnabled) {
            this.fileName = fileName;
            this.active = active;
            this.modified = modified;
            this.logEnabled = logEnabled;
        }
    }

    /**
     * 关闭文件时保存的编辑器快照，用于撤销关闭操作。
     */
    public static class EditorState {
        public final String fileName;
        public final String content;
        public final boolean modified;
        public final boolean logEnabled;

        /**
         * 构造编辑器状态快照。
         *
         * @param fileName 文件名
         * @param content 文件内容
         * @param modified 修改标记
         * @param logEnabled 日志开关
         */
        public EditorState(String fileName, String content, boolean modified, boolean logEnabled) {
            this.fileName = fileName;
            this.content = content;
            this.modified = modified;
            this.logEnabled = logEnabled;
        }
    }

    /**
     * 工作区持久化快照，仅保存实验要求的最小状态。
     */
    public static class WorkspaceSnapshot {
        public List<String> openFiles = new ArrayList<>();
        public String activeFile;
        public Map<String, Boolean> modifiedStatus = new LinkedHashMap<>();
        public Map<String, Boolean> logEnabledStatus = new LinkedHashMap<>();
    }

    /**
     * 获取一个已打开编辑器，不存在则抛出异常。
     *
     * @param fileName 文件名
     * @return 对应编辑器
     */
    private Editor requireEditor(String fileName) {
        Editor editor = editors.get(fileName);
        if (editor == null) {
            throw new IllegalArgumentException("文件未打开: " + fileName);
        }
        return editor;
    }

    /**
     * 解析命令中的目标文件。
     * 当未显式传入文件名时，默认使用当前活动文件。
     *
     * @param fileName 原始文件名，可为空
     * @return 最终使用的文件名
     */
    private String resolveTargetFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            if (activeFileName == null) {
                throw new IllegalStateException("当前没有活动文件");
            }
            return activeFileName;
        }
        return normalizePath(fileName);
    }

    /**
     * 从编辑器对象中提取可恢复状态。
     *
     * @param editor 编辑器
     * @return 编辑器快照
     */
    private EditorState captureEditorState(Editor editor) {
        return new EditorState(
                editor.getFileName(),
                editor.getContentForSave(),
                editor.isModified(),
                editor.isLogEnabled()
        );
    }

    /**
     * 更新最近访问文件列表，用于 close 后选择回退的活动文件。
     *
     * @param fileName 最近访问的文件
     */
    private void touchRecent(String fileName) {
        recentFiles.remove(fileName);
        recentFiles.addFirst(fileName);
    }

    /**
     * 发布工作区状态变化事件。
     *
     * @param fileName 与当前状态变更关联的文件
     */
    private void publishStateChange(String fileName) {
        publishEvent(new EditorEvent(EventType.WORKSPACE_STATE_CHANGED, this, null, fileName));
    }
}
