package script;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * v2ray domain data to common rule file
 * @author gthree
 * @time 2025/2/23 16:39
 */public class DomainExtractor {

    private final Set<String> domains = new LinkedHashSet<>(); // 保持顺序并去重
    private final String basePath; // 规则文件的根目录路径

    public DomainExtractor(String basePath) {
        this.basePath = basePath;
    }

    /**
     * 提取域名
     * @author guanzh
     * @param filename
     * @param attributeParam @cn @!cn @ads
     * @return Set<String>
     * @time 2025/3/5 09:33
     */
    public Set<String> extractDomains(String filename, String attributeParam) throws IOException {
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
                    extractDomains(includedFile, attributeParam); // 先加载被包含文件的域名
                } else if (line.startsWith("domain:")) {
                    String[] split = line.substring("domain:".length()).split("\\s+");
                    String domainPart = split[0];
                    String attributePart = null;
                    if (split.length ==2){
                        attributePart = split[1];
                    }
                    if (checkDomainAttribute(attributeParam, attributePart)) {
                        addDomain(domainPart);
                    }
                } else if (line.startsWith("full:")) {
                    String[] split = line.substring("full:".length()).split("\\s+");
                    String domain = split[0];
                    String attributePart = null;
                    if (split.length ==2){
                        attributePart = split[1];
                    }
                    if (checkDomainAttribute(attributeParam, attributePart)) {
                        addDomain(domain);
                    }
                } else {
                    String[] split = line.split("\\s+");
                    String domain = split[0];
                    String attributePart = null;
                    if (split.length ==2){
                        attributePart = split[1];
                    }
                    if (isValidDomain(domain)) {
                        if (checkDomainAttribute(attributeParam, attributePart)) {
                            addDomain(domain);
                        }
                    }
                }
            }
        }
        return domains;
    }

    /**
     * 检查规则属性
     * @author guanzh
     * @param attributeParam @cn @!cn @ads
     * @param attribute
     * @return boolean
     * @time 2025/3/5 09:51
     */
    private boolean checkDomainAttribute(String attributeParam, String attribute) {
        if (attributeParam == null) return true;
        if (attribute == null) return true;
        return attributeParam.equals(attribute);
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

        //String tag = "cn";
        //String tag = "geolocation-!cn";
        //String tag = "category-ads-all";
        //String tag = "google";
        //String tag = "microsoft";
        //String tag = "category-dev";
        //String tag = "adobe";
        //String tag = "category-ai-chat-!cn";
        //String tag = "apple";
        //String tag = "github";
        //String tag = "yahoo";
        String tag = "youtube";

        String basePath = "/Users/gthree/dev/devProjects/myProjects/domain-list-community/data";

        String outputFile = tag + ".txt"; // 输出文件名
        DomainExtractor extractor = new DomainExtractor(basePath);
        //@cn @!cn @ads
        //Set<String> domains = extractor.extractDomains(tag, "@cn"); // 入口文件
        Set<String> domains = extractor.extractDomains(tag, "@!cn");
        //Set<String> domains = extractor.extractDomains(tag, "@ads");

        System.out.println("domains size = " + domains.size());
        // 输出结果
        domains.forEach(a -> System.out.println("DOMAIN-SUFFIX," + a));


    }
}
