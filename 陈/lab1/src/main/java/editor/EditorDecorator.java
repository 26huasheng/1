package editor;

import java.util.Iterator;

/**
 * 编辑器装饰器抽象基类。
 * <p>
 * 实现装饰器模式，持有 IEditor 委托对象，
 * 所有方法默认转发给委托对象执行。
 * 具体装饰器可继承此类并选择性地重写特定方法。
 * </p>
 */
public abstract class EditorDecorator implements IEditor {

    /**
     * 被装饰的编辑器委托对象。
     */
    protected IEditor delegate;

    /**
     * 构造一个编辑器装饰器实例。
     *
     * @param delegate 被装饰的编辑器对象
     */
    public EditorDecorator(IEditor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void append(String text) {
        delegate.append(text);
    }

    @Override
    public void insert(int line, int col, String text) {
        delegate.insert(line, col, text);
    }

    @Override
    public String delete(int line, int col, int len) {
        return delegate.delete(line, col, len);
    }

    @Override
    public String replace(int line, int col, int len, String text) {
        return delegate.replace(line, col, len, text);
    }

    @Override
    public Iterator<String> getLineIterator(int startLine, int endLine) {
        return delegate.getLineIterator(startLine, endLine);
    }

    @Override
    public boolean isModified() {
        return delegate.isModified();
    }

    @Override
    public void setModified(boolean modified) {
        delegate.setModified(modified);
    }

    @Override
    public String getFilePath() {
        return delegate.getFilePath();
    }
}
