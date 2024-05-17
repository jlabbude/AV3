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

        final Coordinate coordEnd = Coordinate.of(-90, 180);
        final Coordinate coordStart = Coordinate.of(90, -180);
        final double diff = 6;

        final String component = "co";

        CompletableFuture<Resource> futureResource = asyncRequest(coordStart, coordEnd, diff, component, carbonOxideMapGenerator);
        Resource resource = futureResource.join();

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }

    @Async
    public CompletableFuture<Resource> asyncRequest(Coordinate coordStart,
                                                    Coordinate coordEnd,
                                                    final double diff,
                                                    final String component,
                                                    CarbonOxideMapGenerator carbonOxideMapGenerator) {
        int keyIndex = 0;
        int rateLimiter = 0;
        List<String> keys = new Keys().keys();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        double latitude = coordStart.getLatitude();
        double longitude = coordStart.getLongitude();

        for (int latitudeImage = 0;
             latitude > coordEnd.getLatitude();
             latitude -= diff, latitudeImage++) {

            longitude = -180;
            for (int longitudeImage = 0;
                 longitude < coordEnd.getLongitude();
                 longitude += diff, longitudeImage++) {

                int finalKeyIndex = keyIndex;
                int finalLongitudeImage = longitudeImage;
                int finalLatitudeImage = latitudeImage;

                double finalLatitude = latitude;
                double finalLongitude = longitude;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    String json = null;
                    try {
                        json = new OpenWeatherMapClient(keys.get(finalKeyIndex))
                                .airPollution()
                                .current()
                                .byCoordinate(
                                        Coordinate.of(finalLatitude,
                                                finalLongitude))
                                .retrieve()
                                .asJSON();
                    }
                    catch (HttpClientErrorException e) { logger.error("Erro de conexão", e); }
                    logger.debug("lat: {}, lon: {}", finalLatitude, finalLongitude);
                    logger.debug("json: {}", json);

                    try { carbonOxideMapGenerator.noiseMapMapper(json, finalLongitudeImage, finalLatitudeImage, component); }
                    catch (ParseException e) { throw new RuntimeException(e); }
                });

                futures.add(future);

                rateLimiter++;
                if(keyIndex == keys.size() - 1 && rateLimiter == 60) keyIndex = 0;
                else if(rateLimiter == 60) { keyIndex++; rateLimiter = 0; }
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