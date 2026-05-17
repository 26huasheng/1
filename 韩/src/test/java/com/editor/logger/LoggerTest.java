package com.editor.logger;

import com.editor.core.Workspace;
import com.editor.event.EditorEvent;
import com.editor.event.EventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggerTest {
    private Workspace workspace;
    private Logger logger;
    private Path tempDir;
    private String file;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("editor-logger-test");
        file = tempDir.resolve("logtest.txt").toString();
        Files.writeString(Path.of(file), "# log\ncontent");
        workspace = new Workspace();
        workspace.loadFile(file);
        logger = new Logger(workspace);
        workspace.addEventListener(logger);
    }

    @AfterEach
    void tearDown() throws IOException {
        logger.closeAllWriters();
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> path.toFile().delete());
    }

    @Test
    void commandEventShouldWriteLogFile() throws IOException {
        workspace.publishEvent(new EditorEvent(EventType.COMMAND_EXECUTED, workspace, "append \"test\"", workspace.getActiveFileName()));
        String content = Files.readString(workspace.getLogPath(file));
        assertTrue(content.contains("session start at"));
        assertTrue(content.contains("append \"test\""));
    }

    @Test
    void disabledLogShouldNotCreateFile() {
        workspace.setLogEnabled(file, false);
        workspace.publishEvent(new EditorEvent(EventType.COMMAND_EXECUTED, workspace, "append", workspace.getActiveFileName()));
        assertFalse(Files.exists(workspace.getLogPath(file)));
    }
}
