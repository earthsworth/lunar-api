package org.cubewhy.api;

import com.google.gson.Gson;
import org.cubewhy.api.entity.ServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileWriter;

@SpringBootApplication
public class LunarApiApplication {
    public static File CONFIG_DIR = new File("config");
    static File configFile = new File(CONFIG_DIR, "config.json");

    public static void main(String[] args) throws Exception{
        if (configFile.createNewFile()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(new Gson().toJson(new ServerConfig()));
            }
        }
        SpringApplication.run(LunarApiApplication.class, args);
    }

}
