import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunMapGenerator {
    public static void main(String[] args) {
        try {
            // 读取Kotlin脚本内容
            String scriptContent = new String(Files.readAllBytes(Paths.get("run-map-generator-direct.kts")));
            
            // 移除shebang行
            String cleanedContent = scriptContent.replace("#!/usr/bin/env kotlin\n", "");
            
            // 创建临时Kotlin文件
            File tempDir = new File("temp");
            tempDir.mkdirs();
            File tempFile = new File(tempDir, "Main.kt");
            Files.write(tempFile.toPath(), cleanedContent.getBytes());
            
            // 编译Kotlin文件
            Process compileProcess = Runtime.getRuntime().exec(
                "cmd /c kotlinc -cp \"C:\\Program Files\\Android\\Android Studio\\jbr\\lib\\*\" " + 
                tempFile.getAbsolutePath() + " -include-runtime -d map-generator.jar"
            );
            compileProcess.waitFor();
            
            // 运行编译后的jar文件
            Process runProcess = Runtime.getRuntime().exec("java -jar map-generator.jar");
            runProcess.waitFor();
            
            System.out.println("地图生成完成！");
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}