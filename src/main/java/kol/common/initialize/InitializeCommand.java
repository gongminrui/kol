package kol.common.initialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kol.common.config.GlobalConfig;
import kol.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Gongminrui
 * @date 2023-05-08 20:43
 */
@Component
@Slf4j
public class InitializeCommand implements CommandLineRunner {
    @Resource
    private GlobalConfig globalConfig;
    @Override
    public void run(String... args) throws Exception {
        printGitVersion();
        log.info("全局配置: {}", globalConfig);
    }

    private void printGitVersion() {
        JSONObject jsonObject = readGitProperties();
        String gitVersion = jsonObject.getOrDefault("git.commit.id.abbrev", "0000000").toString();
        String buildTime = jsonObject.getOrDefault("git.build.time", "00000000").toString();
        String buildUserName = jsonObject.getOrDefault("git.build.user.name", "00000000").toString();

        log.info("服务启动成功，git版本: {} ， 构建时间: {} ，构建用户名: {} ", gitVersion, buildTime, buildUserName);
    }

    private JSONObject readGitProperties() {
//        InputStream inputStream = null;
//        try {
//            ClassLoader classLoader = getClass().getClassLoader();
//            inputStream = classLoader.getResourceAsStream("git.properties");
//            // 读取文件内容，自定义一个方法实现即可
//            String versionJson = IOUtils.toString(inputStream);
//            return JSON.parseObject(versionJson);
//        } catch (Exception e) {
//            log.error("get git version info fail", e);
//        } finally {
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            } catch (Exception e) {
//                log.error("close inputstream fail", e);
//            }
//        }
        return new JSONObject();
    }
}
