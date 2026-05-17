# editor/command/base.py
from abc import ABC, abstractmethod

class Command(ABC):
    """命令抽象接口"""
    @abstractmethod
    def execute(self) -> None:
        """执行命令"""
        pass

    @abstractmethod
    def undo(self) -> None:
        """撤销命令"""
        pass

    @abstractmethod
    def redo(self) -> None:
        """重做命令"""
        pass

    @abstractmethod
    def get_description(self) -> str:
        """返回命令的文字描述"""
        pass

    def is_undoable(self) -> bool:
        """是否可撤销（显示类命令不可撤销）"""
        return True