package kol.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Jackson工具类
 * 优势：
 * 数据量高于百万的时候，速度和FastJson相差极小
 * API和注解支持最完善，可定制性最强
 * 支持的数据源最广泛（字符串，对象，文件、流、URL）
 *
 * @author duanxinyuan
 * 2018/6/28 23:24
 */
@Slf4j
public class JsonUtils {
    private static ObjectMapper mapper;

    private static final Set<JsonReadFeature> JSON_READ_FEATURES_ENABLED = Set.of(
            //允许在JSON中使用Java注释
            JsonReadFeature.ALLOW_JAVA_COMMENTS,
            //允许 json 存在没用双引号括起来的 field
            JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES,
            //允许 json 存在使用单引号括起来的 field
            JsonReadFeature.ALLOW_SINGLE_QUOTES,
            //允许 json 存在没用引号括起来的 ascii 控制字符
            JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS,
            //允许 json number 类型的数存在前导 0 (例: 0001)
            JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS,
            //允许 json 存在 NaN, INF, -INF 作为 number 类型
            JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS,
            //允许 只有Key没有Value的情况
            JsonReadFeature.ALLOW_MISSING_VALUES,
            //允许数组json的结尾多逗号
            JsonReadFeature.ALLOW_TRAILING_COMMA
    );

    static {
        try {
            //初始化
            mapper = initMapper();
        } catch (Exception e) {
            log.error("jackson config error", e);
        }
    }

    public static ObjectMapper initMapper() {
        JsonMapper.Builder builder = JsonMapper.builder().enable(JSON_READ_FEATURES_ENABLED.toArray(new JsonReadFeature[0]));
        return initMapperConfig(builder.build());
    }

    public static ObjectMapper initMapperConfig(ObjectMapper objectMapper) {
        String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        objectMapper.setDateFormat(new SimpleDateFormat(dateTimeFormat));
        //配置序列化级别
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //配置JSON缩进支持
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        //允许单个数值当做数组处理
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        //禁止重复键, 抛出异常
        objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        //禁止使用int代表Enum的order()來反序列化Enum, 抛出异常
        objectMapper.enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
        //有属性不能映射的时候不报错
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //对象为空时不抛异常
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //时间格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //允许未知字段
        objectMapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
        //序列化BigDecimal时之间输出原始数字还是科学计数, 默认false, 即是否以toPlainString()科学计数方式来输出
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        //识别Java8时间
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.registerModule(new Jdk8Module());
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat)))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(dateTimeFormat)));
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    /**
     * JSON反序列化
     */
    public static <T> T jsonToPojo(String json, Class<T> type) {
        return jsonToPojo(json, (Type) type);
    }

    /**
     * JSON反序列化
     */
    public static <T> T jsonToPojo(String json, TypeReference<T> type) {
        return jsonToPojo(json, type.getType());
    }

    /**
     * JSON反序列化
     */
    public static <T> T jsonToPojo(String json, Type type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            log.error("jackson from error, json: {}, type: {}", json, type, e);
        }
        return null;
    }

    /**
     * JSON反序列化（List）
     */
    public static <T> List<T> fromList(String json, Class<T> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return mapper.readValue(json, collectionType);
        } catch (IOException e) {
            log.error("jackson from error, json: {}, type: {}", json, type, e);
        }
        return null;
    }

    /**
     * JSON反序列化（Map）
     */
    public static Map<String, Object> fromMap(String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
            return mapper.readValue(json, mapType);
        } catch (IOException e) {
            log.error("jackson from error, json: {}, type: {}", json, e);
        }
        return null;
    }

    /**
     * 序列化为JSON
     */
    public static <T> String objectToJson(List<T> list) {
        try {
            return mapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("jackson to error, data: {}", list, e);
        }
        return null;
    }

    /**
     * 序列化为JSON
     */
    public static <T> String objectToJson(T t) {
        try {
            return mapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            log.error("jackson to error, data: {}", t, e);
        }
        return null;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return String，默认为 null
     */
    public static String getAsString(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return null;
            }
            return getAsString(jsonNode);
        } catch (Exception e) {
            log.error("jackson get string error, json: {}, key: {}", json, key, e);
        }
        return null;
    }

    private static String getAsString(JsonNode jsonNode) {
        return jsonNode.isTextual() ? jsonNode.textValue() : jsonNode.toString();
    }

    /**
     * 从json串中获取某个字段
     *
     * @return int，默认为 0
     */
    public static int getAsInt(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0;
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return 0;
            }
            return jsonNode.isInt() ? jsonNode.intValue() : Integer.parseInt(getAsString(jsonNode));
        } catch (Exception e) {
            log.error("jackson get int error, json: {}, key: {}", json, key, e);
        }
        return 0;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return long，默认为 0
     */
    public static long getAsLong(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0L;
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return 0L;
            }
            return jsonNode.isLong() ? jsonNode.longValue() : Long.parseLong(getAsString(jsonNode));
        } catch (Exception e) {
            log.error("jackson get long error, json: {}, key: {}", json, key, e);
        }
        return 0L;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return double，默认为 0.0
     */
    public static double getAsDouble(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0.0;
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return 0.0;
            }
            return jsonNode.isDouble() ? jsonNode.doubleValue() : Double.parseDouble(getAsString(jsonNode));
        } catch (Exception e) {
            log.error("jackson get double error, json: {}, key: {}", json, key, e);
        }
        return 0d;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return BigDecimal，默认为 0.00
     */
    public static BigDecimal getAsBigDecimal(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return new BigDecimal("0.00");
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return new BigDecimal("0.00");
            }
            return jsonNode.isBigDecimal() ? jsonNode.decimalValue() : new BigDecimal(getAsString(jsonNode));
        } catch (Exception e) {
            log.error("jackson get big decimal error, json: {}, key: {}", json, key, e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return boolean, 默认为false
     */
    public static boolean getAsBoolean(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return false;
            }
            if (jsonNode.isBoolean()) {
                return jsonNode.booleanValue();
            } else {
                if (jsonNode.isTextual()) {
                    String textValue = jsonNode.textValue();
                    if ("1".equals(textValue)) {
                        return true;
                    } else {
                        return BooleanUtils.toBoolean(textValue);
                    }
                } else {//number
                    return BooleanUtils.toBoolean(jsonNode.intValue());
                }
            }
        } catch (Exception e) {
            log.error("jackson get boolean error, json: {}, key: {}", json, key, e);
        }
        return false;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return byte[], 默认为 null
     */
    public static byte[] getAsBytes(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return null;
            }
            return jsonNode.isBinary() ? jsonNode.binaryValue() : getAsString(jsonNode).getBytes();
        } catch (Exception e) {
            log.error("jackson get byte error, json: {}, key: {}", json, key, e);
        }
        return null;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return object, 默认为 null
     */
    public static <T> T getAsObject(String json, String key, Class<T> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return null;
            }
            JavaType javaType = mapper.getTypeFactory().constructType(type);
            return jsonToPojo(getAsString(jsonNode), javaType);
        } catch (Exception e) {
            log.error("jackson get list error, json: {}, key: {}, type: {}", json, key, type, e);
        }
        return null;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return list, 默认为 null
     */
    public static <T> List<T> getAsList(String json, String key, Class<T> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            JsonNode jsonNode = getJsonNode(json, key);
            if (null == jsonNode) {
                return null;
            }
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return jsonToPojo(getAsString(jsonNode), collectionType);
        } catch (Exception e) {
            log.error("jackson get list error, json: {}, key: {}, type: {}", json, key, type, e);
        }
        return null;
    }

    /**
     * 从json串中获取某个字段
     *
     * @return JsonNode, 默认为 null
     */
    public static JsonNode getJsonNode(String json, String key) {
        try {
            JsonNode node = mapper.readTree(json);
            if (null == node) {
                return null;
            }
            return node.get(key);
        } catch (IOException e) {
            log.error("jackson get object from json error, json: {}, key: {}", json, key, e);
        }
        return null;
    }

    public static JsonNode getJsonNode(String json) {
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            log.error("jackson get object from json error, json: {}", json, e);
        }
        return null;
    }

    /**
     * 向json中添加属性
     *
     * @return json
     */
    public static <T> String add(String json, String key, T value) {
        try {
            JsonNode node = mapper.readTree(json);
            add(node, key, value);
            return node.toString();
        } catch (IOException e) {
            log.error("jackson add error, json: {}, key: {}, value: {}", json, key, value, e);
        }
        return null;
    }

    /**
     * 向json中添加属性
     */
    private static <T> void add(JsonNode jsonNode, String key, T value) {
        if (value instanceof String) {
            ((ObjectNode) jsonNode).put(key, (String) value);
        } else if (value instanceof Short) {
            ((ObjectNode) jsonNode).put(key, (Short) value);
        } else if (value instanceof Integer) {
            ((ObjectNode) jsonNode).put(key, (Integer) value);
        } else if (value instanceof Long) {
            ((ObjectNode) jsonNode).put(key, (Long) value);
        } else if (value instanceof Float) {
            ((ObjectNode) jsonNode).put(key, (Float) value);
        } else if (value instanceof Double) {
            ((ObjectNode) jsonNode).put(key, (Double) value);
        } else if (value instanceof BigDecimal) {
            ((ObjectNode) jsonNode).put(key, (BigDecimal) value);
        } else if (value instanceof BigInteger) {
            ((ObjectNode) jsonNode).put(key, (BigInteger) value);
        } else if (value instanceof Boolean) {
            ((ObjectNode) jsonNode).put(key, (Boolean) value);
        } else if (value instanceof byte[]) {
            ((ObjectNode) jsonNode).put(key, (byte[]) value);
        } else {
            ((ObjectNode) jsonNode).put(key, objectToJson(value));
        }
    }

    /**
     * 除去json中的某个属性
     *
     * @return json
     */
    public static String remove(String json, String key) {
        try {
            JsonNode node = mapper.readTree(json);
            ((ObjectNode) node).remove(key);
            return node.toString();
        } catch (IOException e) {
            log.error("jackson remove error, json: {}, key: {}", json, key, e);
        }
        return null;
    }

    /**
     * 修改json中的属性
     */
    public static <T> String update(String json, String key, T value) {
        try {
            JsonNode node = mapper.readTree(json);
            ((ObjectNode) node).remove(key);
            add(node, key, value);
            return node.toString();
        } catch (IOException e) {
            log.error("jackson update error, json: {}, key: {}, value: {}", json, key, value, e);
        }
        return null;
    }

    /**
     * 格式化Json(美化)
     *
     * @return json
     */
    public static String format(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (IOException e) {
            log.error("jackson format json error, json: {}", json, e);
        }
        return null;
    }

    /**
     * 判断字符串是否是json
     *
     * @return json
     */
    public static boolean isJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
