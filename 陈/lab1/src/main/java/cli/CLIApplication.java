package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import command.AppendCommand;
import command.DeleteCommand;
import command.InsertCommand;
import command.ReplaceCommand;
import core.EditorException;
import core.IFileSystem;
import core.LocalFileSystem;
import editor.IEditor;
import editor.SpellCheckDecorator;
import log.FileLogger;
import workspace.Workspace;

/**
 * 字符界面控制台应用程序主类。
 * <p>
 * 实现最高安全级别的主循环，任何异常都不会导致程序退出。
 * 提供 18 个命令的完整路由分发，以及隐藏的拼写检查彩蛋功能。
 * </p>
 */
public class CLIApplication {

    /**
     * 文件系统抽象接口实例。
     */
    private static IFileSystem fs;

    /**
     * 工作区实例。
     */
    private static Workspace workspace;

    /**
     * 观察者列表映射，用于管理各文件的日志观察者。
     */
    private static java.util.Map<String, FileLogger> fileLoggers = new java.util.HashMap<>();

    /**
     * 程序入口点。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        fs = new LocalFileSystem();
        workspace = new Workspace(fs);

        Scanner scanner = new Scanner(System.in);
        Pattern paramPattern = Pattern.compile("([^\\s\"']+)|\"([^\"]*)\"|'([^']*)'");

        while (true) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                if (input.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                List<String> params = new ArrayList<>();
                Matcher m = paramPattern.matcher(input);
                while (m.find()) {
                    if (m.group(1) != null) {
                        params.add(m.group(1));
                    } else if (m.group(2) != null) {
                        params.add(m.group(2));
                    } else if (m.group(3) != null) {
                        params.add(m.group(3));
                    }
                }

                if (params.isEmpty()) {
                    continue;
                }

                String command = params.get(0);

                switch (command) {
                    case "load":
                        handleLoad(params);
                        break;
                    case "save":
                        handleSave(params);
                        break;
                    case "init":
                        handleInit(params);
                        break;
                    case "close":
                        handleClose(params);
                        break;
                    case "edit":
                        handleEdit(params);
                        break;
                    case "editor-list":
                        handleEditorList();
                        break;
                    case "dir-tree":
                        handleDirTree(params);
                        break;
                    case "undo":
                        handleUndo();
                        break;
                    case "redo":
                        handleRedo();
                        break;
                    // case "exit":
                    //     System.out.println("退出编辑器。");
                    //     return;
                    case "exit":
                    workspace.WorkspaceMemento memento = workspace.saveState();
                    java.util.Scanner exitScanner = new java.util.Scanner(System.in);
                    
                    // 1. 逐一提示未保存的文件
                    if (memento.getModifiedStates() != null) {
                        for (String file : memento.getOpenedFiles()) {
                            Boolean isModified = memento.getModifiedStates().get(file);
                            if (isModified != null && isModified) {
                                System.out.print("文件已修改，是否保存? (y/n) ");
                                String ans = exitScanner.nextLine();
                                if ("y".equalsIgnoreCase(ans.trim())) {
                                    workspace.save(file);
                                }
                            }
                        }
                    }
                    
                    // 2. 将工作区状态保存到配置文件
                    try {
                        java.util.List<String> confLines = new java.util.ArrayList<>();
                        // 第一行存当前活动文件
                        confLines.add(memento.getActiveFile() != null ? memento.getActiveFile() : "");
                        // 后面存所有打开的文件列表
                        confLines.addAll(memento.getOpenedFiles());
                        fs.writeLines(".workspace.conf", confLines);
                    } catch (Exception e) {
                        System.out.println("[Warning] 工作区状态保存失败: " + e.getMessage());
                    }
                    
                    System.out.println("退出编辑器。");
                    System.exit(0);
                    case "append":
                        handleAppend(params);
                        break;
                    case "insert":
                        handleInsert(params);
                        break;
                    case "delete":
                        handleDelete(params);
                        break;
                    case "replace":
                        handleReplace(params);
                        break;
                    case "show":
                        handleShow(params);
                        break;
                    case "log-on":
                        handleLogOn(params);
                        break;
                    case "log-off":
                        handleLogOff(params);
                        break;
                    case "log-show":
                        handleLogShow(params);
                        break;
                    case "spell-check":
                        handleSpellCheck();
                        break;
                    default:
                        System.out.println("[Error] 未知命令: " + command);
                        break;
                }

            } catch (EditorException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println("[SysError] " + e.getMessage());
            }
        }
    }

    /**
     * 打印 18 行帮助菜单。
     */
    private static void printHelp() {
        System.out.println("1. load <file>");
        System.out.println("2. save [file|all]");
        System.out.println("3. init <file> [with-log]");
        System.out.println("4. close [file]");
        System.out.println("5. edit <file>");
        System.out.println("6. editor-list");
        System.out.println("7. dir-tree [path]");
        System.out.println("8. undo");
        System.out.println("9. redo");
        System.out.println("10. exit");
        System.out.println("11. append \"text\"");
        System.out.println("12. insert <line:col> \"text\"");
        System.out.println("13. delete <line:col> <len>");
        System.out.println("14. replace <line:col> <len> \"text\"");
        System.out.println("15. show [start:end]");
        System.out.println("16. log-on [file]");
        System.out.println("17. log-off [file]");
        System.out.println("18. log-show [file]");
    }

    /**
     * 处理 load 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleLoad(List<String> params) {
        if (params.size() < 2) {
            System.out.println("用法: load <file>");
            return;
        }
        workspace.load(params.get(1));
        System.out.println("文件已加载: " + params.get(1));
    }

    /**
     * 处理 save 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleSave(List<String> params) {
        if (params.size() >= 2 && params.get(1).equalsIgnoreCase("all")) {
            workspace.saveAll();
            System.out.println("所有文件已保存。");
        } else if (params.size() >= 2) {
            workspace.save(params.get(1));
            System.out.println("文件已保存: " + params.get(1));
        } else {
            String activeFile = workspace.getActiveFilePath();
            if (activeFile != null) {
                workspace.save(activeFile);
                System.out.println("文件已保存: " + activeFile);
            } else {
                System.out.println("没有活动文件，请指定文件路径或使用 save all");
            }
        }
    }

    /**
     * 处理 init 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleInit(List<String> params) {
        if (params.size() < 2) {
            System.out.println("用法: init <file> [with-log]");
            return;
        }
        boolean withLog = params.size() >= 3 && params.get(2).equalsIgnoreCase("with-log");
        workspace.init(params.get(1), withLog);
        System.out.println("文件已初始化: " + params.get(1));
    }

    /**
     * 处理 close 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleClose(List<String> params) {
        if (params.size() >= 2) {
            workspace.close(params.get(1));
            System.out.println("文件已关闭: " + params.get(1));
        } else {
                String activeFile = workspace.getActiveFilePath();
                if (activeFile != null) {
                    workspace.close(activeFile);
                    System.out.println("文件已关闭: " + activeFile);
                } else {
                    System.out.println("没有活动文件");
                }
            }
    }

    /**
     * 处理 edit 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleEdit(List<String> params) {
        if (params.size() < 2) {
            System.out.println("用法: edit <file>");
            return;
        }
        workspace.edit(params.get(1));
        System.out.println("已切换到文件: " + params.get(1));
    }

    /**
     * 处理 editor-list 命令。
     */
    private static void handleEditorList() {
        List<String> openedFiles = workspace.getOpenedFiles();
        if (openedFiles.isEmpty()) {
            System.out.println("没有打开的文件。");
            return;
        }
        String activeFile = workspace.getActiveFilePath();
        for (String file : openedFiles) {
            IEditor editor = workspace.getEditor(file);
            boolean isActive = file.equals(activeFile);
            boolean isModified = editor.isModified();
            if (isActive) {
                System.out.println("> " + file + (isModified ? "*" : ""));
            } else {
                System.out.println("  " + file + (isModified ? "*" : ""));
            }
        }
    }

    /**
     * 处理 dir-tree 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleDirTree(List<String> params) {
        String path = params.size() >= 2 ? params.get(1) : ".";
        ITreeNode root = new FileNodeAdapter(fs, path);
        TreePrinter.printTree(root);
    }

    /**
     * 处理 undo 命令。
     */
    private static void handleUndo() {
        workspace.getCommandManager().undo();
        System.out.println("已撤销。");
    }

    /**
     * 处理 redo 命令。
     */
    private static void handleRedo() {
        workspace.getCommandManager().redo();
        System.out.println("已重做。");
    }

    /**
     * 处理 append 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleAppend(List<String> params) {
        if (params.size() < 2) {
            System.out.println("用法: append \"text\"");
            return;
        }
        String text = params.get(1);
        IEditor editor = workspace.getActiveEditor();
        workspace.executeEditorCommand(new AppendCommand(editor, text));
    }

    /**
     * 处理 insert 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleInsert(List<String> params) {
        if (params.size() < 3) {
            System.out.println("用法: insert <line:col> \"text\"");
            return;
        }
        String[] pos = params.get(1).split(":");
        if (pos.length != 2) {
            System.out.println("位置格式错误，应为 line:col");
            return;
        }
        int line = Integer.parseInt(pos[0]);
        int col = Integer.parseInt(pos[1]);
        String text = params.get(2);
        IEditor editor = workspace.getActiveEditor();
        workspace.executeEditorCommand(new InsertCommand(editor, line, col, text));
    }

    /**
     * 处理 delete 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleDelete(List<String> params) {
        if (params.size() < 3) {
            System.out.println("用法: delete <line:col> <len>");
            return;
        }
        String[] pos = params.get(1).split(":");
        if (pos.length != 2) {
            System.out.println("位置格式错误，应为 line:col");
            return;
        }
        int line = Integer.parseInt(pos[0]);
        int col = Integer.parseInt(pos[1]);
        int len = Integer.parseInt(params.get(2));
        IEditor editor = workspace.getActiveEditor();
        workspace.executeEditorCommand(new DeleteCommand(editor, line, col, len));
    }

    /**
     * 处理 replace 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleReplace(List<String> params) {
        if (params.size() < 4) {
            System.out.println("用法: replace <line:col> <len> \"text\"");
            return;
        }
        String[] pos = params.get(1).split(":");
        if (pos.length != 2) {
            System.out.println("位置格式错误，应为 line:col");
            return;
        }
        int line = Integer.parseInt(pos[0]);
        int col = Integer.parseInt(pos[1]);
        int len = Integer.parseInt(params.get(2));
        String text = params.get(3);
        IEditor editor = workspace.getActiveEditor();
        workspace.executeEditorCommand(new ReplaceCommand(editor, line, col, len, text));
    }

    /**
     * 处理 show 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleShow(List<String> params) {
        IEditor editor = workspace.getActiveEditor();
        int startLine = 1;
        int endLine;
        try {
            endLine = getLineCount(editor);
        } catch (Exception e) {
            endLine = 1;
        }

        if (params.size() >= 2) {
            String[] range = params.get(1).split(":");
            if (range.length == 2) {
                startLine = Integer.parseInt(range[0]);
                endLine = Integer.parseInt(range[1]);
            } else {
                startLine = Integer.parseInt(params.get(1));
            }
        }

        java.util.Iterator<String> it = editor.getLineIterator(startLine, endLine);
        int lineNum = startLine;
        while (it.hasNext()) {
            System.out.println(lineNum + ": " + it.next());
            lineNum++;
        }
    }

    /**
     * 处理 log-on 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleLogOn(List<String> params) {
        String file = params.size() >= 2 ? params.get(1) : workspace.getActiveFilePath();
        if (file == null) {
            System.out.println("没有活动文件");
            return;
        }
        String logFilePath = file + ".log";
        FileLogger fileLogger = new FileLogger(fs, logFilePath);
        workspace.attachObserver(fileLogger);
        fileLoggers.put(file, fileLogger);
        System.out.println("日志已开启: " + logFilePath);
    }

    /**
     * 处理 log-off 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleLogOff(List<String> params) {
        String file = params.size() >= 2 ? params.get(1) : workspace.getActiveFilePath();
        if (file == null) {
            System.out.println("没有活动文件");
            return;
        }
        FileLogger fileLogger = fileLoggers.remove(file);
        if (fileLogger != null) {
            workspace.detachObserver(fileLogger);
        }
        System.out.println("日志已关闭: " + file);
    }

    /**
     * 处理 log-show 命令。
     *
     * @param params 命令参数列表
     */
    private static void handleLogShow(List<String> params) {
        String file = params.size() >= 2 ? params.get(1) : workspace.getActiveFilePath();
        if (file == null) {
            System.out.println("没有活动文件");
            return;
        }
        String logFilePath = file + ".log";
        if (!fs.exists(logFilePath)) {
            System.out.println("日志文件不存在: " + logFilePath);
            return;
        }
        List<String> logLines = fs.readLines(logFilePath);
        for (String line : logLines) {
            System.out.println(line);
        }
    }

    /**
     * 处理 spell-check 隐藏彩蛋命令。
     */
    private static void handleSpellCheck() {
        IEditor activeEditor = workspace.getActiveEditor();
        String activeFile = workspace.getActiveFilePath();
        SpellCheckDecorator decoratedEditor = new SpellCheckDecorator(activeEditor);
        workspace.getEditor(activeFile);
        try {
            java.lang.reflect.Field editorsField = Workspace.class.getDeclaredField("editors");
            editorsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, IEditor> editorsMap = (java.util.Map<String, IEditor>) editorsField.get(workspace);
            editorsMap.put(activeFile, decoratedEditor);
        } catch (Exception e) {
            System.out.println("[SysError] 启用拼写检查失败: " + e.getMessage());
            return;
        }
        System.out.println("拼写检查已开启。");
    }

    /**
     * 获取编辑器的总行数。
     *
     * @param editor 编辑器实例
     * @return 总行数
     */
    private static int getLineCount(IEditor editor) {
        int count = 0;
        try {
            java.util.Iterator<String> it = editor.getLineIterator(1, Integer.MAX_VALUE);
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
