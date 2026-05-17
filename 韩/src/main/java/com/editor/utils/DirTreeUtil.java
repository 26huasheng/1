package com.editor.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 目录树生成工具类。
 */
public final class DirTreeUtil {
    private DirTreeUtil() {
    }

    /**
     * 从根目录开始生成树状文本。
     *
     * @param root 根目录
     * @return 树状字符串
     */
    public static String generateTree(File root) {
        StringBuilder builder = new StringBuilder();
        builder.append(root.getName()).append(System.lineSeparator());
        build(root, "", builder);
        return builder.toString().stripTrailing();
    }

    /**
     * 递归构造目录树。
     * 目录优先，其次按名称排序，确保输出稳定。
     *
     * @param dir 当前目录
     * @param prefix 当前层级前缀
     * @param builder 输出构造器
     */
    private static void build(File dir, String prefix, StringBuilder builder) {
        File[] children = dir.listFiles();
        if (children == null || children.length == 0) {
            return;
        }
        Arrays.sort(children, Comparator.comparing(File::isDirectory).reversed()
                .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            boolean isLast = i == children.length - 1;
            builder.append(prefix)
                    .append(isLast ? "└── " : "├── ")
                    .append(child.getName())
                    .append(System.lineSeparator());
            if (child.isDirectory()) {
                build(child, prefix + (isLast ? "    " : "│   "), builder);
            }
        }
    }
}
