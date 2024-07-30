package org.cubewhy.api.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RuntimeInfo {
    List<String> extraArguments;
}
