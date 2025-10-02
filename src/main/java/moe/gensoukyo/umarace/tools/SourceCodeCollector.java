package moe.gensoukyo.umarace.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * 一个多功能源代码处理工具，集成了两个核心功能：
 *
 * <p><b>阶段 1: 自动移除注释</b></p>
 * <ul>
 *     <li>在执行收集任务前，首先会自动清理目标目录中所有 .java 文件的注释。</li>
 *     <li>此过程会直接覆盖原始文件，因此请确保代码已备份或在版本控制中。</li>
 *     <li>移除注释时，会自动跳过文件名以 "SourceCode" 开头的文件。</li>
 * </ul>
 *
 * <p><b>阶段 2: 收集源代码</b></p>
 * <ul>
 *     <li>将清理过注释的所有 .java 文件内容合并到一个位于桌面上的 "code.txt" 文件中。</li>
 *     <li>收集时会排除 "tools" 包以及明确指定的几个文件名（如本文件）。</li>
 * </ul>
 *
 * <p>要使用它，请配置 {@code TARGET_DIR} 和 {@code OUTPUT_FILE} 两个常量，然后运行 main 方法。</p>
 */
public class SourceCodeCollector {

    // ====================================================================
    //                           配置区域
    // ====================================================================
    private static final String TARGET_DIR = "D:\\code\\umarace-template-1.21.1\\src\\main\\java";
    private static final String OUTPUT_FILE = System.getProperty("user.home") + "/Desktop/code.txt";
    private static final Set<String> EXCLUDED_FILES_FOR_COLLECTION = new HashSet<>(Arrays.asList(
            "SourceCodeCollector.java",
            "SourceCodeRestorer.java",
            "CommentRemover.java" // 最好也把原始的CommentRemover加入排除列表
    ));


    // ====================================================================
    //         第一阶段：从 CommentRemover.java 移植的核心代码
    // ====================================================================

    /**
     * 用于匹配字符串字面量、多行注释和单行注释的正则表达式。
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "(\"([^\"\\\\]|\\\\.)*\")|(/\\*[\\s\\S]*?\\*/)|(//.*)"
    );

    /**
     * 移除给定Java源代码字符串中的所有注释。
     * @param sourceCode 原始源代码内容。
     * @return 移除了注释的源代码内容。
     */
    private static String removeComments(String sourceCode) {
        return COMMENT_PATTERN.matcher(sourceCode).replaceAll(match -> {
            if (match.group(1) != null) {
                // 这是一个字符串字面量，原样返回。
                return match.group(0);
            } else {
                // 这是一个注释，用空字符串替换。
                return "";
            }
        });
    }

    /**
     * 处理单个文件：读取、移除注释、清理空行、写回。
     * @param filePath 要处理的文件的路径。
     * @throws IOException 如果发生文件读写错误。
     */
    private static void processAndCleanFile(Path filePath) throws IOException {
        System.out.println("  Cleaning: " + filePath);
        String originalContent = Files.readString(filePath, StandardCharsets.UTF_8);
        String cleanedContent = removeComments(originalContent);
        // 清理可能因移除注释而产生的多余空行
        cleanedContent = cleanedContent.replaceAll("(?m)^[ \t]*\r?\n", "").trim();
        Files.writeString(filePath, cleanedContent, StandardCharsets.UTF_8);
    }

    /**
     * 递归地处理目录下的所有 .java 文件以移除注释。
     * @param rootDir 起始目录的路径。
     * @return 处理过的文件总数。
     * @throws IOException 如果遍历目录时发生错误。
     */
    private static int removeCommentsInDirectory(Path rootDir) throws IOException {
        // 注释移除阶段的排除规则
        final String exclusionPrefix = "SourceCode";
        AtomicInteger fileCount = new AtomicInteger(0);

        Files.walk(rootDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    // 在移除注释阶段，我们只排除以 "SourceCode" 开头的文件
                    if (fileName.startsWith(exclusionPrefix)) {
                        System.out.println("  Skipping comment removal for: " + path);
                        return;
                    }
                    try {
                        processAndCleanFile(path);
                        fileCount.incrementAndGet();
                    } catch (IOException e) {
                        System.err.println("  Failed to process file " + path + ": " + e.getMessage());
                    }
                });
        return fileCount.get();
    }


    // ====================================================================
    //               第二阶段：原始 SourceCodeCollector 的代码
    // ====================================================================

    /**
     * 递归地收集所有符合条件的 .java 文件。
     * @param directory 起始目录。
     * @param javaFiles 用于存放结果的列表。
     */
    private static void collectJavaFiles(File directory, List<File> javaFiles) {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                // 如果目录是 "tools"，则跳过整个目录
                if (file.getName().equals("tools")) {
                    System.out.println("  Skipping collection from directory: " + file.getPath());
                    continue;
                }
                collectJavaFiles(file, javaFiles);
            } else if (file.isFile() &&
                    file.getName().endsWith(".java") &&
                    !EXCLUDED_FILES_FOR_COLLECTION.contains(file.getName())) {
                javaFiles.add(file);
            }
        }
    }

    /**
     * 将单个源文件的内容追加到输出文件中。
     * @param sourceFile 源文件。
     * @param outputPath 目标输出文件的路径。
     * @throws IOException 如果发生文件读写错误。
     */
    private static void appendFileContent(File sourceFile, Path outputPath) throws IOException {
        Path targetPath = Paths.get(TARGET_DIR);
        Path sourcePath = sourceFile.toPath();
        String relativePath = targetPath.relativize(sourcePath).toString().replace('\\', '/');
        String header = String.format("--- FILE_PATH: %s ---%n", relativePath);

        Files.writeString(outputPath, header, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        String content = Files.readString(sourcePath, StandardCharsets.UTF_8);
        Files.writeString(outputPath, content, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        Files.writeString(outputPath, "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }


    // ====================================================================
    //                      主执行方法 (main)
    // ====================================================================

    public static void main(String[] args) {
        Path targetDirPath = Paths.get(TARGET_DIR);
        if (!Files.isDirectory(targetDirPath)) {
            System.err.println("错误：目标目录不是一个有效的目录: " + targetDirPath.toAbsolutePath());
            return;
        }

        try {
            // --- 阶段 1: 移除注释 ---
            System.out.println("==========================================");
            System.out.println("         PHASE 1: REMOVING COMMENTS");
            System.out.println("==========================================");
            System.out.println("警告: 此过程将直接修改源文件！请确保已备份。");
            System.out.println("目标目录: " + targetDirPath.toAbsolutePath());
            System.out.println("排除规则 (不移除注释): 文件名以 'SourceCode' 开头");
            System.out.println("------------------------------------------");

            long startTime = System.currentTimeMillis();
            int cleanedCount = removeCommentsInDirectory(targetDirPath);
            long endTime = System.currentTimeMillis();

            System.out.println("------------------------------------------");
            System.out.println("注释移除完成！");
            System.out.println("总共清理了 " + cleanedCount + " 个文件。");
            System.out.println("耗时: " + (endTime - startTime) + " ms\n");


            // --- 阶段 2: 收集源代码 ---
            System.out.println("==========================================");
            System.out.println("      PHASE 2: COLLECTING SOURCE CODE");
            System.out.println("==========================================");

            Path outputPath = Paths.get(OUTPUT_FILE);
            Files.deleteIfExists(outputPath);
            Files.createFile(outputPath);

            File targetDirectory = new File(TARGET_DIR);
            List<File> allJavaFiles = new ArrayList<>();
            collectJavaFiles(targetDirectory, allJavaFiles);

            System.out.println("收集目标目录: " + TARGET_DIR);
            System.out.println("排除收集的文件: " + EXCLUDED_FILES_FOR_COLLECTION);
            System.out.println("排除收集的包: 'tools'");
            System.out.println("------------------------------------------");

            if (!allJavaFiles.isEmpty()) {
                System.out.println("找到 " + allJavaFiles.size() + " 个符合条件的Java文件准备收集...");
                for (File javaFile : allJavaFiles) {
                    appendFileContent(javaFile, outputPath);
                }
                System.out.println("------------------------------------------");
                System.out.println("成功收集 " + allJavaFiles.size() + " 个Java文件到: " + OUTPUT_FILE);
            } else {
                System.out.println("目录中未找到任何符合收集条件的Java文件。");
            }
            System.out.println("==========================================");
            System.out.println("所有任务执行完毕！");

        } catch (Exception e) {
            System.err.println("处理过程中出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}