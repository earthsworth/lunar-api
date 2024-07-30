package org.cubewhy.api.controller;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.cubewhy.api.entity.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.cubewhy.api.LunarApiApplication.config;

@RestController
@RequestMapping("/launcher")
public class LauncherController {
    @PostMapping("launch")
    public GameArtifactInfo launch(@NotNull HttpServletRequest request) throws Exception {
        LaunchRequest launchRequest = new Gson().fromJson(new String(request.getInputStream().readAllBytes()), LaunchRequest.class);
        List<Artifact> artifacts = new ArrayList<>();
        Textures textures = Textures.builder().build(); // todo parse textures from files
        // todo find artifacts
        return GameArtifactInfo.builder()
                .jre(RuntimeInfo.builder().extraArguments(config.getExtraArguments()).build())
                .textures(textures)
                .launchTypeData(LaunchTypeData.builder()
                        .mainClass(config.getLunarMain())
                        .artifacts(artifacts)
                        .build())
                .build();
        // todo
    }

    @GetMapping("metadata")
    public LauncherMetadata metadata() throws Exception {
        List<LauncherBlogpost> blogposts = new ArrayList<>();
        // todo
        return LauncherMetadata.builder()
                .blogPosts(blogposts)
                .build();
    }
}
