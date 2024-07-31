package org.cubewhy.api.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LunarModule {
    String id;
    @SerializedName("default")
    @Builder.Default
    boolean isDefault = false;
}
