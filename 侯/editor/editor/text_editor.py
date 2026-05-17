# editor/editor/text_editor.py
from typing import List, Optional
from .base import Editor
from editor.command.history import CommandHistory
from editor.persistence.file import load_file, save_file

class TextEditor(Editor):
    """文本编辑器实现"""
    def __init__(self, file_path: str):
        self._file_path = file_path
        self._modified = False
        self._lines: List[str] = []
        self._command_history = CommandHistory()
        self._log_enabled = False

    # ===== 核心编辑方法 =====
    def append(self, text: str) -> None:
        """追加文本到末尾"""
        self._lines.append(text)
        self._modified = True

    def insert(self, line_num: int, col_num: int, text: str) -> None:
        """在指定位置插入文本（line:col 从1开始）"""
        if not self._lines:
            if line_num != 1 or col_num != 1:
                raise Exception("空文件只能在1:1位置插入")
            self._lines.append(text)
            self._modified = True
            return

        if line_num < 1 or line_num > len(self._lines):
            raise Exception("行号越界")
        target_line = self._lines[line_num - 1]
        if col_num < 1 or col_num > len(target_line) + 1:
            raise Exception("列号越界")

        # 处理包含换行符的文本，按行分割
        split_text = text.split("\n")
        if len(split_text) == 1:
            new_line = target_line[:col_num-1] + text + target_line[col_num-1:]
            self._lines[line_num - 1] = new_line
        else:
            # 多行插入
            first_part = target_line[:col_num-1] + split_text[0]
            self._lines[line_num - 1] = first_part
            for i in range(1, len(split_text)-1):
                self._lines.insert(line_num + i - 1, split_text[i])
            last_part = split_text[-1] + target_line[col_num-1:]
            self._lines.insert(line_num + len(split_text) - 2, last_part)
        self._modified = True

    def delete(self, line_num: int, col_num: int, length: int) -> None:
        """删除指定位置开始的 length 个字符（不可跨行）"""
        if not self._lines:
            raise Exception("文件为空，无法删除")
        if line_num < 1 or line_num > len(self._lines):
            raise Exception("行号越界")
        target_line = self._lines[line_num - 1]
        if col_num < 1 or col_num > len(target_line):
            raise Exception("列号越界")
        if col_num + length - 1 > len(target_line):
            raise Exception("删除长度超出行尾")

        new_line = target_line[:col_num-1] + target_line[col_num-1+length:]
        self._lines[line_num - 1] = new_line
        self._modified = True

    def replace(self, line_num: int, col_num: int, length: int, text: str) -> None:
        """替换指定位置的字符（先删除后插入）"""
        self.delete(line_num, col_num, length)
        self.insert(line_num, col_num, text)

    def show(self, start_line: Optional[int] = None, end_line: Optional[int] = None) -> None:
        """显示指定范围的文本（按行）"""
        if not self._lines:
            print("文件为空")
            return

        start = start_line or 1
        end = end_line or len(self._lines)
        start = max(1, start)
        end = min(len(self._lines), end)

        if start > end:
            print("起始行号大于结束行号")
            return

        for i in range(start-1, end):
            print(f"{i+1}: {self._lines[i]}")

    # ===== Editor 接口实现 =====
    def get_file_path(self) -> str:
        return self._file_path

    def set_file_path(self, path: str) -> None:
        self._file_path = path

    def is_modified(self) -> bool:
        return self._modified

    def set_modified(self, modified: bool) -> None:
        self._modified = modified

    def get_command_history(self) -> CommandHistory:
        return self._command_history

    def load_content(self) -> None:
        from editor.persistence.file import load_file
        self._lines = load_file(self._file_path)
    # 文件第一行是# log，自动启用日志
        if self._lines and self._lines[0].strip() == "# log":
            self._log_enabled = True
        self._modified = False

    def save_content(self) -> None:
        save_file(self._file_path, self._lines)
        self._modified = False

    def close(self) -> None:
        self._lines.clear()
        self._command_history.clear()

    def get_lines(self) -> List[str]:
        return self._lines.copy()  # 返回副本，避免外部修改

    def is_log_enabled(self) -> bool:
        return self._log_enabled

    def set_log_enabled(self, enabled: bool) -> None:
        self._log_enabled = enabled
    
    def set_lines(self, lines: list[str]) -> None:
        """从快照恢复文本内容"""
        self._lines = lines.copy()
        self._modified = True