# tests/conftest.py
import pytest
from editor.workspace.workspace import Workspace
from editor.event.bus import EventBus

@pytest.fixture(autouse=True)
def reset_singletons():
    """每个测试前重置单例状态，避免测试间污染"""
    # 重置 Workspace
    if Workspace._instance is not None:
        Workspace._instance._reset()
    # 重置 EventBus
    if EventBus._instance is not None:
        EventBus._instance.listeners = []
    yield