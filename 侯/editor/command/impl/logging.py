from typing import Optional
from editor.command.base import Command
from editor.workspace.workspace import Workspace
from editor.editor.text_editor import TextEditor
from editor.logging.listener import LogListener


class LogOnCommand(Command):
    """log-on [file] - 启用日志记录"""
    def __init__(self, target: Optional[str]):
        self.workspace = Workspace()
        self.target = target

    def execute(self) -> None:
        # 确定目标文件
        if self.target is None:
            editor = self.workspace.get_active_editor()
            if not editor:
                raise Exception("无活动文件")
        else:
            editor = self.workspace.get_editors().get(self.target)
            if not editor:
                raise Exception(f"文件未打开：{self.target}")

        # 启用日志
        if isinstance(editor, TextEditor):
            editor.set_log_enabled(True)
        self.workspace.set_log_global_enabled(True)

    def undo(self) -> None:
        raise Exception("日志开关操作不可撤销")

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"log-on {self.target if self.target is not None else ''}".strip()

    def is_undoable(self) -> bool:
        return False


class LogOffCommand(Command):
    """log-off [file] - 关闭日志记录"""
    def __init__(self, target: Optional[str]):
        self.workspace = Workspace()
        self.target = target

    def execute(self) -> None:
        # 确定目标文件
        if self.target is None:
            editor = self.workspace.get_active_editor()
            if not editor:
                raise Exception("无活动文件")
        else:
            editor = self.workspace.get_editors().get(self.target)
            if not editor:
                raise Exception(f"文件未打开：{self.target}")

        # 关闭日志
        if isinstance(editor, TextEditor):
            editor.set_log_enabled(False)
        # 所有文件都关闭日志时，关闭全局开关
        all_log_off = all(
            not e.is_log_enabled() for e in self.workspace.get_editors().values()
            if isinstance(e, TextEditor)
        )
        if all_log_off:
            self.workspace.set_log_global_enabled(False)

    def undo(self) -> None:
        raise Exception("日志开关操作不可撤销")

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"log-off {self.target if self.target is not None else ''}".strip()

    def is_undoable(self) -> bool:
        return False


class LogShowCommand(Command):
    """log-show [file] - 显示日志记录"""
    def __init__(self, target: Optional[str]):
        self.workspace = Workspace()
        self.target = target

    def execute(self) -> None:
        # 确定目标文件
        if self.target is None:
            editor = self.workspace.get_active_editor()
            if not editor:
                raise Exception("无活动文件")
            self.target = editor.get_file_path()
        else:
            if self.target not in self.workspace.get_editors():
                raise Exception(f"文件未打开：{self.target}")

        # 显示日志
        LogListener().show_log(self.target)

    def undo(self) -> None:
        raise Exception("显示类命令不可撤销")

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"log-show {self.target if self.target is not None else ''}".strip()

    def is_undoable(self) -> bool:
        return False