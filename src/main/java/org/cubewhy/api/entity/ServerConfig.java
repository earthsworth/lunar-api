package org.cubewhy.api.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ServerConfig {
    String lunarMain = "com.moonsworth.lunar.genesis.Genesis";
    List<String> extraArguments = new ArrayList<>(List.of(new String[]{"--add-modules", "jdk.naming.dns", "--add-exports", "jdk.naming.dns/com.sun.jndi.dns=java.naming", "-Djna.boot.library.path=natives", "-Dlog4j2.formatMsgNoLookups=true", "--add-opens", "java.base/java.io=ALL-UNNAMED", "-XX:+UseStringDeduplication"}));
}
