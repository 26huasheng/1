package com.editor.command;

import com.editor.command.impl.AppendCommand;
import com.editor.command.impl.CloseCommand;
import com.editor.command.impl.DeleteCommand;
import com.editor.command.impl.DirTreeCommand;
import com.editor.command.impl.EditCommand;
import com.editor.command.impl.EditorListCommand;
import com.editor.command.impl.ExitCommand;
import com.editor.command.impl.InitCommand;
import com.editor.command.impl.InsertCommand;
import com.editor.command.impl.LoadCommand;
import com.editor.command.impl.LogOffCommand;
import com.editor.command.impl.LogOnCommand;
import com.editor.command.impl.LogShowCommand;
import com.editor.command.impl.ReplaceCommand;
import com.editor.command.impl.SaveCommand;
import com.editor.command.impl.ShowCommand;
import com.editor.core.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * 将用户输入的原始命令行解析为具体的命令对象。
 */
public class CommandFactory {
    private final Workspace workspace;

    /**
     * 构造命令工厂。
     *
     * @param workspace 当前工作区
     */
    public CommandFactory(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * 根据输入文本创建命令对象。
     *
     * @param input 用户输入的一整行命令
     * @return 对应的命令实例
     */
    public Command createCommand(String input) {
        List<String> tokens = parseCommandLine(input);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("空命令");
        }
        String name = tokens.get(0).toLowerCase();
        return switch (name) {
            case "load" -> new LoadCommand(workspace, requireSize(tokens, 2).get(1));
            case "save" -> new SaveCommand(workspace, tokens.size() > 1 ? tokens.get(1) : null);
            case "init" -> new InitCommand(workspace,
                    requireSize(tokens, 2).get(1),
                    tokens.size() > 2 && "with-log".equalsIgnoreCase(tokens.get(2)));
            case "close" -> new CloseCommand(workspace, tokens.size() > 1 ? tokens.get(1) : null);
            case "edit" -> new EditCommand(workspace, requireSize(tokens, 2).get(1));
            case "editor-list" -> new EditorListCommand(workspace);
            case "dir-tree" -> new DirTreeCommand(workspace, tokens.size() > 1 ? tokens.get(1) : null);
            case "exit" -> new ExitCommand(workspace);
            case "append" -> new AppendCommand(workspace, requireSize(tokens, 2).get(1));
            case "insert" -> {
                requireSize(tokens, 3);
                int[] position = parsePosition(tokens.get(1));
                yield new InsertCommand(workspace, position[0], position[1], tokens.get(2));
            }
            case "delete" -> {
                requireSize(tokens, 3);
                int[] position = parsePosition(tokens.get(1));
                yield new DeleteCommand(workspace, position[0], position[1], Integer.parseInt(tokens.get(2)));
            }
            case "replace" -> {
                requireSize(tokens, 4);
                int[] position = parsePosition(tokens.get(1));
                yield new ReplaceCommand(workspace, position[0], position[1],
                        Integer.parseInt(tokens.get(2)), tokens.get(3));
            }
            case "show" -> {
                if (tokens.size() == 1) {
                    yield new ShowCommand(workspace, null, null);
                }
                int[] range = parsePosition(tokens.get(1));
                yield new ShowCommand(workspace, range[0], range[1]);
            }
            case "log-on" -> new LogOnCommand(workspace, tokens.size() > 1 ? tokens.get(1) : null);
            case "log-off" -> new LogOffCommand(workspace, tokens.size() > 1 ? tokens.get(1) : null);
            case "log-show" -> new LogShowCommand(workspace, tokens.size() > 1 ? tokens.get(1) : null);
            default -> throw new IllegalArgumentException("未知命令: " + name);
        };
    }

    /**
     * 校验命令参数个数是否满足要求。
     *
     * @param tokens 已分词的命令列表
     * @param size 最少需要的参数个数
     * @return 原始 tokens，便于链式使用
     */
    private List<String> requireSize(List<String> tokens, int size) {
        if (tokens.size() < size) {
            throw new IllegalArgumentException("命令参数不足");
        }
        return tokens;
    }

    /**
     * 解析形如 line:col 或 start:end 的位置参数。
     *
     * @param token 位置字符串
     * @return 长度为 2 的整数数组
     */
    private int[] parsePosition(String token) {
        String[] split = token.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException("位置参数格式错误: " + token);
        }
        try {
            return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("位置参数格式错误: " + token, e);
        }
    }

    /**
     * 解析命令行。
     * 支持双引号包裹的文本参数，以及 \n、\t、\" 等简单转义。
     *
     * @param input 原始输入
     * @return 解析后的 token 列表
     */
    private List<String> parseCommandLine(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == '\\' && i + 1 < input.length()) {
                char next = input.charAt(++i);
                current.append(switch (next) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> next;
                });
                continue;
            }
            if (Character.isWhitespace(c) && !inQuotes) {
                // 引号外空白字符作为分隔符；引号内空白保留在同一个参数里。
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            current.append(c);
        }
        if (inQuotes) {
            throw new IllegalArgumentException("引号未闭合");
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
