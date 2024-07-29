package org.cubewhy.api.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class LunarModule {
    String id;
    @SerializedName("default")
    boolean isDefault;

    // todo isDefault
}
