package com.editor.command.impl;

import com.editor.command.Command;
import com.editor.core.Workspace;
import com.editor.utils.DirTreeUtil;

import java.io.File;
import java.nio.file.Paths;

/**
 * 显示当前目录或指定目录的树形结构。
 */
public class DirTreeCommand implements Command {
    private final String path;

    /**
     * 构造目录树命令。
     *
     * @param workspace 工作区，当前实现中不直接使用，保留接口一致性
     * @param path 目标目录，可为空
     */
    public DirTreeCommand(Workspace workspace, String path) {
        this.path = path;
    }

    /**
     * 生成并打印目录树。
     */
    @Override
    public void execute() {
        String target = path == null ? System.getProperty("user.dir") : path;
        File root = Paths.get(target).toAbsolutePath().normalize().toFile();
        if (!root.exists() || !root.isDirectory()) {
            throw new IllegalArgumentException("目录不存在或不是有效目录: " + target);
        }
        System.out.println(DirTreeUtil.generateTree(root));
    }

    /**
     * 目录树展示命令不支持撤销。
     */
    @Override
    public void undo() {
    }

    /**
     * @return 命令名称
     */
    @Override
    public String getName() {
        return path == null ? "dir-tree" : "dir-tree " + path;
    }

    /**
     * @return 展示类命令不进入撤销栈
     */
    @Override
    public boolean isUndoable() {
        return false;
    }
}
