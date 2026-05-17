package com.editor.command;

import com.editor.core.TextEditor;
import com.editor.core.Workspace;

/**
 * 文本修改类命令的抽象基类。
 * 统一处理“执行前保存旧状态”和“撤销时恢复旧状态”的模板逻辑。
 */
public abstract class AbstractTextChangeCommand implements Command {
    protected final Workspace workspace;
    private String fileName;
    private String previousContent;
    private boolean previousModified;

    protected AbstractTextChangeCommand(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    /**
     * 模板方法：先快照旧内容，再执行具体文本修改。
     */
    public final void execute() {
        TextEditor editor = requireEditor();
        fileName = editor.getFileName();
        previousContent = editor.getContentAsString();
        previousModified = editor.isModified();
        apply(editor);
        workspace.markActiveFileModified();
    }

    @Override
    /**
     * 将编辑器恢复到命令执行前的内容与修改状态。
     */
    public final void undo() {
        TextEditor editor = (TextEditor) workspace.getEditorByFileName(fileName);
        if (editor == null) {
            throw new IllegalStateException("无法撤销，文件未打开: " + fileName);
        }
        editor.restoreFromString(previousContent, previousModified);
        if (fileName.equals(workspace.getActiveFileName())) {
            if (previousModified) {
                workspace.markActiveFileModified();
            } else {
                workspace.markActiveFileSaved();
            }
        }
    }

    /**
     * 子类只需实现具体的文本变更逻辑。
     *
     * @param editor 当前活动的文本编辑器
     */
    protected abstract void apply(TextEditor editor);

    /**
     * 获取当前活动文本编辑器。
     *
     * @return 当前活动的 TextEditor
     */
    protected TextEditor requireEditor() {
        if (!(workspace.getActiveEditor() instanceof TextEditor editor)) {
            throw new IllegalStateException("当前活动文件不是文本编辑器");
        }
        return editor;
    }
}
