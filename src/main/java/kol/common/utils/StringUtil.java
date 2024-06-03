package kol.common.utils;

import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-19 11:50
 */
public abstract class StringUtil {
    public static String replace(String value, Object... obj) {
        if (obj == null || obj.length == 0) {
            return value;
        }
        return replace(value, Arrays.asList(obj));
    }

    public static String replace(String value, List<Object> obj) {
        if (CollectionUtils.isEmpty(obj)) {
            return value;
        }
        String newset = value;
        for (int i = 0; i < obj.size(); i++) {
            newset = newset.replace(String.format("{%s}", i + 1), obj.get(i).toString());
        }

        return newset;
    }

    public static void main(String[] args) {
        System.out.println(replace("fdsaf {1}  {2} sda {1}", 11, 99));
    }
}
