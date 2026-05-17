# editor/logging/listener.py（最终完美版）
import os
from datetime import datetime
from typing import Optional
from editor.event.listener import EditorEvent, EventListener
from editor.editor.text_editor import TextEditor
from editor.workspace.workspace import Workspace

class LogListener(EventListener):
    _TIMESTAMP_FORMAT = "%Y%m%d %H:%M:%S"

    def __init__(self):
        self._session_started = set()

    def on_event(self, event: EditorEvent) -> None:
        workspace = Workspace()
        if not workspace.is_log_global_enabled():
            return

        file_path = self._get_target_file_path(event)
        if not file_path:
            return

        editor = workspace.get_editors().get(file_path)
        if not (isinstance(editor, TextEditor) and editor.is_log_enabled()):
            return

        log_path = f".{os.path.basename(file_path)}.log"
        cmd_desc = event.data

        # 过滤掉所有显示类命令和无意义命令
        if (cmd_desc.startswith("show") or 
            cmd_desc.startswith("editor-list") or 
            cmd_desc.startswith("dir-tree") or
            cmd_desc.startswith("log-show") or
            cmd_desc == file_path):  # 过滤掉奇怪的单独文件名
            return

        try:
            # 每个文件只写一次 session start
            if file_path not in self._session_started:
                with open(log_path, "a", encoding="utf-8") as f:
                    f.write(f"session start at {datetime.now().strftime(self._TIMESTAMP_FORMAT)}\n")
                self._session_started.add(file_path)

            # 写入命令
            with open(log_path, "a", encoding="utf-8") as f:
                ts = event.timestamp.strftime(self._TIMESTAMP_FORMAT)
                f.write(f"{ts} {cmd_desc}\n")

        except Exception:
            pass

    def _get_target_file_path(self, event: EditorEvent) -> Optional[str]:
        workspace = Workspace()
        active = workspace.get_active_editor()
        return active.get_file_path() if active else None

    def show_log(self, file_path: str) -> None:
        log_path = f".{os.path.basename(file_path)}.log"
        if not os.path.exists(log_path):
            print("无日志")
            return
        with open(log_path, "r", encoding="utf-8") as f:
            print(f.read().strip())