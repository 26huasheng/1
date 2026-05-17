package com.editor.event;

/**
 * 事件监听器接口。
 */
public interface EventListener {
    /**
     * 处理工作区发布的事件。
     *
     * @param event 事件对象
     */
    void onEvent(EditorEvent event);
}
