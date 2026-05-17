package editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 拼写检查装饰器。
 * <p>
 * 继承自 EditorDecorator，重写 append 和 insert 方法，
 * 在调用委托对象对应方法后，使用正则表达式检查传入文本中
 * 是否包含连续三个或以上的辅音字母组合。
 * 若匹配到疑似拼写错误，仅打印警告信息，不抛出异常阻断业务。
 * </p>
 */
public class SpellCheckDecorator extends EditorDecorator {

    /**
     * 检查连续三个或以上辅音字母的正则表达式（忽略大小写）。
     */
    // private static final Pattern SPELL_CHECK_PATTERN = Pattern.compile("(?i)([bcdfghjklmnpqrstvwxyz])\\1{2,}");
    private static final Pattern SPELL_CHECK_PATTERN = Pattern.compile("(?i)[bcdfghjklmnpqrstvwxyz]{3,}");

    /**
     * 构造一个拼写检查装饰器实例。
     *
     * @param delegate 被装饰的编辑器对象
     */
    public SpellCheckDecorator(IEditor delegate) {
        super(delegate);
    }

    @Override
    public void append(String text) {
        delegate.append(text);
        checkSpelling(text);
    }

    @Override
    public void insert(int line, int col, String text) {
        delegate.insert(line, col, text);
        checkSpelling(text);
    }

    /**
     * 使用正则表达式检查文本中是否包含疑似拼写错误。
     * <p>
     * 仅打印警告信息到控制台，绝对不抛出异常阻断业务流程。
     * </p>
     *
     * @param text 要检查的文本内容
     */
    private void checkSpelling(String text) {
        Matcher matcher = SPELL_CHECK_PATTERN.matcher(text);
        if (matcher.find()) {
            System.out.println("[Warning] 疑似拼写错误: " + text);
        }
    }
}
