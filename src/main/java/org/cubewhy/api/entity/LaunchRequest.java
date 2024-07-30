package org.cubewhy.api.entity;

import lombok.Data;

@Data
public class LaunchRequest {
    String version;
    String module;
    String branch;

    String os;
    String arch;
}
