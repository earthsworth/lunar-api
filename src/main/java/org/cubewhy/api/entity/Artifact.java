package org.cubewhy.api.entity;

import lombok.Data;

@Data
public class Artifact {
    String name;
    String sha1;
    String url;
    ArtifactType type;

    enum ArtifactType {
        CLASS_PATH, EXTERNAL_FILE, NATIVES, JAVAAGENT
    }
}
