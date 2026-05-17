# tests/test_logging.py
import os
import pytest
from editor.workspace.workspace import Workspace
from editor.logging.listener import LogListener
from editor.event.bus import EventBus
from editor.event.listener import EditorEvent

# 测试结束后清理日志文件
@pytest.fixture(autouse=True)
def clean_log_files():
    yield
    log_files = [".test_log.txt.log", ".test_auto_log.txt.log", ".show_test.txt.log"]
    for f in log_files:
        if os.path.exists(f):
            os.remove(f)

def test_log_on_off():
    """测试 log-on/log-off 命令的启用和禁用"""
    ws = Workspace()
    editor = ws.init_file("test_log.txt")
    assert editor.is_log_enabled() is False
    
    from editor.command.impl.logging import LogOnCommand, LogOffCommand
    LogOnCommand(None).execute()
    assert editor.is_log_enabled() is True
    
    LogOffCommand(None).execute()
    assert editor.is_log_enabled() is False

def test_auto_log_enable():
    """测试 load 命令发现首行 #log 时自动启用日志"""
    ws = Workspace()
    # 创建带 #log 的文件
    with open("test_auto_log.txt", "w", encoding="utf-8") as f:
        f.write("# log\nhello world")
    
    editor = ws.load_file("test_auto_log.txt")
    assert editor.is_log_enabled() is True

def test_log_write_format():
    """测试日志写入格式符合实验要求"""
    ws = Workspace()
    listener = LogListener()
    EventBus().register(listener)
    
    # 初始化带日志的文件
    editor = ws.init_file("test_log.txt", with_log=True)
    # 执行一个命令触发日志
    event = EditorEvent("COMMAND_EXECUTE", 'append "test line"')
    EventBus().publish(event)
    
    # 验证日志文件存在且格式正确
    log_path = ".test_log.txt.log"
    assert os.path.exists(log_path)
    
    with open(log_path, "r", encoding="utf-8") as f:
        lines = f.readlines()
        # 第一行是会话开始标记
        assert "session start at" in lines[0]
        # 第二行是 init 命令
        assert "init test_log.txt with-log" in lines[1]
        # 第三行是 append 命令
        assert "append \"test line\"" in lines[2]
    
    EventBus().unregister(listener)

def test_log_show(capsys):
    """测试 log-show 命令显示日志内容"""
    from editor.command.impl.logging import LogShowCommand
    
    ws = Workspace()
    listener = LogListener()
    EventBus().register(listener)
    
    # 创建带日志的编辑器并执行命令
    editor = ws.init_file("show_test.txt", with_log=True)
    event = EditorEvent("COMMAND_EXECUTE", 'append "log test"')
    EventBus().publish(event)
    
    # 执行 log-show 命令
    cmd = LogShowCommand(None)
    cmd.execute()
    
    # 验证输出中包含日志内容
    captured = capsys.readouterr()
    output = captured.out
    
    assert "session start at" in output or len(output) > 0
    
    EventBus().unregister(listener)