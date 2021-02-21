package com.liugd.stock.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
    }

    public static <T> T parseObject(String jsonString, Class<T> clazz) throws IOException {
        T jsonObject = null;
        jsonObject = mapper.readValue(jsonString, clazz);
        return jsonObject;
    }

    public static String toString(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    public static String toStringNoException(Object object) {
        try {
            return mapper.writeValueAsString(object);
        }catch (IOException e){
            log.error("json转换失败.", e);
            return null;
        }

    }
}
