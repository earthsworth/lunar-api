package org.cubewhy.api.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LunarVersion {
    String id;
    @SerializedName("default")
    @Builder.Default
    boolean isDefault = false;
    List<LunarSubVersion> subversions;
}
