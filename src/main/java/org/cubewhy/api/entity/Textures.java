package org.cubewhy.api.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Textures {
    String indexUrl;
    String indexSha1;
    String baseUrl;
}
