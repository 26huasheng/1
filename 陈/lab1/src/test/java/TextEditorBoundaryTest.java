

import core.EditorException;
import editor.IEditor;
import editor.TextEditor;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 文本编辑器边界测试类。
 * <p>
 * 严格验证编辑器的越界拦截、多行插入拆分等核心边界逻辑，
 * 所有测试均基于内存 TextEditor，无任何磁盘 I/O 副作用。
 * </p>
 */
public class TextEditorBoundaryTest {

    /**
     * 待测试的空编辑器实例。
     */
    private IEditor editor;

    @Before
    public void setUp() {
        editor = new TextEditor();
    }

    /**
     * 测试空文件删除操作应被越界异常拦截。
     * <p>
     * 在空文件上执行 delete(1,1,5) 应抛出 EditorException。
     * </p>
     */
    @Test
    public void testDeleteOutOfBounds() {
        assertThrows(EditorException.class, () -> {
            editor.delete(1, 1, 5);
        });
    }

    /**
     * 测试多行插入时底层应准确拆分为多行。
     * <p>
     * 在 1:1 位置插入 "A\nB"，验证底层 lines 列表准确拆分为两行。
     * </p>
     */
    @Test
    public void testInsertMultiline() {
        editor.insert(1, 1, "A\nB");

        List<String> result = new ArrayList<>();
        Iterator<String> it = editor.getLineIterator(1, 2);
        while (it.hasNext()) {
            result.add(it.next());
        }

        assertEquals(2, result.size());
        assertEquals("A", result.get(0));
        assertEquals("B", result.get(1));
    }

    /**
     * 测试空文件只能在 1:1 位置插入。
     */
    @Test
    public void testInsertInEmptyFileAtWrongPosition() {
        assertThrows(EditorException.class, () -> {
            editor.insert(2, 1, "text");
        });

        assertThrows(EditorException.class, () -> {
            editor.insert(1, 2, "text");
        });
    }

    /**
     * 测试列号越界应被正确拦截。
     */
    @Test
    public void testColumnOutOfBounds() {
        editor.insert(1, 1, "Hello");

        assertThrows(EditorException.class, () -> {
            editor.insert(1, 7, "X");
        });

        assertThrows(EditorException.class, () -> {
            editor.delete(1, 7, 1);
        });
    }

    /**
     * 测试删除长度超出行尾应被正确拦截。
     */
    @Test
    public void testDeleteLengthExceedsLineEnd() {
        editor.insert(1, 1, "Hello");

        assertThrows(EditorException.class, () -> {
            editor.delete(1, 3, 10);
        });
    }

    /**
     * 测试行号越界应被正确拦截。
     */
    @Test
    public void testLineOutOfBounds() {
        editor.insert(1, 1, "Hello");

        assertThrows(EditorException.class, () -> {
            editor.insert(3, 1, "X");
        });

        assertThrows(EditorException.class, () -> {
            editor.delete(3, 1, 1);
        });
    }
}
