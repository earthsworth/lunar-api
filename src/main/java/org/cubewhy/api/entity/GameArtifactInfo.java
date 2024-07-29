package org.cubewhy.api.entity;

import lombok.Data;

@Data
public class GameArtifactInfo {
    LaunchTypeData launchTypeData;
    Textures textures;
    RuntimeInfo jre;
}
