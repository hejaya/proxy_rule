import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * TODO
 * @author guanzh
 * @time 2025/2/23 16:39
 */public class DomainExtractor {
    private final Set<String> domains = new LinkedHashSet<>(); // 保持顺序并去重
    private final String basePath; // 规则文件的根目录路径

    public DomainExtractor(String basePath) {
        this.basePath = basePath;
    }

    public Set<String> extractDomains(String filename) throws IOException {
        File file = new File(basePath, filename);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 移除行内注释和首尾空格
                line = line.split("#")[0].trim();
                if (line.isEmpty()) {
                    continue; // 跳过空行和纯注释行
                }
                if (line.startsWith("include:")) {
                    // 递归处理 include 文件，并保持顺序
                    String includedFile = line.substring("include:".length()).trim();
                    extractDomains(includedFile); // 先加载被包含文件的域名
                } else if (line.startsWith("domain:")) {
                    String domainPart = line.substring("domain:".length()).split("\\s+")[0];
                    addDomain(domainPart);
                } else if (line.startsWith("full:")) {
                    String domain = line.substring("full:".length()).split("\\s+")[0];
                    addDomain(domain);
                } else {
                    String domain = line.split("\\s+")[0];
                    if (isValidDomain(domain)) {
                        addDomain(domain);
                    }
                }
            }
        }
        return domains;
    }

    // 添加域名并去重（LinkedHashSet 自动处理）
    private void addDomain(String domain) {
        if (isValidDomain(domain)) {
            domains.add(domain); // 如果已存在，不会重复添加
        }
    }

    // 域名格式校验
    private boolean isValidDomain(String domain) {
        return domain.matches("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    // 新增方法：将结果写入文件
    public void saveToFile(Set<String> domains, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String domain : domains) {
                writer.write(domain);
                writer.newLine(); // 换行符
            }
        }
        System.out.println("域名已保存至文件: " + outputPath);
    }

    public static void main(String[] args) throws IOException {

        String tag = "category-ai-chat-!cn";
        String basePath = "./data";

        String outputFile = tag + ".txt"; // 输出文件名

        DomainExtractor extractor = new DomainExtractor(basePath);
        Set<String> domains = extractor.extractDomains(tag); // 入口文件

        System.out.println("domains size = " + domains.size());
        // 输出结果
        domains.forEach(a -> System.out.println("DOMAIN-SUFFIX," + a));


    }
}
