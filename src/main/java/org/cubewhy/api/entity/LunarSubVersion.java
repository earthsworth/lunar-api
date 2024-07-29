package org.cubewhy.api.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class LunarSubVersion {
    String id;
    @SerializedName("default")
    boolean isDefault;
    List<LunarModule> modules;
}
