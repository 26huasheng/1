# editor/cli.py
import shlex
from typing import Optional
from editor.workspace.workspace import Workspace
from editor.event.bus import EventBus
from editor.event.listener import EditorEvent
from editor.logging.listener import LogListener
# 导入所有命令
from editor.command.impl.workspace import (
    LoadCommand, SaveCommand, InitCommand, CloseCommand, EditCommand,
    EditorListCommand, DirTreeCommand, UndoCommand, RedoCommand, ExitCommand
)
from editor.command.impl.text import (
    AppendCommand, InsertCommand, DeleteCommand, ReplaceCommand, ShowCommand
)
from editor.command.impl.logging import (
    LogOnCommand, LogOffCommand, LogShowCommand
)

class CommandLineInterface:
    def __init__(self):
        self.workspace = Workspace()
        self.log_listener = LogListener()
        EventBus().register(self.log_listener)

    def start(self) -> None:
        print("=== 文本编辑器 v1.0 (Python) ===")
        print("输入 'help' 查看命令列表，'exit' 退出程序")
        while True:
            try:
                active = self.workspace.get_active_editor()
                prompt = f"[{active.get_file_path()}] > " if active else "> "
                input_str = input(prompt).strip()
                if not input_str:
                    continue

                # 使用 shlex 解析带引号的参数
                parts = shlex.split(input_str)
                cmd = self._parse_command(parts)
                if cmd:
                    cmd.execute()
                    # 记录到当前活动编辑器的 history
                    active_editor = self.workspace.get_active_editor()
                    if active_editor and cmd.is_undoable():
                        active_editor.get_command_history().push(cmd)
                    # 发布事件通知日志监听器记录命令
                    EventBus().publish(EditorEvent("COMMAND_EXECUTE", cmd.get_description()))
            except Exception as e:
                print(f"错误：{e}")

    def _parse_command(self, parts: list[str]) -> Optional[object]:
        """解析命令（完整18个命令）"""
        cmd_name = parts[0].lower()
        args = parts[1:]

        # 工作区命令（10个）
        if cmd_name == "load":
            if len(args) != 1:
                raise Exception("用法：load <file>")
            return LoadCommand(args[0])
        elif cmd_name == "save":
            return SaveCommand(args[0] if args else None)
        elif cmd_name == "init":
            with_log = "with-log" in args
            file_path = args[0] if args and args[0] != "with-log" else (args[1] if len(args) > 1 else "")
            if not file_path:
                raise Exception("用法：init <file> [with-log]")
            return InitCommand(file_path, with_log)
        elif cmd_name == "close":
            return CloseCommand(args[0] if args else None)
        elif cmd_name == "edit":
            if len(args) != 1:
                raise Exception("用法：edit <file>")
            return EditCommand(args[0])
        elif cmd_name == "editor-list":
            return EditorListCommand()
        elif cmd_name == "dir-tree":
            return DirTreeCommand(args[0] if args else ".")
        elif cmd_name == "undo":
            return UndoCommand()
        elif cmd_name == "redo":
            return RedoCommand()
        elif cmd_name == "exit":
            return ExitCommand()

        # 文本编辑命令（5个）
        elif cmd_name == "append":
            if len(args) != 1:
                raise Exception("用法：append \"text\"")
            return AppendCommand(args[0])
        elif cmd_name == "insert":
            if len(args) != 2:
                raise Exception("用法：insert <line:col> \"text\"")
            return InsertCommand(args[0], args[1])
        elif cmd_name == "delete":
            if len(args) != 2:
                raise Exception("用法：delete <line:col> <len>")
            return DeleteCommand(args[0], args[1])
        elif cmd_name == "replace":
            if len(args) != 3:
                raise Exception("用法：replace <line:col> <len> \"text\"")
            return ReplaceCommand(args[0], args[1], args[2])
        elif cmd_name == "show":
            return ShowCommand(args[0] if args else "")

        # 日志命令（3个）
        elif cmd_name == "log-on":
            return LogOnCommand(args[0] if args else None)
        elif cmd_name == "log-off":
            return LogOffCommand(args[0] if args else None)
        elif cmd_name == "log-show":
            return LogShowCommand(args[0] if args else None)

        elif cmd_name == "help":
            self._show_help()
            return None
        else:
            raise Exception(f"未知命令：{cmd_name}")

    def _show_help(self) -> None:
        print("=== 命令列表 ===")
        print("【工作区命令】")
        print("  load <file>                - 加载文件")
        print("  save [file|all]            - 保存文件")
        print("  init <file> [with-log]     - 创建新缓冲区")
        print("  close [file]                - 关闭文件")
        print("  edit <file>                 - 切换活动文件")
        print("  editor-list                 - 显示文件列表")
        print("  dir-tree [path]             - 显示目录树")
        print("  undo                        - 撤销")
        print("  redo                        - 重做")
        print("  exit                        - 退出程序")
        print("\n【文本编辑命令】")
        print("  append \"text\"               - 追加文本")
        print("  insert <line:col> \"text\"    - 插入文本")
        print("  delete <line:col> <len>     - 删除字符")
        print("  replace <line:col> <len> \"text\" - 替换字符")
        print("  show [start:end]            - 显示内容")
        print("\n【日志命令】")
        print("  log-on [file]               - 启用日志")
        print("  log-off [file]              - 关闭日志")
        print("  log-show [file]             - 显示日志")