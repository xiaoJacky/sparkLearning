package com.learn.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by xiaojie on 17/6/7.
 */
public class SystemConfigTool implements Serializable {

    private static final long serialVersionUID = 4179549560142368883L;
    private static Gson gson = new GsonBuilder().create();

    private SystemConfigTool() {
    }

    /**
     * 加载Properties配置信息
     *
     * @param propertieFile
     * @return Properties
     */
    public static Properties initSystemProperties(String propertieFile) {
        InputStreamReader inputStream = null;
        Properties properties = new Properties();
        try {
            inputStream = new InputStreamReader(SystemConfigTool.class.getClassLoader().getResourceAsStream(propertieFile), "UTF-8");
            properties.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties;
    }

    /**
     * 加载Json配置信息
     * @param jsonFile
     * @return JSONObject
     */
    public static JsonObject initSystemJson(String jsonFile) {
        InputStreamReader inputStream = null;
        JsonObject jsonObject = null;
        try {
            inputStream = new InputStreamReader(SystemConfigTool.class.getClassLoader().getResourceAsStream(jsonFile), "UTF-8");
            jsonObject = gson.fromJson(inputStream, JsonObject.class);//将InputStreamReader转换成JsonObject
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

}
