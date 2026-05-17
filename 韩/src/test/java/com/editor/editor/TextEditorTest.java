package com.editor.editor;

import com.editor.core.TextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextEditorTest {
    private TextEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TextEditor("test.txt", "line1\nline2\nline3");
    }

    @Test
    void insertShouldUpdateTargetLine() {
        editor.insert(1, 3, "INSERTED");
        assertEquals("liINSERTEDne1", editor.getContentForSave().split("\n")[0]);
        assertTrue(editor.isModified());
    }

    @Test
    void insertShouldSupportMultilineText() {
        editor.insert(1, 6, "\nnext");
        assertEquals("line1\nnext\nline2\nline3", editor.getContentForSave());
    }

    @Test
    void deleteShouldRejectCrossLineDeletion() {
        assertThrows(IllegalArgumentException.class, () -> editor.delete(1, 3, 10));
    }

    @Test
    void replaceShouldChangeTextInPlace() {
        editor.replace(2, 1, 4, "row");
        assertEquals("row2", editor.getContentForSave().split("\n")[1]);
    }

    @Test
    void showShouldRenderLineNumbers() {
        assertEquals("2: line2\n3: line3", editor.show(2, 3));
    }

    @Test
    void insertShouldRejectOutOfRangePosition() {
        assertThrows(IllegalArgumentException.class, () -> editor.insert(10, 1, "x"));
        assertThrows(IllegalArgumentException.class, () -> editor.insert(1, 100, "x"));
    }

    @Test
    void showShouldRejectInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> editor.show(0, 1));
        assertThrows(IllegalArgumentException.class, () -> editor.show(3, 2));
        assertThrows(IllegalArgumentException.class, () -> editor.show(1, 10));
    }

    @Test
    void emptyEditorShouldRejectInsertOutsideOrigin() {
        TextEditor emptyEditor = new TextEditor("empty.txt", "");
        assertThrows(IllegalArgumentException.class, () -> emptyEditor.insert(2, 1, "x"));
    }
}
