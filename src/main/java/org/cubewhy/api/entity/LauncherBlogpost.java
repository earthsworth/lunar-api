package org.cubewhy.api.entity;

import lombok.Data;

@Data
public class LauncherBlogpost {
    String title;
    String excerpt;
    String image;
    String link;
    String author;
    String buttonText;
    ButtonType type = ButtonType.OPEN_LINK;

    enum ButtonType {
        OPEN_LINK,
        CHANGE_API
    }
}
