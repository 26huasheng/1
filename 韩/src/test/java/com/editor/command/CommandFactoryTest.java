package com.editor.command;

import com.editor.core.Workspace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandFactoryTest {
    @Test
    void shouldParseQuotedTextAndEscapes() {
        CommandFactory factory = new CommandFactory(new Workspace());
        Command command = factory.createCommand("append \"hello\\nworld\"");
        assertEquals("append \"hello\nworld\"", command.getName());
    }

    @Test
    void shouldRejectUnknownCommand() {
        CommandFactory factory = new CommandFactory(new Workspace());
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> factory.createCommand("missing command"));
        assertTrue(exception.getMessage().contains("未知命令")
                || exception.getMessage().contains("鏈煡鍛戒护"));
    }

    @Test
    void shouldRejectUnclosedQuotes() {
        CommandFactory factory = new CommandFactory(new Workspace());
        assertThrows(IllegalArgumentException.class,
                () -> factory.createCommand("append \"hello"));
    }

    @Test
    void shouldRejectInvalidPositionFormat() {
        CommandFactory factory = new CommandFactory(new Workspace());
        assertThrows(IllegalArgumentException.class,
                () -> factory.createCommand("insert wrong-format \"text\""));
    }

    @Test
    void shouldRejectMissingRequiredArguments() {
        CommandFactory factory = new CommandFactory(new Workspace());
        assertThrows(IllegalArgumentException.class,
                () -> factory.createCommand("load"));
    }
}
