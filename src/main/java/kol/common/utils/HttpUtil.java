package kol.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Gongminrui
 * @date 2023-03-19 11:13
 */
public final class HttpUtil {

    public static HttpServletRequest gerRequest() {
        return ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
    }

    public static String getHeader(String name) {
        return gerRequest().getHeader(name);
    }

    public static String getLanguage() {
        String language = getHeader("language");
        if (StringUtils.isBlank(language)) {
            language = "zh";
        }
        return language;
    }

    public static boolean isEn() {
        return "en".equals(getLanguage());
    }
}
