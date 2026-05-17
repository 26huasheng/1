package com.editor.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandHistoryTest {
    @Test
    void recordUndoAndRedoShouldInvokeCommandMethods() {
        CommandHistory history = new CommandHistory();
        CounterCommand command = new CounterCommand();

        history.record(command);
        assertTrue(history.undo());
        assertEquals(1, command.undoCalls);

        assertTrue(history.redo());
        assertEquals(1, command.executeCalls);
    }

    @Test
    void emptyHistoryShouldReturnFalse() {
        CommandHistory history = new CommandHistory();
        assertFalse(history.undo());
        assertFalse(history.redo());
    }

    private static class CounterCommand implements Command {
        int executeCalls;
        int undoCalls;

        @Override
        public void execute() {
            executeCalls++;
        }

        @Override
        public void undo() {
            undoCalls++;
        }

        @Override
        public String getName() {
            return "counter";
        }
    }
}
