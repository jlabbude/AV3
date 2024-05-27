package com.unijorge.devsoft.AV3;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.model.Coordinate;
import jakarta.annotation.PostConstruct;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@EnableAsync
public class Mapper {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private static final String NOISE_MAP = "noise_map.png";

    @PostConstruct
    @Scheduled(fixedRate = 3600000)
    public void initMap(){
        new Thread(() -> {
            final Coordinate coordEnd = Coordinate.of(-90, 180);
            final Coordinate coordStart = Coordinate.of(90, -180);
            final double diff = 6;

            final String component = "pm10";

            PM10MapGenerator pm10MapGenerator = new PM10MapGenerator();

            CompletableFuture<Resource> futureResource = asyncRequest(coordStart, coordEnd, diff, component, pm10MapGenerator);

            futureResource.join();
        }).start();
    }

    @Async
    public CompletableFuture<Resource> asyncRequest(Coordinate coordStart,
                                                    Coordinate coordEnd,
                                                    final double diff,
                                                    final String component,
                                                    PM10MapGenerator pm10MapGenerator) {
        int keyIndex = 0;
        int rateLimiter = 0;
        List<String> keys = new Keys().keys();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        double latitude = coordStart.getLatitude();
        double longitude; // = coordStart.getLongitude(); <-- Removed variable initializer for optimization,
        //                                  keeping it as comment for clarity of what's going on

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
                    catch (HttpClientErrorException e) { logger.error("Erro de conexão"); }
                    catch (IllegalStateException e) { logger.error("Rate limit"); }
                    logger.debug("lat: {}, lon: {}", finalLatitude, finalLongitude);
                    logger.debug("json: {}", json);

                    try { pm10MapGenerator.noiseMapMapper(json, finalLongitudeImage, finalLatitudeImage, component); }
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
            ImageIO.write(pm10MapGenerator.setTransparency(pm10MapGenerator.getNewImage()), "png", new File(NOISE_MAP)); }
        catch (IOException e) { logger.error("Error writing image to file", e);  }

        Resource resource = new FileSystemResource(NOISE_MAP);

        return CompletableFuture.completedFuture(resource);
    }

}
