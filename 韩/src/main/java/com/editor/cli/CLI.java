package com.editor.cli;

import com.editor.command.Command;
import com.editor.command.CommandFactory;
import com.editor.command.CommandInvoker;
import com.editor.core.Workspace;
import com.editor.logger.Logger;
import com.editor.persistence.Persistence;

import java.util.Scanner;

/**
 * 命令行交互入口，负责驱动整个编辑器的 REPL 循环。
 */
public class CLI {
    private final Workspace workspace;
    private final CommandFactory commandFactory;
    private final Persistence persistence;
    private final Logger logger;
    private final Scanner scanner;
    private boolean running;

    /**
     * 使用默认工作区、标准输入和默认持久化路径启动 CLI。
     */
    public CLI() {
        this(new Workspace(), new Scanner(System.in), ".editor_workspace.properties");
    }

    /**
     * 允许在测试中注入自定义工作区和输入流。
     *
     * @param workspace 工作区对象
     * @param scanner 输入读取器
     */
    public CLI(Workspace workspace, Scanner scanner) {
        this(workspace, scanner, ".editor_workspace.properties");
    }

    /**
     * 构造 CLI，并初始化持久化、日志监听器和命令工厂。
     *
     * @param workspace 工作区对象
     * @param scanner 输入读取器
     * @param persistencePath 工作区状态保存路径
     */
    public CLI(Workspace workspace, Scanner scanner, String persistencePath) {
        this.workspace = workspace;
        this.scanner = scanner;
        this.persistence = new Persistence(workspace, persistencePath);
        this.persistence.restore();
        this.logger = new Logger(workspace);
        workspace.addEventListener(logger);
        workspace.setConfirmationHandler(this::confirmSave);
        this.commandFactory = new CommandFactory(workspace);
        this.running = true;
    }

    /**
     * 启动命令行主循环。
     * 循环中负责读取输入、分发命令、处理异常以及在结束时执行收尾动作。
     */
    public void start() {
        System.out.println("Text Editor v1.0. Type 'exit' to quit.");
        while (running && !workspace.isExitRequested()) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            try {
                // undo/redo 直接交给 Workspace 处理，不走普通命令工厂。
                if ("undo".equalsIgnoreCase(input)) {
                    System.out.println(workspace.undo() ? "undo success" : "nothing to undo");
                    continue;
                }
                if ("redo".equalsIgnoreCase(input)) {
                    System.out.println(workspace.redo() ? "redo success" : "nothing to redo");
                    continue;
                }
                Command command = commandFactory.createCommand(input);
                // 某些命令作用于整个工作区，因此要使用工作区级别的 invoker。
                CommandInvoker invoker = command.useWorkspaceInvoker()
                        ? workspace.getWorkspaceCommandInvoker()
                        : workspace.getActiveCommandInvoker();
                invoker.executeCommand(command, workspace);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.err.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
            }
        }
        shutdown();
    }

    /**
     * 外部可调用该方法主动结束主循环。
     */
    public void stop() {
        running = false;
    }

    /**
     * 程序主入口。
     *
     * @param args 命令行参数，当前版本未使用
     */
    public static void main(String[] args) {
        new CLI().start();
    }

    /**
     * 当文件有未保存修改时，向用户询问是否保存。
     *
     * @param fileName 待确认的文件名
     * @return 用户选择保存时返回 true
     */
    private boolean confirmSave(String fileName) {
        System.out.printf("文件已修改，是否保存? (y/n) [%s]%n", fileName);
        if (!scanner.hasNextLine()) {
            return false;
        }
        return "y".equalsIgnoreCase(scanner.nextLine().trim());
    }

    /**
     * 退出前统一执行状态持久化和资源释放。
     */
    private void shutdown() {
        persistence.save();
        logger.closeAllWriters();
        scanner.close();
    }
}
