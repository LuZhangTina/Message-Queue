package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by tina on 2019/2/12.
 */
public class ConfigProperties {
    private InputStream inputStream;
    private Properties properties;

    public ConfigProperties(String filePath) {
        try {
            this.inputStream = new FileInputStream(filePath);
            this.properties = new Properties();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Properties getProperties() {
        return this.properties;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public String readConfig() {
        InputStream inputStream = getInputStream();
        Properties properties = getProperties();
        String serviceType = "";
        try {
            properties.load(inputStream);
            serviceType = properties.getProperty("queueServiceType");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return serviceType;
    }
}
