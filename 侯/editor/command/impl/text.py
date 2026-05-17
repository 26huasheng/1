from abc import abstractmethod
from typing import Optional
from editor.command.base import Command
from editor.editor.text_editor import TextEditor
from editor.workspace.workspace import Workspace


class TextEditCommand(Command):
    """所有修改文本内容的命令基类：自动保存快照"""
    def __init__(self):
        self.workspace = Workspace()
        self._editor: Optional[TextEditor] = None
        self._before_snapshot: Optional[list[str]] = None  # 执行前的全文快照
        self._before_modified: Optional[bool] = None        # 执行前的modified状态

    def _get_editor(self) -> TextEditor:
        editor = self.workspace.get_active_editor()
        if not isinstance(editor, TextEditor):
            raise Exception("无活动文本编辑器")
        return editor

    def execute(self) -> None:
        self._editor = self._get_editor()
        # 保存执行前的状态
        self._before_snapshot = self._editor.get_lines().copy()
        self._before_modified = self._editor.is_modified()
        # 执行具体编辑逻辑
        self._do_execute()

    @abstractmethod
    def _do_execute(self) -> None:
        """子类实现具体编辑逻辑"""
        pass

    def undo(self) -> None:
        if not self._editor or self._before_snapshot is None:
            return
        # 恢复快照
        self._editor.set_lines(self._before_snapshot)
        self._editor.set_modified(self._before_modified)

    def redo(self) -> None:
        self.execute()


class AppendCommand(TextEditCommand):
    def __init__(self, text: str):
        super().__init__()
        self.text = text.strip('"')

    def _do_execute(self) -> None:
        self._editor.append(self.text)

    def get_description(self) -> str:
        return f'append "{self.text}"'


class InsertCommand(TextEditCommand):
    def __init__(self, position: str, text: str):
        super().__init__()
        # 解析line:col
        line_str, col_str = position.split(":")
        self.line = int(line_str)
        self.col = int(col_str)
        self.text = text.strip('"')

    def _do_execute(self) -> None:
        self._editor.insert(self.line, self.col, self.text)

    def get_description(self) -> str:
        return f"insert {self.line}:{self.col} \"{self.text}\""


class DeleteCommand(TextEditCommand):
    def __init__(self, position: str, length: str):
        super().__init__()
        line_str, col_str = position.split(":")
        self.line = int(line_str)
        self.col = int(col_str)
        self.length = int(length)

    def _do_execute(self) -> None:
        self._editor.delete(self.line, self.col, self.length)

    def get_description(self) -> str:
        return f"delete {self.line}:{self.col} {self.length}"


class ReplaceCommand(TextEditCommand):
    def __init__(self, position: str, length: str, text: str):
        super().__init__()
        line_str, col_str = position.split(":")
        self.line = int(line_str)
        self.col = int(col_str)
        self.length = int(length)
        self.text = text.strip('"')

    def _do_execute(self) -> None:
        self._editor.replace(self.line, self.col, self.length, self.text)

    def get_description(self) -> str:
        return f"replace {self.line}:{self.col} {self.length} \"{self.text}\""


class ShowCommand(Command):
    def __init__(self, range_str: str = ""):
        self.workspace = Workspace()
        self.range_str = range_str.strip()

    def execute(self) -> None:
        editor = self.workspace.get_active_editor()
        if not isinstance(editor, TextEditor):
            raise Exception("无活动文本编辑器")
        
        if not self.range_str:
            editor.show()
        else:
            start_str, end_str = self.range_str.split(":")
            editor.show(int(start_str), int(end_str))

    def undo(self) -> None:
        raise Exception("显示命令不可撤销")

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"show {self.range_str}".strip()

    def is_undoable(self) -> bool:
        return False