package com.editor.command.impl;

import com.editor.command.AbstractTextChangeCommand;
import com.editor.core.TextEditor;
import com.editor.core.Workspace;

/**
 * 在文本末尾追加一整行内容。
 */
public class AppendCommand extends AbstractTextChangeCommand {
    private final String text;

    /**
     * 构造追加命令。
     *
     * @param workspace 工作区
     * @param text 追加文本
     */
    public AppendCommand(Workspace workspace, String text) {
        super(workspace);
        this.text = text;
    }

    /**
     * 执行追加逻辑。
     *
     * @param editor 当前文本编辑器
     */
    @Override
    protected void apply(TextEditor editor) {
        editor.appendLine(text);
    }

    /**
     * @return 便于日志记录的命令文本
     */
    @Override
    public String getName() {
        return "append \"" + text + "\"";
    }
}
