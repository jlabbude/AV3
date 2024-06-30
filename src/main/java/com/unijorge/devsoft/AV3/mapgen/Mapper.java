package com.unijorge.devsoft.AV3.mapgen;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.model.Coordinate;
import com.unijorge.devsoft.AV3.Keys;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.unijorge.devsoft.AV3.APIController.NOISE_DATA;
import static com.unijorge.devsoft.AV3.APIController.NOISE_MAP;

@Service
@EnableAsync
public class Mapper {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    public static final int diff = 6;

    //@PostConstruct
    //@Scheduled(fixedRate = 3600000)
    public void initMap(){
        new Thread(() -> {
            final Coordinate coordEnd = Coordinate.of(-90, 180);
            final Coordinate coordStart = Coordinate.of(90, -180);

            final String component = "pm10";

            PM10MapGenerator pm10MapGenerator = new PM10MapGenerator();

            CompletableFuture<Resource> futureResource =
                    dataRetriever(
                            coordStart,
                            coordEnd,
                            diff,
                            component,
                            pm10MapGenerator
                    );

            futureResource.join();
        }).start();
    }

    @Async
    public CompletableFuture<Resource> dataRetriever(Coordinate coordStart,
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

        JSONArray responses = new JSONArray();
        new File(NOISE_MAP).delete();

        for (int latitudeImage = 0;
             latitude > coordEnd.getLatitude();
             latitude -= diff, latitudeImage++) {

            // Reset back to start
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
                                        Coordinate.of(
                                                finalLatitude,
                                                finalLongitude
                                        )
                                )
                                .retrieve()
                                .asJSON();
                    }
                    catch (HttpClientErrorException e) { logger.error("Erro de conexão"); }
                    catch (IllegalStateException e) { logger.error("Rate limit"); return; }
                    logger.debug("lat: {}, lon: {}", finalLatitude, finalLongitude);
                    logger.debug("json: {}", json);

                    try {
                        pm10MapGenerator.noiseMapMapper(json, finalLongitudeImage, finalLatitudeImage, component);
                        JSONObject response = new JSONObject();
                        response.put(finalLongitude + "," + finalLatitude, json);
                        responses.add(response);
                    }
                    catch (ParseException e) { throw new RuntimeException(e); }
                });

                futures.add(future);

                rateLimiter++;
                if(keyIndex == keys.size() - 1 && rateLimiter == 60) keyIndex = 0;
                else if(rateLimiter == 60) { keyIndex++; rateLimiter = 0; }
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();


        try {
            Files.write(Paths.get(NOISE_DATA), responses.toJSONString().getBytes());
            ImageIO.write(pm10MapGenerator.setTransparency(pm10MapGenerator.getNewImage()), "png", new File(NOISE_MAP)); }
        catch (IOException e) { logger.error("Error writing image to file", e);  }

        Resource resource = new FileSystemResource(NOISE_MAP);

        return CompletableFuture.completedFuture(resource);
    }

}
