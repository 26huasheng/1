package com.editor.event;

/**
 * 工作区对外发布的事件对象。
 */
public class EditorEvent {
    private final EventType type;
    private final Object source;
    private final String commandName;
    private final String fileName;
    private final long timestamp;

    /**
     * 构造一个编辑器事件。
     *
     * @param type 事件类型
     * @param source 事件源
     * @param commandName 相关命令名
     * @param fileName 相关文件名
     */
    public EditorEvent(EventType type, Object source, String commandName, String fileName) {
        this.type = type;
        this.source = source;
        this.commandName = commandName;
        this.fileName = fileName;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @return 事件类型
     */
    public EventType getType() {
        return type;
    }

    /**
     * @return 事件源对象
     */
    public Object getSource() {
        return source;
    }

    /**
     * @return 事件关联的命令名称
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * @return 事件关联的文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return 事件产生的时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
}
