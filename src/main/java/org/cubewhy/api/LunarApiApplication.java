package org.cubewhy.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    public static File versionsDir = new File(CONFIG_DIR, "versions");

    public static ServerConfig config;
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws Exception{
        CONFIG_DIR.mkdirs();
        if (configFile.createNewFile()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(gson.toJson(new ServerConfig()));
            }
        }
        versionsDir.mkdirs();
        try (FileReader reader = new FileReader(configFile)) {
            config = gson.fromJson(reader, ServerConfig.class);
        }
        SpringApplication.run(LunarApiApplication.class, args);
    }

}
