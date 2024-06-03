package kol.common.config;

import kol.money.model.SettleModeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 全局配置项
 *
 * @author kent
 */
@Data
@Component
@ConfigurationProperties(prefix = "config")
public class GlobalConfig {
    private String key;
    private String secret;
    private String password;
    private Boolean simulated;
    private Boolean enableVip;
    private Boolean useProxy;
    private SettleModeEnum settleMode;


    /**
     * @return 是否账单模式
     */
    public boolean isBillMode() {
        return SettleModeEnum.BILL == settleMode;
    }

    /**
     * 是否实时结算模式
     *
     * @return
     */
    public boolean isRealTimeMode() {
        if (settleMode == null) {
            return true;
        }
        return SettleModeEnum.REAL_TIME == settleMode;
    }
}
