package cli;

import java.util.List;

/**
 * 目录树打印工具类。
 * <p>
 * 提供静态方法递归打印带有规范符号的目录树格式，
 * 使用 ├── 、└── 和 │   符号构建可视化树形结构。
 * </p>
 */
public class TreePrinter {

    /**
     * 打印指定树节点的目录树结构到标准输出。
     *
     * @param node 要打印的树节点
     */
    public static void printTree(ITreeNode node) {
        System.out.println(node.getName());
        printTreeRecursive(node, "");
    }

    /**
     * 递归打印树节点。
     *
     * @param node   当前树节点
     * @param prefix 当前层级的缩进前缀
     */
    private static void printTreeRecursive(ITreeNode node, String prefix) {
        List<ITreeNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            ITreeNode child = children.get(i);
            boolean isLast = (i == children.size() - 1);

            System.out.println(prefix + (isLast ? "└── " : "├── ") + child.getName());

            if (!child.getChildren().isEmpty()) {
                String newPrefix = prefix + (isLast ? "    " : "│   ");
                printTreeRecursive(child, newPrefix);
            }
        }
    }
}
