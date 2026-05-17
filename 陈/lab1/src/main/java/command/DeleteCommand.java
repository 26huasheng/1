package command;

import editor.IEditor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 删除命令类。
 * <p>
 * 执行时调用 editor.delete 获取被删除文本并保存，
 * 撤销时调用 editor.insert 在原位置恢复被删除的文本。
 * </p>
 */
public class DeleteCommand implements ICommand {

    /**
     * 编辑器实例引用。
     */
    private final IEditor editor;

    /**
     * 删除的起始行号（从 1 开始）。
     */
    public final int line;

    /**
     * 删除的起始列号（从 1 开始）。
     */
    public final int col;

    /**
     * 删除的字符长度。
     */
    public final int len;

    /**
     * 被删除的文本内容，由 execute 时保存，供 undo 使用。
     */
    private String deletedText;

    /**
     * 命令创建时的时间戳。
     */
    private final String timestamp;

    /**
     * 构造一个删除命令实例。
     *
     * @param editor 编辑器实例
     * @param line   删除的起始行号（从 1 开始）
     * @param col    删除的起始列号（从 1 开始）
     * @param len    删除的字符长度
     */
    public DeleteCommand(IEditor editor, int line, int col, int len) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.len = len;
        this.timestamp = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
    }

    @Override
    public void execute() {
        deletedText = editor.delete(line, col, len);
    }

    @Override
    public void undo() {
        editor.insert(line, col, deletedText);
    }

    @Override
    public String getCommandLog() {
        return timestamp + " delete " + line + ":" + col + " len=" + len;
    }
}
