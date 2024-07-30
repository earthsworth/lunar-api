package org.cubewhy.api;

import com.google.gson.Gson;
import org.cubewhy.api.entity.ServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@SpringBootApplication
public class LunarApiApplication {
    public static File CONFIG_DIR = new File("config");
    static File configFile = new File(CONFIG_DIR, "config.json");

    public static ServerConfig config;

    public static void main(String[] args) throws Exception{
        if (configFile.createNewFile()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(new Gson().toJson(new ServerConfig()));
            }
        }
        try (FileReader reader = new FileReader(configFile)) {
            config = new Gson().fromJson(reader, ServerConfig.class);
        }
        SpringApplication.run(LunarApiApplication.class, args);
    }

}
