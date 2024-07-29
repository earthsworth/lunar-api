package org.cubewhy.api.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LauncherMetadata {
    List<LunarVersion> versions;
    List<LauncherBlogpost> blogPosts = new ArrayList<>();
    Alert alert = null;
}
