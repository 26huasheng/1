import os
from typing import Optional
from editor.command.base import Command
from editor.workspace.workspace import Workspace
from editor.editor.text_editor import TextEditor


class LoadCommand(Command):
    """load <file> - 加载文件到工作区"""
    def __init__(self, file_path: str):
        self.workspace = Workspace()
        self.file_path = file_path
        # 保存执行前的活动文件，便于撤销
        self._prev_active_path: Optional[str] = None
        self._file_existed_before: bool = False
        self._prev_editor_state: Optional[dict] = None

    def execute(self) -> None:
        # 记录当前活动文件，以便撤销时恢复
        self._prev_active_path = self.workspace.get_active_editor().get_file_path() if self.workspace.get_active_editor() else None
        self._file_existed_before = self.file_path in self.workspace.get_editors()
        
        self.workspace.load_file(self.file_path)

    def undo(self) -> None:
        # 如果文件已打开过，只需恢复之前的活动文件
        if self._file_existed_before:
            if self._prev_active_path:
                self.workspace.set_active_editor(self._prev_active_path)
            return
        # 若是新加载的文件，则关闭它
        if self.file_path in self.workspace.get_editors():
            self.workspace.close_file(self.file_path)
            # 恢复之前的活动文件
            if self._prev_active_path and self._prev_active_path in self.workspace.get_editors():
                self.workspace.set_active_editor(self._prev_active_path)

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"load {self.file_path}"


class SaveCommand(Command):
    """save [file|all] - 保存文件"""
    def __init__(self, target: Optional[str]):
        self.workspace = Workspace()
        self.target = target  # None=活动文件, all=所有, 其他=指定文件

    def execute(self) -> None:
        if self.target is None:
            active = self.workspace.get_active_editor()
            if not active:
                raise Exception("无活动文件可保存")
            self.workspace.save_file(active.get_file_path())
        elif self.target == "all":
            self.workspace.save_all()
        else:
            self.workspace.save_file(self.target)

    def undo(self) -> None:
        raise Exception("保存操作不可撤销（已写入磁盘）")

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"save {self.target if self.target is not None else ''}".strip()

    def is_undoable(self) -> bool:
        return False


class InitCommand(Command):
    """init <file> [with-log] - 创建新缓冲区"""
    def __init__(self, file_path: str, with_log: bool = False):
        self.workspace = Workspace()
        self.file_path = file_path
        self.with_log = with_log
        self._prev_active_path: Optional[str] = None

    def execute(self) -> None:
        self._prev_active_path = self.workspace.get_active_editor().get_file_path() if self.workspace.get_active_editor() else None
        self.workspace.init_file(self.file_path, self.with_log)

        from editor.event.bus import EventBus
        from editor.event.listener import EditorEvent
        EventBus().publish(EditorEvent("COMMAND_EXECUTE", self.get_description()))
    
    def undo(self) -> None:
        # 关闭init创建的文件
        if self.file_path in self.workspace.get_editors():
            self.workspace.close_file(self.file_path)
            # 恢复之前的活动文件
            if self._prev_active_path and self._prev_active_path in self.workspace.get_editors():
                self.workspace.set_active_editor(self._prev_active_path)

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        log_suffix = " with-log" if self.with_log else ""
        return f"init {self.file_path}{log_suffix}"


class CloseCommand(Command):
    """close [file] - 关闭文件"""
    def __init__(self, target: Optional[str]):
        self.workspace = Workspace()
        self.target = target
        # 备份被关闭的编辑器内容和状态
        self._closed_editor_state: Optional[dict] = None
        self._prev_active_path: Optional[str] = None

    def execute(self) -> None:
        # 确定要关闭的文件
        if self.target is None:
            active = self.workspace.get_active_editor()
            if not active:
                raise Exception("无活动文件可关闭")
            self.target = active.get_file_path()

        # 保存编辑器的完整状态便于撤销
        editor = self.workspace.get_editors().get(self.target)
        if not editor:
            raise Exception(f"文件未打开：{self.target}")
        
        self._closed_editor_state = {
            "file_path": self.target,
            "lines": editor.get_lines() if isinstance(editor, TextEditor) else [],
            "modified": editor.is_modified(),
            "log_enabled": editor.is_log_enabled() if isinstance(editor, TextEditor) else False
        }
        self._prev_active_path = self.workspace.get_active_editor().get_file_path() if self.workspace.get_active_editor() else None

        # 执行关闭
        self.workspace.close_file(self.target)

    def undo(self) -> None:
        if not self._closed_editor_state:
            return
        # 恢复编辑器
        state = self._closed_editor_state
        editor = TextEditor(state["file_path"])
        editor.set_lines(state["lines"])
        editor.set_modified(state["modified"])
        editor.set_log_enabled(state["log_enabled"])
        
        # 重新加入工作区
        self.workspace.editors[state["file_path"]] = editor
        # 恢复活动文件
        if self._prev_active_path == state["file_path"] or self.workspace.get_active_editor() is None:
            self.workspace.active_editor = editor

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"close {self.target if self.target is not None else ''}".strip()


class EditCommand(Command):
    """edit <file> - 切换活动文件"""
    def __init__(self, file_path: str):
        self.workspace = Workspace()
        self.file_path = file_path
        self._prev_active_path: Optional[str] = None

    def execute(self) -> None:
        self._prev_active_path = self.workspace.get_active_editor().get_file_path() if self.workspace.get_active_editor() else None
        self.workspace.set_active_editor(self.file_path)

    def undo(self) -> None:
        if self._prev_active_path and self._prev_active_path in self.workspace.get_editors():
            self.workspace.set_active_editor(self._prev_active_path)

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"edit {self.file_path}"


class EditorListCommand(Command):
    """editor-list - 显示打开的文件列表"""
    def __init__(self):
        self.workspace = Workspace()

    def execute(self) -> None:
        self.workspace.show_editor_list()

    def undo(self) -> None:
        raise Exception("显示类命令不可撤销")

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return "editor-list"

    def is_undoable(self) -> bool:
        return False


class DirTreeCommand(Command):
    """dir-tree [path] - 显示目录树"""
    def __init__(self, root_path: str = "."):
        self.root_path = os.path.abspath(root_path)

    def execute(self) -> None:
        if not os.path.exists(self.root_path):
            raise Exception(f"路径不存在：{self.root_path}")
        if not os.path.isdir(self.root_path):
            raise Exception(f"不是目录：{self.root_path}")
        
        # 递归绘制目录树
        self._print_tree(self.root_path)

    def _print_tree(self, root: str) -> None:
        """按实验要求的格式绘制目录树"""
        prefix_map = {}
        root_name = os.path.basename(root)
        print(f"{root_name}/" if os.path.isdir(root) else root_name)

        def _recurse(current_path: str, prefix: str = ""):
            items = sorted(os.listdir(current_path), key=lambda x: (not os.path.isdir(os.path.join(current_path, x)), x.lower()))
            for i, item in enumerate(items):
                full_path = os.path.join(current_path, item)
                is_last = i == len(items) - 1
                # 绘制前缀和节点
                connector = "└── " if is_last else "├── "
                print(f"{prefix}{connector}{item}")
                # 递归子目录
                if os.path.isdir(full_path):
                    new_prefix = prefix + ("    " if is_last else "│   ")
                    _recurse(full_path, new_prefix)

        _recurse(root)

    def undo(self) -> None:
        raise Exception("显示类命令不可撤销")

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return f"dir-tree {self.root_path}" if self.root_path != "." else "dir-tree"

    def is_undoable(self) -> bool:
        return False


class UndoCommand(Command):
    """undo - 撤销当前活动文件的最后一次操作"""
    def __init__(self):
        self.workspace = Workspace()

    def execute(self) -> None:
        self.workspace.undo()

    def undo(self) -> None:
        raise Exception("撤销命令本身不可撤销")

    def redo(self) -> None:
        raise Exception("撤销命令本身不可重做")

    def get_description(self) -> str:
        return "undo"

    def is_undoable(self) -> bool:
        return False


class RedoCommand(Command):
    """redo - 重做当前活动文件的最后一次撤销操作"""
    def __init__(self):
        self.workspace = Workspace()

    def execute(self) -> None:
        self.workspace.redo()

    def undo(self) -> None:
        raise Exception("重做命令本身不可撤销")

    def redo(self) -> None:
        raise Exception("重做命令本身不可重做")

    def get_description(self) -> str:
        return "redo"

    def is_undoable(self) -> bool:
        return False


class ExitCommand(Command):
    """exit - 退出程序"""
    def __init__(self):
        self.workspace = Workspace()

    def execute(self) -> None:
        self.workspace.exit()

    def undo(self) -> None:
        raise Exception("退出命令不可撤销")

    def redo(self) -> None:
        self.execute()

    def get_description(self) -> str:
        return "exit"

    def is_undoable(self) -> bool:
        return False