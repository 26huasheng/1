package command;

import editor.IEditor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 追加命令类。
 * <p>
 * 执行时在文本末尾追加指定内容，撤销时从尾部精准删除
 * 当初追加的所有行。
 * </p>
 */
public class AppendCommand implements ICommand {

    /**
     * 编辑器实例引用。
     */
    private final IEditor editor;

    /**
     * 要追加的文本内容。
     */
    private final String text;

    /**
     * 命令创建时的时间戳。
     */
    private final String timestamp;

    /**
     * 记录追加文本拆分后的行数，用于撤销时精准删除。
     */
    private int appendedLineCount;

    /**
     * 构造一个追加命令实例。
     *
     * @param editor 编辑器实例
     * @param text   要追加的文本内容
     */
    public AppendCommand(IEditor editor, String text) {
        this.editor = editor;
        this.text = text;
        this.timestamp = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
        this.appendedLineCount = text.contains("\n") ? text.split("\n", -1).length : 1;
    }

    @Override
    public void execute() {
        editor.append(text);
    }

    @Override
    public void undo() {
        for (int i = 0; i < appendedLineCount; i++) {
            int lineCount = getLineCount();
            if (lineCount > 0) {
                String lastLine = editor.getLineIterator(lineCount, lineCount).next();
                editor.delete(lineCount, 1, lastLine.length());
            }
        }
    }

    @Override
    public String getCommandLog() {
        return timestamp + " append \"" + text + "\"";
    }

    /**
     * 获取当前编辑器的总行数。
     *
     * @return 总行数
     */
    private int getLineCount() {
        int count = 0;
        try {
            var it = editor.getLineIterator(1, Integer.MAX_VALUE);
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
