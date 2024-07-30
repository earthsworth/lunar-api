package org.cubewhy.api.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.cubewhy.api.entity.GameArtifactInfo;
import org.cubewhy.api.entity.LaunchRequest;
import org.cubewhy.api.entity.LaunchTypeData;
import org.cubewhy.api.entity.RuntimeInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.cubewhy.api.LunarApiApplication.config;

@RestController
@RequestMapping("/launcher")
public class LauncherController {
    @PostMapping("launch")
    public GameArtifactInfo launch(@NotNull HttpServletRequest request) throws Exception {
        LaunchRequest launchRequest = new Gson().fromJson(new String(request.getInputStream().readAllBytes()), LaunchRequest.class);
        return GameArtifactInfo.builder()
                .jre(RuntimeInfo.builder()
                        .extraArguments(config.getExtraArguments()).build())
                .launchTypeData(LaunchTypeData.builder()
                        .mainClass(config.getLunarMain())
                        .build())
                .build();
        // todo
    }
}
