package com.unijorge.devsoft.AV3;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableCaching
@org.springframework.stereotype.Controller
@EnableAsync
public class Controller implements WebMvcConfigurer {

    public static final String NOISE_MAP = "noise_map.png";
    public static final String NOISE_DATA = "noise_data.json";

    @GetMapping("/pollution")
    public ResponseEntity<Resource> displayMap() {

        Resource resource = new FileSystemResource(NOISE_MAP);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource);
    }

    @GetMapping("/data")
    public ResponseEntity<Resource> data() {

        Resource resource = new FileSystemResource(NOISE_DATA);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(resource);
    }

    @RequestMapping(value="/map")
    public String map() {
        return "forward:map.html";
    }
}


