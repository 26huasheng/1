from typing import Dict, Optional
from editor.editor.base import Editor
from editor.editor.text_editor import TextEditor
from editor.event.bus import EventBus
from editor.event.listener import EditorEvent
from editor.persistence.workspace import save_workspace_state, load_workspace_state


class Workspace:
    """工作区（单例模式）"""
    _instance: "Workspace | None" = None

    def __new__(cls) -> "Workspace":
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._reset()
        return cls._instance

    def _reset(self) -> None:
        """恢复工作区初始状态"""
        self.editors: Dict[str, Editor] = {}
        self.active_editor: Optional[Editor] = None
        self.log_global_enabled = True
        self.event_bus = EventBus()
        self._restore_state()

    def _restore_state(self) -> None:
        """从持久化数据恢复工作区状态"""
        state = load_workspace_state()
        if not state:
            return

        # 恢复打开的文件
        for path in state.get("open_files", []):
            try:
                self.load_file(path)
                if path in state.get("modified", {}):
                    editor = self.editors.get(path)
                    if editor:
                        editor.set_modified(state["modified"][path])
            except Exception as e:
                print(f"恢复文件失败：{path} - {e}")
        # 恢复活动文件
        active_file = state.get("active_file")
        if active_file and active_file in self.editors:
            self.active_editor = self.editors[active_file]
        # 恢复日志开关
        self.log_global_enabled = state.get("log_enabled", True)

    def _save_state(self) -> None:
        """提取当前状态并保存"""
        open_files = list(self.editors.keys())
        active_file = self.active_editor.get_file_path() if self.active_editor else None
        modified = {path: editor.is_modified() for path, editor in self.editors.items()}
        log_enabled = self.log_global_enabled
        save_workspace_state(open_files, active_file, modified, log_enabled)

    # ===== 核心操作 =====
    def load_file(self, file_path: str) -> Editor:
        """加载文件"""
        if file_path in self.editors:
            self.active_editor = self.editors[file_path]
            self.event_bus.publish(EditorEvent("LOAD_FILE", file_path))
            return self.active_editor

        editor = TextEditor(file_path)
        editor.load_content()
        self.editors[file_path] = editor
        self.active_editor = editor
        self.event_bus.publish(EditorEvent("LOAD_FILE", file_path))
        return editor

    def init_file(self, file_path: str, with_log: bool = False) -> Editor:
    
        if file_path in self.editors:
            raise Exception(f"文件已存在于工作区：{file_path}")

        editor = TextEditor(file_path)
    # Lab1 要求：with-log 自动添加 # log 首行
        if with_log:
            editor.append("# log")
            editor.set_log_enabled(True)
        editor.set_modified(True)
        self.editors[file_path] = editor
        self.active_editor = editor

    #  发布事件
        from editor.event.bus import EventBus
        from editor.event.listener import EditorEvent
        cmd_str = f"init {file_path}{' with-log' if with_log else ''}"
        EventBus().publish(EditorEvent("COMMAND_EXECUTE", cmd_str))

        return editor

    def save_file(self, file_path: str) -> None:
        """保存文件"""
        editor = self.editors.get(file_path)
        if not editor:
            raise Exception(f"文件未打开：{file_path}")
        editor.save_content()
        self.event_bus.publish(EditorEvent("SAVE_FILE", file_path))

    def save_all(self) -> None:
        """保存所有文件"""
        failed = []
        for path in self.editors:
            try:
                self.save_file(path)
            except Exception as e:
                failed.append(f"{path}：{e}")
        if failed:
            print("以下文件保存失败：")
            for msg in failed:
                print(msg)

    def close_file(self, file_path: str) -> None:
        """关闭文件"""
        editor = self.editors.get(file_path)
        if not editor:
            raise Exception(f"文件未打开：{file_path}")

        # 检查是否已修改
        if editor.is_modified():
            choice = input(f"文件 {file_path} 已修改，是否保存? (y/n)：").strip().lower()
            if choice == "y":
                self.save_file(file_path)

        editor.close()
        del self.editors[file_path]
        self.event_bus.publish(EditorEvent("CLOSE_FILE", file_path))

        # 切换活动编辑器
        if self.active_editor == editor:
            self.active_editor = next(iter(self.editors.values())) if self.editors else None

    def set_active_editor(self, file_path: str) -> None:
        """切换活动编辑器"""
        editor = self.editors.get(file_path)
        if not editor:
            raise Exception(f"文件未打开：{file_path}")
        self.active_editor = editor
        self.event_bus.publish(EditorEvent("SWITCH_EDITOR", file_path))

    def show_editor_list(self) -> None:
        """显示打开的文件列表"""
        if not self.editors:
            print("无打开的文件")
            return
        for path, editor in self.editors.items():
            active_mark = "* " if editor == self.active_editor else "  "
            modified_mark = " [modified]" if editor.is_modified() else ""
            print(f"{active_mark}{path}{modified_mark}")

    def exit(self) -> None:
        """退出程序"""
        self._save_state()
        # 关闭所有文件
        for path in list(self.editors.keys()):
            try:
                self.close_file(path)
            except Exception as e:
                print(f"关闭文件失败：{e}")
        print("程序已退出")
        import sys
        sys.exit(0)

    # ===== Getter 方法 =====
    def get_active_editor(self) -> Optional[Editor]:
        return self.active_editor

    def get_editors(self) -> Dict[str, Editor]:
        return self.editors.copy()

    def is_log_global_enabled(self) -> bool:
        return self.log_global_enabled

    def set_log_global_enabled(self, enabled: bool) -> None:
        self.log_global_enabled = enabled

    def undo(self) -> None:
        """撤销当前活动编辑器的最后一步操作（不切换文件）"""
        if not self.active_editor:
            print("无活动文件，无法撤销")
            return
        self.active_editor.get_command_history().undo()

    def redo(self) -> None:
        """重做当前活动文件的最后一个撤销操作"""
        editor = self.get_active_editor()
        if not editor:
            print("无活动文件")
            return
        editor.get_command_history().redo()