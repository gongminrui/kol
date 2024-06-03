package kol.account.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author kent
 */
@Getter
@AllArgsConstructor
public enum ClientType {
    NOT("NOT"),
    CLIENT("client"),
    ADMIN("admin");
    private String type;

    public static ClientType of(String value) {
        if (StringUtils.isBlank(value)) return NOT;
        return Arrays.stream(values()).filter(v -> v.getType().equals(value)).findFirst().orElse(NOT);
    }
}
