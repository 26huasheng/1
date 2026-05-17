# editor/persistence/workspace.py（完全重写版）
import json
import os
from typing import Dict, Any, List, Optional

CONFIG_FILE = ".editor_workspace.json"

def save_workspace_state(
    open_files: List[str],
    active_file: Optional[str],
    modified: Dict[str, bool],
    log_enabled: bool
) -> None:
    """保存工作区状态（只接收纯数据，不依赖Workspace类）"""
    state: Dict[str, Any] = {
        "open_files": open_files,
        "active_file": active_file,
        "modified": modified,
        "log_enabled": log_enabled
    }
    with open(CONFIG_FILE, "w", encoding="utf-8") as f:
        json.dump(state, f, ensure_ascii=False, indent=2)

def load_workspace_state() -> Optional[Dict[str, Any]]:
    """加载工作区状态（只返回纯数据字典，不依赖Workspace类）"""
    if not os.path.exists(CONFIG_FILE):
        return None
    try:
        with open(CONFIG_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"警告：工作区状态恢复失败 - {e}")
        return None