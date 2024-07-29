package org.cubewhy.api.entity;

import lombok.Data;

import java.util.List;

@Data
public class LaunchTypeData {
    List<Artifact> artifacts;
    String mainClass;
    boolean ichor = true;
}
