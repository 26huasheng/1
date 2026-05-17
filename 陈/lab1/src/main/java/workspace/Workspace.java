package workspace;

import command.CommandManager;
import command.ICommand;
import core.EditorException;
import core.IFileSystem;
import editor.IEditor;
import editor.TextEditor;
import log.FileLogger;
import log.ICommandObserver;
import log.ISubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 工作区核心类（Facade 模式 + Observer 模式主题）。
 * <p>
 * 作为系统的门面，统一管理文件编辑器、命令调度器、
 * 日志观察者以及工作区状态的保存与恢复。
 * </p>
 */
public class Workspace implements ISubject {

    /**
     * 文件系统抽象接口引用。
     */
    private final IFileSystem fs;

    /**
     * 文件路径到编辑器实例的映射表。
     */
    private final Map<String, IEditor> editors;

    /**
     * 当前活动文件的路径。
     */
    private String activeFilePath;

    /**
     * 命令调度器实例。
     */
    private final CommandManager cmdManager;

    /**
     * 已注册的观察者列表。
     */
    private final List<ICommandObserver> observers;

    /**
     * 构造一个工作区实例。
     *
     * @param fs 文件系统抽象接口
     */
    public Workspace(IFileSystem fs) {
        this.fs = fs;
        this.editors = new HashMap<>();
        this.cmdManager = new CommandManager();
        this.observers = new ArrayList<>();
    }

    @Override
    public void attachObserver(ICommandObserver o) {
        observers.add(o);
    }

    @Override
    public void detachObserver(ICommandObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String msg) {
        for (ICommandObserver observer : observers) {
            observer.onCommandExecuted(msg);
        }
    }

    /**
     * 执行编辑器命令的解耦枢纽方法。
     * <p>
     * 内部调用命令调度器执行命令，并通过 notifyObservers 通知所有观察者。
     * </p>
     *
     * @param cmd 要执行的命令
     */
    public void executeEditorCommand(ICommand cmd) {
        cmdManager.executeCommand(cmd, logMsg -> notifyObservers(logMsg));
    }

    /**
     * 初始化一个新文件。
     * <p>
     * 若文件已存在则抛出异常。新建 TextEditor 实例。
     * 若 withLog 为真，执行一条 AppendCommand("# log") 并自动挂载 FileLogger。
     * </p>
     *
     * @param file   文件路径
     * @param withLog 是否启用日志功能
     * @throws EditorException 当文件已存在时抛出
     */
    public void init(String file, boolean withLog) {
        if (fs.exists(file)) {
            throw new EditorException("文件已存在: " + file);
        }

        IEditor editor = new TextEditor(file);
        editors.put(file, editor);
        activeFilePath = file;

        if (withLog) {
            String logFilePath = file + ".log";
            FileLogger fileLogger = new FileLogger(fs, logFilePath);
            attachObserver(fileLogger);

            ICommand appendCommand = new command.AppendCommand(editor, "# log");
            executeEditorCommand(appendCommand);
        }
    }

    /**
     * 加载一个已存在的文件。
     * <p>
     * 用 fs 读取文件内容构建 TextEditor。
     * 若首行刚好是 "# log"，也自动实例化并挂载 FileLogger。
     * </p>
     *
     * @param file 文件路径
     * @throws EditorException 当文件不存在时抛出
     */
    public void load(String file) {
        if (!fs.exists(file)) {
            throw new EditorException("文件不存在: " + file);
        }

        List<String> lines = fs.readLines(file);
        IEditor editor = new TextEditor(file);

        for (String line : lines) {
            editor.append(line);
        }

        editors.put(file, editor);
        activeFilePath = file;

        if (!lines.isEmpty() && lines.get(0).equals("# log")) {
            String logFilePath = file + ".log";
            FileLogger fileLogger = new FileLogger(fs, logFilePath);
            attachObserver(fileLogger);
        }
    }

    /**
     * 保存指定文件。
     * <p>
     * 通过 getLineIterator 获取文本内容，调用 fs 写入文件，
     * 并清除该文件的 modified 状态。
     * </p>
     *
     * @param file 文件路径
     * @throws EditorException 当文件未打开时抛出
     */
    public void save(String file) {
        if (!editors.containsKey(file)) {
            throw new EditorException("文件未打开: " + file);
        }

        IEditor editor = editors.get(file);
        List<String> lines = new ArrayList<>();
        Iterator<String> it = editor.getLineIterator(1, getLineCount(editor));
        while (it.hasNext()) {
            lines.add(it.next());
        }

        fs.writeLines(file, lines);
        editor.setModified(false);
    }

    /**
     * 保存所有已打开的文件。
     */
    public void saveAll() {
        for (String file : editors.keySet()) {
            save(file);
        }
    }

    /**
     * 关闭指定文件。
     * <p>
     * 检查文件是否已修改，若已修改则抛出异常提示用户先保存。
     * 否则关闭文件并切换下一个文件为活动文件。
     * </p>
     *
     * @param file 文件路径
     * @throws EditorException 当文件未保存时抛出
     */
    public void close(String file) {
        if (!editors.containsKey(file)) {
            throw new EditorException("文件未打开: " + file);
        }

        IEditor editor = editors.get(file);
        if (editor.isModified()) {
            throw new EditorException("文件未保存，请先保存");
        }

        editors.remove(file);

        if (activeFilePath != null && activeFilePath.equals(file)) {
            if (!editors.isEmpty()) {
                activeFilePath = editors.keySet().iterator().next();
            } else {
                activeFilePath = null;
            }
        }
    }

    /**
     * 切换活动文件。
     *
     * @param file 文件路径
     * @throws EditorException 当文件未打开时抛出
     */
    public void edit(String file) {
        if (!editors.containsKey(file)) {
            throw new EditorException("文件未打开: " + file);
        }
        activeFilePath = file;
    }

    /**
     * 获取当前活动文件的编辑器实例。
     *
     * @return 活动文件的编辑器实例
     * @throws EditorException 当没有活动文件时抛出
     */
    public IEditor getActiveEditor() {
        if (activeFilePath == null) {
            throw new EditorException("没有活动文件");
        }
        return editors.get(activeFilePath);
    }

    /**
     * 获取指定文件的编辑器实例。
     *
     * @param file 文件路径
     * @return 编辑器实例
     */
    public IEditor getEditor(String file) {
        return editors.get(file);
    }

    /**
     * 获取当前活动文件的路径。
     *
     * @return 活动文件路径
     */
    public String getActiveFilePath() {
        return activeFilePath;
    }

    /**
     * 获取所有已打开的文件路径列表。
     *
     * @return 文件路径列表
     */
    public List<String> getOpenedFiles() {
        return new ArrayList<>(editors.keySet());
    }

    /**
     * 保存当前工作区状态（Memento 模式）。
     *
     * @return 工作区备忘录实例
     */
    public WorkspaceMemento saveState() {
        List<String> openedFiles = new ArrayList<>(editors.keySet());
        Map<String, Boolean> modifiedStates = new HashMap<>();
        for (String file : editors.keySet()) {
            modifiedStates.put(file, editors.get(file).isModified());
        }
        return new WorkspaceMemento(openedFiles, activeFilePath, modifiedStates);
    }

    /**
     * 恢复工作区状态（Memento 模式）。
     *
     * @param memento 工作区备忘录实例
     */
    public void restoreState(WorkspaceMemento memento) {
        this.activeFilePath = memento.getActiveFile();

        for (Map.Entry<String, Boolean> entry : memento.getModifiedStates().entrySet()) {
            IEditor editor = editors.get(entry.getKey());
            if (editor != null) {
                editor.setModified(entry.getValue());
            }
        }
    }

    /**
     * 获取命令调度器实例。
     *
     * @return 命令调度器实例
     */
    public CommandManager getCommandManager() {
        return cmdManager;
    }

    /**
     * 获取编辑器的总行数。
     *
     * @param editor 编辑器实例
     * @return 总行数
     */
    private int getLineCount(IEditor editor) {
        int count = 0;
        try {
            Iterator<String> it = editor.getLineIterator(1, Integer.MAX_VALUE);
            while (it.hasNext()) {
                it.next();
                count++;
            }
        } catch (Exception e) {
            return count;
        }
        return count;
    }
}
