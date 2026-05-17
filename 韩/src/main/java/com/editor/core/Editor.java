package com.editor.core;

import com.editor.command.CommandInvoker;

/**
 * 编辑器的统一抽象接口。
 * 目前由文本编辑器实现，后续可扩展到 XML 等其他类型。
 */
public interface Editor {
    /**
     * @return 当前编辑器绑定的文件名或路径
     */
    String getFileName();

    /**
     * 修改当前编辑器绑定的文件名。
     *
     * @param fileName 新文件名
     */
    void setFileName(String fileName);

    /**
     * @return 当前编辑器专属的命令调用器
     */
    CommandInvoker getCommandInvoker();

    /**
     * @return 文件自上次保存后是否被修改过
     */
    boolean isModified();

    /**
     * 将编辑器状态标记为“已修改”。
     */
    void markModified();

    /**
     * 将编辑器状态标记为“已保存”。
     */
    void markSaved();

    /**
     * @return 当前文件是否启用了日志
     */
    boolean isLogEnabled();

    /**
     * 设置日志开关状态。
     *
     * @param enabled 是否启用日志
     */
    void setLogEnabled(boolean enabled);

    /**
     * 将当前内容转换为可直接写入磁盘的文本形式。
     *
     * @return 可保存的文本内容
     */
    String getContentForSave();

    /**
     * 从字符串加载编辑器内容。
     *
     * @param content 外部读取到的文本内容
     */
    void loadContentFromString(String content);

    /**
     * @return 编辑器类型标识
     */
    String getType();
}
