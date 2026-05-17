# tests/test_command.py
from editor.command.base import Command
from editor.command.history import CommandHistory

class _TestCommand(Command):
    def __init__(self):
        self.execute_called = False
        self.undo_called = False
        self.redo_called = False

    def execute(self):
        self.execute_called = True

    def undo(self):
        self.undo_called = True

    def redo(self):
        self.redo_called = True

    def get_description(self):
        return "test command"

def test_undo_redo():
    history = CommandHistory()
    cmd = _TestCommand()

    history.push(cmd)
    history.undo()
    assert cmd.undo_called

    history.redo()
    assert cmd.redo_called