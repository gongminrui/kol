package kol.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-22 20:27
 */
@Component
@Data
@ConfigurationProperties(prefix = "dispense")
public class DispenseConfig {
    private List<String> serverUrl;
}
