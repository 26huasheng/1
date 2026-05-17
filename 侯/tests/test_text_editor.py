# tests/test_text_editor.py
import pytest
from editor.editor.text_editor import TextEditor

def test_append():
    editor = TextEditor("test.txt")
    editor.append("Hello World")
    assert len(editor.get_lines()) == 1
    assert editor.get_lines()[0] == "Hello World"

def test_insert():
    editor = TextEditor("test.txt")
    editor.append("abcdef")
    editor.insert(1, 4, "XYZ")
    assert editor.get_lines()[0] == "abcXYZdef"

def test_delete():
    editor = TextEditor("test.txt")
    editor.append("Hello World")
    editor.delete(1, 7, 5)
    assert editor.get_lines()[0] == "Hello "

def test_replace():
    editor = TextEditor("test.txt")
    editor.append("fast fox")
    # 替换第一行前 4 个字符
    editor.replace(1, 1, 4, "slow")
    assert editor.get_lines()[0] == "slow fox"

def test_insert_multiline():
    """测试 insert 命令处理包含换行符的文本"""
    editor = TextEditor("test.txt")
    editor.append("line1")
    editor.insert(1, 6, "\nline2")
    lines = editor.get_lines()
    assert len(lines) == 2
    assert lines[0] == "line1"
    assert lines[1] == "line2"

def test_boundaries_and_exceptions():
    """测试异常处理：行号越界、列号超限、删除长度溢出"""
    editor = TextEditor("test.txt")
    
    # 行号越界时抛出 Exception
    with pytest.raises(Exception):
        editor.insert(2, 1, "out of bounds")
        
    editor.append("hello")
    # 删除长度超过行尾时抛出 Exception
    with pytest.raises(Exception):
        editor.delete(1, 1, 10)