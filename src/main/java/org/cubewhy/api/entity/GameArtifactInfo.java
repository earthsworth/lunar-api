package org.cubewhy.api.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameArtifactInfo {
    LaunchTypeData launchTypeData;
    Textures textures;
    RuntimeInfo jre;
}
