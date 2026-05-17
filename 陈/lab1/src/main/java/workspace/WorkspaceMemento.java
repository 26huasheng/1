package workspace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作区备忘录数据类（Memento 模式）。
 * <p>
 * 用于保存和恢复工作区的状态，包括已打开文件列表、
 * 当前活动文件路径以及各文件的修改状态。
 * </p>
 */
public class WorkspaceMemento {

    /**
     * 已打开的文件路径列表。
     */
    private final List<String> openedFiles;

    /**
     * 当前活动文件的路径。
     */
    private final String activeFile;

    /**
     * 各文件的修改状态映射表，key 为文件路径，value 为是否已修改。
     */
    private final Map<String, Boolean> modifiedStates;

    /**
     * 构造一个工作区备忘录实例。
     *
     * @param openedFiles  已打开的文件路径列表
     * @param activeFile   当前活动文件的路径
     * @param modifiedStates 各文件的修改状态映射表
     */
    public WorkspaceMemento(List<String> openedFiles, String activeFile, Map<String, Boolean> modifiedStates) {
        this.openedFiles = openedFiles;
        this.activeFile = activeFile;
        this.modifiedStates = modifiedStates;
    }

    /**
     * 获取已打开的文件路径列表。
     *
     * @return 文件路径列表
     */
    public List<String> getOpenedFiles() {
        return openedFiles;
    }

    /**
     * 获取当前活动文件的路径。
     *
     * @return 活动文件路径
     */
    public String getActiveFile() {
        return activeFile;
    }

    /**
     * 获取各文件的修改状态映射表。
     *
     * @return 修改状态映射表
     */
    public Map<String, Boolean> getModifiedStates() {
        return modifiedStates;
    }
}
