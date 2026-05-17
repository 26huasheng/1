package cli;

import java.util.List;

/**
 * 树节点接口（Adapter 模式）。
 * <p>
 * 定义树形结构的基本操作，用于目录树的渲染。
 * </p>
 */
public interface ITreeNode {

    /**
     * 获取节点的名称。
     *
     * @return 节点名称字符串
     */
    String getName();

    /**
     * 获取该节点的所有子节点列表。
     *
     * @return 子节点列表
     */
    List<ITreeNode> getChildren();
}
