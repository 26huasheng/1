# editor/event/bus.py
from typing import List
from .listener import EditorEvent, EventListener


class EventBus:
    """事件总线（单例模式）"""
    _instance: "EventBus | None" = None
    listeners: List[EventListener]

    def __new__(cls) -> "EventBus":
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance.listeners = []
        return cls._instance

    def register(self, listener: EventListener) -> None:
        """订阅事件"""
        if listener not in self.listeners:
            self.listeners.append(listener)

    def unregister(self, listener: EventListener) -> None:
        """取消订阅"""
        if listener in self.listeners:
            self.listeners.remove(listener)

    def publish(self, event: EditorEvent) -> None:
        """发布事件"""
        for listener in self.listeners:
            try:
                listener.on_event(event)
            except Exception as e:
                print(f"警告：事件处理失败 - {e}")