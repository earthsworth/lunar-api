package org.cubewhy.api.entity;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class LauncherMetadata {
    List<LunarVersion> versions;
    @Builder.Default
    List<LauncherBlogpost> blogPosts = new ArrayList<>();
    Alert alert = null;
}
