package cli;

import core.IFileSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件系统节点适配器（Adapter 模式）。
 * <p>
 * 将底层的 IFileSystem 和路径字符串递归适配为 ITreeNode 树形结构，
 * 用于目录树的渲染展示。
 * </p>
 */
public class FileNodeAdapter implements ITreeNode {

    /**
     * 文件系统抽象接口引用。
     */
    private final IFileSystem fs;

    /**
     * 当前节点对应的文件/目录路径。
     */
    private final String path;

    /**
     * 当前节点的名称（路径的最后一部分）。
     */
    private final String name;

    /**
     * 构造一个文件系统节点适配器实例。
     *
     * @param fs   文件系统抽象接口
     * @param path 当前节点对应的文件/目录路径
     */
    public FileNodeAdapter(IFileSystem fs, String path) {
        this.fs = fs;
        this.path = path;
        int lastSlash = path.lastIndexOf("/");
        this.name = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ITreeNode> getChildren() {
        List<ITreeNode> children = new ArrayList<>();
        try {
            List<String> entries = fs.listDirectory(path);
            for (String entry : entries) {
                String childPath = path.endsWith("/") ? path + entry : path + "/" + entry;
                children.add(new FileNodeAdapter(fs, childPath));
            }
        } catch (Exception e) {
            // 非目录或无权限时返回空子节点列表
        }
        return children;
    }
}
