package command;

import editor.IEditor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 插入命令类。
 * <p>
 * 执行时在指定位置插入文本，撤销时利用插入文本计算出的
 * 实际字符长度，调用 delete 删除它。
 * </p>
 */
public class InsertCommand implements ICommand {

    /**
     * 编辑器实例引用。
     */
    private final IEditor editor;

    /**
     * 插入的行号（从 1 开始）。
     */
    private final int line;

    /**
     * 插入的列号（从 1 开始）。
     */
    private final int col;

    /**
     * 要插入的文本内容。
     */
    private final String text;

    /**
     * 命令创建时的时间戳。
     */
    private final String timestamp;

    /**
     * 构造一个插入命令实例。
     *
     * @param editor 编辑器实例
     * @param line   插入的行号（从 1 开始）
     * @param col    插入的列号（从 1 开始）
     * @param text   要插入的文本内容
     */
    public InsertCommand(IEditor editor, int line, int col, String text) {
        this.editor = editor;
        this.line = line;
        this.col = col;
        this.text = text;
        this.timestamp = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
    }

    @Override
    public void execute() {
        editor.insert(line, col, text);
    }

    @Override
    public void undo() {
        int totalLength = text.length();
        editor.delete(line, col, totalLength);
    }

    @Override
    public String getCommandLog() {
        return timestamp + " insert " + line + ":" + col + " \"" + text + "\"";
    }
}
