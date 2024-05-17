package com.unijorge.devsoft.AV3;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.model.Coordinate;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@EnableAsync
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @GetMapping("/pollution")
    public ResponseEntity<Resource> collectData(CarbonOxideMapGenerator carbonOxideMapGenerator) {

        final Coordinate salvadorEnd = Coordinate.of(-13.017222, -38.534444);
        final Coordinate salvadorStart = Coordinate.of(-12.784722, -38.185202);
        final double diff = 0.01;

        final String component = "co";

        CompletableFuture<Resource> futureResource = asyncRequest(salvadorStart, salvadorEnd, diff, component, carbonOxideMapGenerator);
        Resource resource = futureResource.join();

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }

    @Async
    public CompletableFuture<Resource> asyncRequest(final Coordinate salvadorStart,
                                                    final Coordinate salvadorEnd,
                                                    final double diff,
                                                    final String component,
                                                    CarbonOxideMapGenerator carbonOxideMapGenerator) {
        int keyIndex = 0;
        int rateLimiter = 0;
        List<String> keys = new Keys().keys();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int latitudeImage = 0;
             salvadorStart.getLatitude() > salvadorEnd.getLatitude();
             salvadorStart.setLatitude(salvadorStart.getLatitude() - diff), latitudeImage++) {

            salvadorStart.setLongitude(-38.185202);
            for (int longitudeImage = 0;
                 salvadorStart.getLongitude() > salvadorEnd.getLongitude();
                 salvadorStart.setLongitude(salvadorStart.getLongitude() - diff), longitudeImage++) {

                int finalKeyIndex = keyIndex;
                int finalLongitudeImage = longitudeImage;
                int finalLatitudeImage = latitudeImage;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    String json = null;
                    try {
                        json = new OpenWeatherMapClient(keys.get(finalKeyIndex))
                                .airPollution()
                                .current()
                                .byCoordinate(
                                        Coordinate.of(salvadorStart.getLatitude(),
                                                      salvadorStart.getLongitude()))
                                .retrieve()
                                .asJSON();
                    }
                    catch (HttpClientErrorException e) { logger.error("Erro de conexão", e); }
                    logger.debug("json: {}", json);

                    try { carbonOxideMapGenerator.noiseMapMapper(json, finalLongitudeImage, finalLatitudeImage, component); }
                    catch (ParseException e) { throw new RuntimeException(e); }
                });

                futures.add(future);

                rateLimiter++;
                if(rateLimiter == 60) { keyIndex++; rateLimiter = 0; }
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        try { //noinspection AccessStaticViaInstance
            ImageIO.write(carbonOxideMapGenerator.setTransparency(carbonOxideMapGenerator.getNewImage()), "png", new File("noise_map.png")); }
        catch (IOException e) { logger.error("Error writing image to file", e);  }

        Resource resource = new FileSystemResource("noise_map.png");

        return CompletableFuture.completedFuture(resource);
    }
}