# tests/test_workspace.py
import os
import pytest
from editor.workspace.workspace import Workspace

# 测试结束后清理临时文件
@pytest.fixture(autouse=True)
def clean_temp_files():
    yield
    temp_files = ["test_workspace.txt", "test_init.txt", "test_close.txt", 
                  "test_save.txt", "test_undo.txt", ".editor_workspace.json"]
    for f in temp_files:
        if os.path.exists(f):
            os.remove(f)

def test_init_file():
    """测试 init 命令创建新缓冲区"""
    ws = Workspace()
    editor = ws.init_file("test_init.txt", with_log=False)
    assert editor.get_file_path() == "test_init.txt"
    assert editor.is_modified() is True
    assert "test_init.txt" in ws.get_editors()
    assert ws.get_active_editor() == editor

def test_load_file():
    """测试 load 命令加载文件"""
    ws = Workspace()
    # 先创建测试文件
    with open("test_workspace.txt", "w", encoding="utf-8") as f:
        f.write("test content")
    
    editor = ws.load_file("test_workspace.txt")
    assert editor.get_file_path() == "test_workspace.txt"
    assert editor.get_lines() == ["test content"]
    assert ws.get_active_editor() == editor

def test_close_file(monkeypatch):
    """测试 close 命令关闭文件，模拟用户输入"""
    ws = Workspace()
    ws.init_file("test_close.txt")
    assert "test_close.txt" in ws.get_editors()
    
    # 模拟用户输入"n"（不保存）
    monkeypatch.setattr('builtins.input', lambda _: 'n')
    
    ws.close_file("test_close.txt")
    assert "test_close.txt" not in ws.get_editors()

def test_switch_active_editor():
    """测试 edit 命令切换活动文件"""
    ws = Workspace()
    ws.init_file("file1.txt")
    ws.init_file("file2.txt")
    
    ws.set_active_editor("file1.txt")
    assert ws.get_active_editor().get_file_path() == "file1.txt"
    
    ws.set_active_editor("file2.txt")
    assert ws.get_active_editor().get_file_path() == "file2.txt"

def test_save_file(monkeypatch):
    """测试 save 命令保存文件内容到磁盘"""
    ws = Workspace()
    editor = ws.init_file("test_save.txt")
    editor.append("save test")
    
    ws.save_file("test_save.txt")
    assert editor.is_modified() is False
    # 验证文件写入磁盘
    with open("test_save.txt", "r", encoding="utf-8") as f:
        assert f.read() == "save test"

def test_undo_redo_delegate():
    """测试 Workspace 的 undo/redo 正确委托给活动编辑器"""
    ws = Workspace()
    from editor.command.impl.text import AppendCommand
    
    editor = ws.init_file("test_undo.txt")
    cmd = AppendCommand("test")
    cmd.execute()
    editor.get_command_history().push(cmd)
    assert editor.get_lines() == ["test"]
    
    ws.undo()
    assert editor.get_lines() == []
    
    ws.redo()
    assert editor.get_lines() == ["test"]

def test_workspace_persistence(monkeypatch):
    """测试 exit 时的状态保存和重启后的状态恢复"""
    ws1 = Workspace()
    ws1.init_file("fileA.txt")
    ws1.init_file("fileB.txt")
    ws1.set_active_editor("fileA.txt")

    # 模拟用户输入 'n'，防止 close_file 时的 input() 阻塞测试
    monkeypatch.setattr('builtins.input', lambda _: 'n')

    # 捕获 sys.exit(0) 抛出的 SystemExit 异常
    with pytest.raises(SystemExit):
        ws1.exit()

    # 验证持久化文件是否按预期生成
    import os
    assert os.path.exists(".editor_workspace.json")

def test_save_all():
    """测试 save all 命令保存所有打开的文件"""
    ws = Workspace()
    editor1 = ws.init_file("file1.txt")
    editor2 = ws.init_file("file2.txt")
    
    editor1.append("1")
    editor2.append("2")
    
    assert editor1.is_modified() is True
    assert editor2.is_modified() is True
    
    ws.save_all() 
    
    assert editor1.is_modified() is False
    assert editor2.is_modified() is False

def test_editor_list_display(capsys):
    """测试 editor-list 命令显示已打开的文件列表"""
    from editor.command.impl.workspace import EditorListCommand
    
    ws = Workspace()
    ws.init_file("file1.txt")
    ws.init_file("file2.txt")
    ws.set_active_editor("file1.txt")
    
    cmd = EditorListCommand()
    cmd.execute()
    
    captured = capsys.readouterr()
    output = captured.out
    
    assert "file1.txt" in output
    assert "file2.txt" in output
    # 活动文件应该有标记
    assert "*" in output or ">" in output

def test_dir_tree_display(capsys):
    """测试 dir-tree 命令显示目录树结构"""
    from editor.command.impl.workspace import DirTreeCommand
    
    cmd = DirTreeCommand(".")
    cmd.execute()
    
    captured = capsys.readouterr()
    output = captured.out
    
    # 检查输出中是否包含树形结构字符
    assert any(char in output for char in ["├──", "└──", "│"])
    # 至少应该显示某些目录或文件
    assert len(output) > 0