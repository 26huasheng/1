package com.editor.integration;

import com.editor.command.Command;
import com.editor.command.CommandFactory;
import com.editor.command.CommandInvoker;
import com.editor.core.Editor;
import com.editor.core.Workspace;
import com.editor.logger.Logger;
import com.editor.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorIntegrationTest {
    private Path tempDir;
    private Workspace workspace;
    private Logger logger;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("editor-integration-test");
        workspace = new Workspace();
        logger = new Logger(workspace);
        workspace.addEventListener(logger);
        workspace.setConfirmationHandler(file -> false);
    }

    @AfterEach
    void tearDown() throws IOException {
        logger.closeAllWriters();
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> path.toFile().delete());
    }

    @Test
    void commandPipelineShouldUpdateFileAndWriteLog() throws IOException {
        Path file = tempDir.resolve("demo.txt");
        Files.writeString(file, "# log\nhello");
        String commandPath = file.toString().replace('\\', '/');

        CommandFactory factory = new CommandFactory(workspace);

        execute(factory.createCommand("load \"" + commandPath + "\""));
        execute(factory.createCommand("append \"world\""));
        execute(factory.createCommand("save"));

        String content = Files.readString(file);
        String logContent = Files.readString(workspace.getLogPath(file.toString()));

        assertEquals("# log\nhello\nworld", content);
        assertTrue(logContent.contains("append \"world\""));
        assertTrue(logContent.contains("save"));
    }

    @Test
    void persistenceShouldRestoreWorkspaceState() throws IOException {
        Path fileA = tempDir.resolve("a.txt");
        Path fileB = tempDir.resolve("b.txt");
        Files.writeString(fileA, "# log\nalpha");
        Files.writeString(fileB, "beta");

        workspace.loadFile(fileA.toString());
        workspace.loadFile(fileB.toString());
        workspace.setActiveFile(fileA.toString());
        workspace.getEditorByFileName(fileB.toString()).markModified();
        workspace.setLogEnabled(fileB.toString(), true);

        Path stateFile = tempDir.resolve("workspace.properties");
        new Persistence(workspace, stateFile.toString()).save();

        Workspace restoredWorkspace = new Workspace();
        restoredWorkspace.setConfirmationHandler(file -> false);
        new Persistence(restoredWorkspace, stateFile.toString()).restore();

        assertEquals(restoredWorkspace.normalizePath(fileA.toString()), restoredWorkspace.getActiveFileName());

        Editor restoredA = restoredWorkspace.getEditorByFileName(fileA.toString());
        Editor restoredB = restoredWorkspace.getEditorByFileName(fileB.toString());

        assertTrue(restoredA.isLogEnabled());
        assertTrue(restoredB.isModified());
        assertTrue(restoredB.isLogEnabled());
        assertFalse(restoredA.isModified());
    }

    private void execute(Command command) {
        CommandInvoker invoker = command.useWorkspaceInvoker()
                ? workspace.getWorkspaceCommandInvoker()
                : workspace.getActiveCommandInvoker();
        invoker.executeCommand(command, workspace);
    }
}
