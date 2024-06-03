package kol.common.utils;


import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author kent
 */
public class BeanExtensionUtils extends BeanUtils {
    /**
     * Bean属性复制工具方法。
     *
     * @param sources   原始集合
     * @param supplier: 目标类::new(eg: UserVO::new)
     */
    public static <S, T> List<T> copyList(List<S> sources, Supplier<T> supplier) {
        List<T> list = new ArrayList<>(sources.size());
        for (S source : sources) {
            T t = supplier.get();
            copyProperties(source, t);
            list.add(t);
        }
        return list;
    }
}
