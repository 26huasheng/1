#!/usr/bin/env python3
"""文本编辑器主程序"""
from editor.cli import CommandLineInterface

if __name__ == "__main__":
    cli = CommandLineInterface()
    cli.start()