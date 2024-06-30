package com.unijorge.devsoft.AV3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.File;

import static com.unijorge.devsoft.AV3.Mapper.diff;

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
    @ResponseBody
    public ResponseEntity<String> data(@RequestParam float x, @RequestParam float y) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(NOISE_DATA));


        // Since diff is probably always going to be in between values requested from the map,
        // this way it can always return a value.
        float roundedX = Math.round(x / diff) * diff;
        float roundedY = Math.round(y / diff) * diff;

        String coordXY = roundedX+","+roundedY;

        for (JsonNode node : root) {
            if (node.get(coordXY) != null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body(node.get(coordXY).asText());
            }
        }

        return ResponseEntity.notFound().build();

    }


    @RequestMapping(value="/map")
    public String map() {
        return "forward:map.html";
    }
}


