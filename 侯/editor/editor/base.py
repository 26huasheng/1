# editor/editor/base.py
from abc import ABC, abstractmethod
from typing import Optional
from editor.command.history import CommandHistory

class Editor(ABC):
    """编辑器抽象接口（后续可扩展 XMLEditor）"""
    @abstractmethod
    def get_file_path(self) -> str:
        pass

    @abstractmethod
    def set_file_path(self, path: str) -> None:
        pass

    @abstractmethod
    def is_modified(self) -> bool:
        pass

    @abstractmethod
    def set_modified(self, modified: bool) -> None:
        pass

    @abstractmethod
    def get_command_history(self) -> CommandHistory:
        pass

    @abstractmethod
    def load_content(self) -> None:
        pass

    @abstractmethod
    def save_content(self) -> None:
        pass

    @abstractmethod
    def close(self) -> None:
        pass