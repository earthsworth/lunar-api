package org.cubewhy.api.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class LunarVersion {
    String id;
    @SerializedName("default")
    boolean isDefault;
    List<LunarSubVersion> subversions;
}
