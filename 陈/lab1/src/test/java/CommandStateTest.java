

import command.AppendCommand;
import command.CommandManager;
import command.InsertCommand;
import editor.IEditor;
import editor.TextEditor;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 命令状态机与宏命令测试类。
 * <p>
 * 验证 CommandManager 的撤销/重做链、Redo 栈清空机制
 * 以及命令执行后状态回滚的无损性。
 * </p>
 */
public class CommandStateTest {

    /**
     * 编辑器实例。
     */
    private IEditor editor;

    /**
     * 命令调度器实例。
     */
    private CommandManager cmdManager;

    @Before
    public void setUp() {
        editor = new TextEditor();
        cmdManager = new CommandManager();
    }

    /**
     * 测试撤销/重做链的完整性。
     * <p>
     * 执行序列：append("Hello") -> insert(2,1,"World") -> undo -> undo -> redo。
     * 最终断言文本内容严格等于单次 "Hello"，证明 Redo 栈和指针回滚无损。
     * </p>
     */
    @Test
    public void testUndoRedoChain() {
        cmdManager.executeCommand(new AppendCommand(editor, "Hello"), msg -> {});
        cmdManager.executeCommand(new InsertCommand(editor, 2, 1, "World"), msg -> {});

        cmdManager.undo();
        cmdManager.undo();

        cmdManager.redo();

        List<String> result = new ArrayList<>();
        Iterator<String> it = editor.getLineIterator(1, 1);
        while (it.hasNext()) {
            result.add(it.next());
        }

        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0));
    }

    /**
     * 测试执行新命令后重做栈应被清空。
     * <p>
     * 执行命令后 undo，再执行新命令，此时 redo 应抛出异常。
     * </p>
     */
    @Test
    public void testRedoStackClearedOnNewCommand() {
        cmdManager.executeCommand(new AppendCommand(editor, "Line1"), msg -> {});
        cmdManager.executeCommand(new AppendCommand(editor, "Line2"), msg -> {});

        cmdManager.undo();

        cmdManager.executeCommand(new AppendCommand(editor, "Line3"), msg -> {});

        assertThrows(core.EditorException.class, () -> {
            cmdManager.redo();
        });
    }

    /**
     * 测试空撤销栈应抛出异常。
     */
    @Test
    public void testUndoEmptyStack() {
        assertThrows(core.EditorException.class, () -> {
            cmdManager.undo();
        });
    }

    /**
     * 测试空重做栈应抛出异常。
     */
    @Test
    public void testRedoEmptyStack() {
        assertThrows(core.EditorException.class, () -> {
            cmdManager.redo();
        });
    }

    /**
     * 测试多次撤销后文本应恢复为空。
     */
    @Test
    public void testMultipleUndoRestoresEmptyState() {
        cmdManager.executeCommand(new AppendCommand(editor, "Line1"), msg -> {});
        cmdManager.executeCommand(new AppendCommand(editor, "Line2"), msg -> {});

        cmdManager.undo();
        cmdManager.undo();

        assertEquals(0, getLineCount(editor));
    }

    /**
     * 获取编辑器的总行数。
     *
     * @param editor 编辑器实例
     * @return 总行数
     */
    private int getLineCount(IEditor editor) {
        int count = 0;
        try {
            Iterator<String> it = editor.getLineIterator(1, Integer.MAX_VALUE);
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
