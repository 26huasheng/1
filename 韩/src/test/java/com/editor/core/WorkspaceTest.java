package com.editor.core;

import com.editor.command.impl.AppendCommand;
import com.editor.command.impl.EditCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceTest {
    private Workspace workspace;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("editor-workspace-test");
        workspace = new Workspace();
        workspace.setConfirmationHandler(file -> false);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> path.toFile().delete());
    }

    @Test
    void loadNonExistentFileShouldCreateModifiedBuffer() {
        String file = tempDir.resolve("new.txt").toString();
        workspace.loadFile(file);

        Editor editor = workspace.getActiveEditor();
        assertNotNull(editor);
        assertEquals(workspace.normalizePath(file), editor.getFileName());
        assertTrue(editor.isModified());
    }

    @Test
    void saveFileShouldWriteToDisk() throws IOException {
        String file = tempDir.resolve("test.txt").toString();
        workspace.initFile(file, false);
        ((TextEditor) workspace.getActiveEditor()).appendLine("Hello");

        workspace.saveFile(file);

        assertFalse(workspace.getActiveEditor().isModified());
        assertEquals("Hello", Files.readString(Path.of(workspace.normalizePath(file))));
    }

    @Test
    void closeShouldSwitchToMostRecentEditor() {
        String a = tempDir.resolve("a.txt").toString();
        String b = tempDir.resolve("b.txt").toString();
        workspace.loadFile(a);
        workspace.loadFile(b);

        workspace.closeFile(b);

        assertEquals(workspace.normalizePath(a), workspace.getActiveFileName());
    }

    @Test
    void undoShouldOnlyAffectActiveEditorHistory() {
        String a = tempDir.resolve("a.txt").toString();
        String b = tempDir.resolve("b.txt").toString();
        workspace.initFile(a, false);
        workspace.getActiveCommandInvoker().executeCommand(new AppendCommand(workspace, "first"), workspace);
        workspace.initFile(b, false);

        assertFalse(workspace.undo());
        assertEquals(workspace.normalizePath(b), workspace.getActiveFileName());

        workspace.setActiveFile(a);
        assertTrue(workspace.undo());
        assertEquals("", workspace.getActiveEditor().getContentForSave());
    }

    @Test
    void undoShouldNotRollbackWorkspaceLevelEditSwitch() {
        String a = tempDir.resolve("a.txt").toString();
        String b = tempDir.resolve("b.txt").toString();
        workspace.initFile(a, false);
        workspace.initFile(b, false);

        workspace.getWorkspaceCommandInvoker()
                .executeCommand(new EditCommand(workspace, workspace.normalizePath(a)), workspace);

        assertEquals(workspace.normalizePath(a), workspace.getActiveFileName());
        assertFalse(workspace.undo());
        assertEquals(workspace.normalizePath(a), workspace.getActiveFileName());
    }

    @Test
    void saveWithoutActiveFileShouldThrow() {
        assertThrows(IllegalStateException.class, () -> workspace.saveFile(null));
    }

    @Test
    void setActiveFileForUnopenedFileShouldThrow() {
        String missing = tempDir.resolve("missing.txt").toString();
        assertThrows(IllegalArgumentException.class, () -> workspace.setActiveFile(missing));
    }
}
