# editor/event/listener.py
from abc import ABC, abstractmethod
from typing import Any
from datetime import datetime

class EditorEvent:
    """事件基类"""
    def __init__(self, event_type: str, data: Any):
        self.timestamp = datetime.now()
        self.type = event_type  # 事件类型：COMMAND_EXECUTE、FILE_MODIFY 等
        self.data = data        # 事件数据

class EventListener(ABC):
    """观察者接口"""
    @abstractmethod
    def on_event(self, event: EditorEvent) -> None:
        pass