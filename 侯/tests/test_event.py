# tests/test_event.py
from editor.event.bus import EventBus
from editor.event.listener import EditorEvent, EventListener

class _TestListener(EventListener):
    def __init__(self):
        self.received_event = None

    def on_event(self, event: EditorEvent) -> None:
        self.received_event = event

def test_event_publish():
    bus = EventBus()
    listener = _TestListener()
    bus.register(listener)

    event = EditorEvent("TEST", "test data")
    bus.publish(event)

    assert listener.received_event is not None
    assert listener.received_event.type == "TEST"
    bus.unregister(listener)