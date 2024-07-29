package org.cubewhy.api.entity;

import lombok.Data;

import java.util.List;

@Data
public class RuntimeInfo {
    List<String> extraArguments;
}
