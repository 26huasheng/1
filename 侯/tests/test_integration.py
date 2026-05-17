# tests/test_integration.py
from editor.workspace.workspace import Workspace
from editor.command.impl.text import AppendCommand, InsertCommand

def test_editor_independent_undo_redo():
    """集成测试：验证每个 Editor 独立维护 undo/redo 状态"""
    ws = Workspace()
    
    # 初始化两个编辑器
    editor_a = ws.init_file("a.txt")
    editor_b = ws.init_file("b.txt")

    # 在 a.txt 执行 append 操作
    ws.set_active_editor("a.txt")
    cmd_a = AppendCommand("Hello A")
    cmd_a.execute()
    editor_a.get_command_history().push(cmd_a)
    assert editor_a.get_lines() == ["Hello A"]

    # 在 b.txt 执行 append 操作
    ws.set_active_editor("b.txt")
    cmd_b = AppendCommand("Hello B")
    cmd_b.execute()
    editor_b.get_command_history().push(cmd_b)
    assert editor_b.get_lines() == ["Hello B"]

    # 切换回 a.txt 执行 undo：只影响 a.txt
    ws.set_active_editor("a.txt")
    ws.undo()
    assert editor_a.get_lines() == []  # a.txt 已撤销
    assert editor_b.get_lines() == ["Hello B"]  # b.txt 不受影响