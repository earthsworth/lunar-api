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
        Textures textures = Textures.builder().build(); // todo parse textures from files
        // todo find artifacts
        return GameArtifactInfo.builder()
                .jre(RuntimeInfo.builder().extraArguments(config.getExtraArguments()).build())
                .textures(textures)
                .launchTypeData(LaunchTypeData.builder()
                        .mainClass(config.getLunarMain())
                        .artifacts(findArtifacts(launchRequest))
                        .build())
                .build();
        // todo
    }

    @GetMapping("metadata")
    public LauncherMetadata metadata() throws Exception {
        return LauncherMetadata.builder()
                .versions(findVersions())
                .blogPosts(findBlogposts())
                .alert(config.getAlert())
                .build();
    }

    private List<LunarVersion> findVersions() throws Exception {
        return List.of(); // todo
    }

    private List<LauncherBlogpost> findBlogposts() throws Exception {
        return List.of(); // todo
    }

    private List<Artifact> findArtifacts(LaunchRequest launchRequest) throws Exception {
        return List.of(); // todo
    }
}
