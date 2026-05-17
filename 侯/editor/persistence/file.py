# editor/persistence/file.py
from typing import List
import os

def load_file(file_path: str) -> List[str]:
    """加载文件内容为行数组"""
    if not os.path.exists(file_path):
        return []
    with open(file_path, "r", encoding="utf-8") as f:
        return [line.rstrip("\n") for line in f]

def save_file(file_path: str, lines: List[str]) -> None:
    """保存行数组到文件"""
    os.makedirs(os.path.dirname(file_path) or ".", exist_ok=True)
    with open(file_path, "w", encoding="utf-8") as f:
        for i, line in enumerate(lines):
            f.write(line)
            if i < len(lines) - 1:
                f.write("\n")