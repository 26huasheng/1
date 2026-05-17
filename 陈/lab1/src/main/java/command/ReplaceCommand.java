package command;

import editor.IEditor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 替换命令类（宏命令）。
 * <p>
 * 内部组合了一个 DeleteCommand 和一个 InsertCommand，
 * 执行时先执行 delete 再执行 insert，撤销时严格逆序：
 * 先执行 insert.undo()，再执行 delete.undo()。
 * </p>
 */
public class ReplaceCommand implements ICommand {

    /**
     * 内部删除命令。
     */
    private final DeleteCommand deleteCommand;

    /**
     * 内部插入命令。
     */
    private final InsertCommand insertCommand;

    /**
     * 命令创建时的时间戳。
     */
    private final String timestamp;

    /**
     * 替换的新文本内容。
     */
    private final String newText;

    /**
     * 构造一个替换命令实例（宏命令）。
     *
     * @param editor 编辑器实例
     * @param line   替换的起始行号（从 1 开始）
     * @param col    替换的起始列号（从 1 开始）
     * @param len    要替换的字符长度
     * @param text   新的文本内容
     */
    public ReplaceCommand(IEditor editor, int line, int col, int len, String text) {
        this.timestamp = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
        this.newText = text;
        this.deleteCommand = new DeleteCommand(editor, line, col, len);
        this.insertCommand = new InsertCommand(editor, line, col, text);
    }

    @Override
    public void execute() {
        deleteCommand.execute();
        insertCommand.execute();
    }

    @Override
    public void undo() {
        insertCommand.undo();
        deleteCommand.undo();
    }

    @Override
    public String getCommandLog() {
        return timestamp + " replace " + deleteCommand.line + ":" + deleteCommand.col + " " + deleteCommand.len + " \"" + newText + "\"";
    }
}
