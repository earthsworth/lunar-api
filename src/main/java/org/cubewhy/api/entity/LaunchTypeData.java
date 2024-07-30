package org.cubewhy.api.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LaunchTypeData {
    List<Artifact> artifacts;
    String mainClass;
    @Builder.Default
    boolean ichor = true;
}
