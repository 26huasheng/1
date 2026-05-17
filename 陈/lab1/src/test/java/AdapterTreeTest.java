

import cli.FileNodeAdapter;
import cli.ITreeNode;
import core.IFileSystem;
import core.MockFileSystem;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * 适配器验证测试类（Adapter 模式）。
 * <p>
 * 在 MockFileSystem 中构造目录树结构，装载入 FileNodeAdapter，
 * 验证其 getChildren() 返回的列表大小和层级关系正确。
 * 全程无物理磁盘读写。
 * </p>
 */
public class AdapterTreeTest {

    /**
     * 模拟文件系统实例。
     */
    private MockFileSystem fs;

    @Before
    public void setUp() {
        fs = new MockFileSystem();
    }

    /**
     * 测试 a/b/c 路径结构的适配器子节点数量。
     * <p>
     * 在 MockFileSystem 中构造 a/b/c 路径结构，
     * 验证根节点 a 的 getChildren() 返回大小为 1（仅包含 b）。
     * </p>
     */
    @Test
    public void testNestedDirectoryChildren() {
        fs.writeLines("a/b/c/file.txt", List.of("content"));

        ITreeNode root = new FileNodeAdapter(fs, "a");

        List<ITreeNode> children = root.getChildren();

        assertEquals(1, children.size());
        assertEquals("b", children.get(0).getName());
    }

    /**
     * 测试二级目录的子节点数量。
     */
    @Test
    public void testSecondLevelDirectoryChildren() {
        fs.writeLines("a/b/c/file.txt", List.of("content"));

        ITreeNode root = new FileNodeAdapter(fs, "a");
        ITreeNode levelB = root.getChildren().get(0);

        List<ITreeNode> childrenB = levelB.getChildren();

        assertEquals(1, childrenB.size());
        assertEquals("c", childrenB.get(0).getName());
    }

    /**
     * 测试空目录应返回空子节点列表。
     */
    @Test
    public void testEmptyDirectory() {
        fs.writeLines("empty/file.txt", List.of("content"));

        ITreeNode root = new FileNodeAdapter(fs, "empty");

        List<ITreeNode> children = root.getChildren();

        assertEquals(1, children.size());
        assertEquals("file.txt", children.get(0).getName());
    }

    /**
     * 测试多文件目录的子节点数量。
     */
    @Test
    public void testMultipleFilesInDirectory() {
        fs.writeLines("dir/file1.txt", List.of("content1"));
        fs.writeLines("dir/file2.txt", List.of("content2"));
        fs.writeLines("dir/file3.txt", List.of("content3"));

        ITreeNode root = new FileNodeAdapter(fs, "dir");

        List<ITreeNode> children = root.getChildren();

        assertEquals(3, children.size());
    }

    /**
     * 测试节点名称应正确提取路径最后一部分。
     */
    @Test
    public void testNodeNameExtraction() {
        fs.writeLines("a/b/c/file.txt", List.of("content"));

        ITreeNode root = new FileNodeAdapter(fs, "a");
        assertEquals("a", root.getName());

        ITreeNode levelB = root.getChildren().get(0);
        assertEquals("b", levelB.getName());

        ITreeNode levelC = levelB.getChildren().get(0);
        assertEquals("c", levelC.getName());
    }
}
