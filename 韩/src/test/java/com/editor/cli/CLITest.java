package com.editor.cli;

import com.editor.core.Workspace;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CLITest {
    @Test
    void shouldRunSimpleSession() throws IOException {
        String input = String.join(System.lineSeparator(),
                "init temp.txt",
                "append \"hello\"",
                "show",
                "exit",
                "n") + System.lineSeparator();

        Path tempState = Files.createTempFile("editor-cli-test", ".properties");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            CLI cli = new CLI(new Workspace(),
                    new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
                    tempState.toString());
            cli.start();
        } finally {
            System.setOut(originalOut);
            Files.deleteIfExists(tempState);
        }

        assertTrue(out.toString(StandardCharsets.UTF_8).contains("1: hello"));
    }

    @Test
    void editorListShouldOnlyShowActiveAndModifiedMarkers() throws IOException {
        String input = String.join(System.lineSeparator(),
                "init temp.txt with-log",
                "editor-list",
                "exit",
                "n") + System.lineSeparator();

        Path tempState = Files.createTempFile("editor-cli-test", ".properties");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            CLI cli = new CLI(new Workspace(),
                    new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
                    tempState.toString());
            cli.start();
        } finally {
            System.setOut(originalOut);
            Files.deleteIfExists(tempState);
        }

        String console = out.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("* "));
        assertTrue(console.contains("[modified]"));
        assertFalse(console.contains("[log]"));
    }

    @Test
    void shouldPrintErrorForUnknownCommand() throws IOException {
        String input = String.join(System.lineSeparator(),
                "unknown-command",
                "exit",
                "n") + System.lineSeparator();

        Path tempState = Files.createTempFile("editor-cli-test", ".properties");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err));
        try {
            CLI cli = new CLI(new Workspace(),
                    new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
                    tempState.toString());
            cli.start();
        } finally {
            System.setErr(originalErr);
            Files.deleteIfExists(tempState);
        }

        assertTrue(err.toString(StandardCharsets.UTF_8).contains("Error:"));
    }

    @Test
    void shouldPrintErrorWhenSaveNeedsActiveFile() throws IOException {
        String input = String.join(System.lineSeparator(),
                "save",
                "exit",
                "n") + System.lineSeparator();

        Path tempState = Files.createTempFile("editor-cli-test", ".properties");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err));
        try {
            CLI cli = new CLI(new Workspace(),
                    new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
                    tempState.toString());
            cli.start();
        } finally {
            System.setErr(originalErr);
            Files.deleteIfExists(tempState);
        }

        String errorOutput = err.toString(StandardCharsets.UTF_8);
        assertTrue(errorOutput.contains("Error:"));
        assertFalse(errorOutput.isBlank());
    }
}
