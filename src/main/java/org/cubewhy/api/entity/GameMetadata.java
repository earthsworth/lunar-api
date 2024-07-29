package org.cubewhy.api.entity;

import lombok.Data;

import java.util.List;

@Data
public class GameMetadata {
    List<GameBlogpost> blogPosts;
}
