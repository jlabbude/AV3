package com.unijorge.devsoft.AV3;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.model.Coordinate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@EnableCaching
@RestController
@EnableAsync
public class Controller {

    private static final String NOISE_MAP = "noise_map.png";

    @Cacheable("map")
    @GetMapping("/pollution")
    public ResponseEntity<Resource> displayMap() {

        Resource resource = new FileSystemResource(NOISE_MAP);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }

}