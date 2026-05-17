# editor/command/history.py
from typing import List
from .base import Command

class CommandHistory:
    """命令历史（支撑 undo/redo）"""
    def __init__(self):
        self.undo_stack: List[Command] = []
        self.redo_stack: List[Command] = []

    def push(self, command: Command) -> None:
        """记录已执行的命令"""
        if command.is_undoable():
            self.undo_stack.append(command)
            self.redo_stack.clear()  # 新命令执行后清空 redo 栈

    def undo(self) -> None:
        """撤销最后一个命令"""
        if not self.undo_stack:
            print("无可撤销的操作")
            return
        command = self.undo_stack.pop()
        command.undo()
        self.redo_stack.append(command)

    def redo(self) -> None:
        """重做最后一个撤销的命令"""
        if not self.redo_stack:
            print("无可重做的操作")
            return
        command = self.redo_stack.pop()
        command.redo()
        self.undo_stack.append(command)

    def clear(self) -> None:
        """清空历史"""
        self.undo_stack.clear()
        self.redo_stack.clear()