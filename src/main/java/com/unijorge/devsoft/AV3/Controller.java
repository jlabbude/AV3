package com.unijorge.devsoft.AV3;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.model.Coordinate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @GetMapping("/pollution")
    public ResponseEntity<String> collectData(CarbonOxideMapGenerator carbonOxideMapGenerator) {

        final Coordinate salvadorEnd = Coordinate.of(-13.017222, -38.534444);
        final Coordinate salvadorStart = Coordinate.of(-12.784722, -38.185202);
        final double diff = 0.01;

        final String component = "co";

        int keyIndex = 0;
        int rateLimiter = 0;
        List<String> keys = Keys();

        ExecutorService executor = Executors.newFixedThreadPool(10); // Adjust the number of threads based on your needs
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        JSONArray responses = new JSONArray();

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
                    Coordinate currentCoordinate = Coordinate.of(salvadorStart.getLatitude(), salvadorStart.getLongitude());
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
                    //String json = "[{\"coordinate\":{\"latitude\":40.71,\"longitude\":-74.01},\"airPollutionRecords\":[{\"forecastTime\":\"2024-03-14T17:16:37\", \"airQualityIndex\":\"FAIR\", \"o3\":94.41, \"co\":440.6, \"no\":8.05, \"no2\":37.01, \"so2\":8.94, \"pm2_5\":21.59, \"pm10\":27.09, \"nh3\":3.86, \"carbonMonoxide\":440.6, \"sulphurDioxide\":8.94, \"nitrogenDioxide\":37.01, \"coarseParticulateMatter\":27.09, \"fineParticlesMatter\":21.59, \"nitrogenMonoxide\":8.05, \"ozone\":94.41, \"ammonia\":3.86}]}]";
                    //String json = "{\"coord\":{\"lon\":-38.1852,\"lat\":-12.7847},\"list\":[{\"main\":{\"aqi\":1},\"components\":{\"co\":297.07,\"no\":0.08,\"no2\":0.35,\"o3\":37.19,\"so2\":0.77,\"pm2_5\":1.03,\"pm10\":4.8,\"nh3\":0.05},\"dt\":1710688227}]}";
                    catch (HttpClientErrorException e) { logger.error("Erro de conexão", e); }
                    logger.debug("json: {}", json);

                    try { carbonOxideMapGenerator.noiseMapMapper(json, finalLongitudeImage, finalLatitudeImage, component); }
                    catch (ParseException e) { throw new RuntimeException(e); }

                    JSONObject response = new JSONObject();
                    response.put(currentCoordinate.getLatitude() + "," + currentCoordinate.getLongitude(), json);
                    responses.add(response);

                }, executor);
                futures.add(future);
                rateLimiter++;
                if(rateLimiter == 60) { keyIndex++; rateLimiter = 0; }
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        String jsonOutput = responses.toJSONString();

        try { Files.writeString(Paths.get("output.json"), jsonOutput); }
        catch (IOException e) { logger.error("Error writing to file", e); }

        return ResponseEntity.ok().body(responses.toJSONString());
    }

    private List<String> Keys() {
        List<String> keys = new ArrayList<>();

        keys.add("e2ce3793c593a0d749bd61edc88077c2");
        keys.add("1c49c3f1d2afe3d8d4d88be2a43a1170");
        keys.add("50eafd1b3135ffb830f6df2d167ec5e7");
        keys.add("d0ed64c75b924f60bc876d734802952d");
        keys.add("8b932507812110b94ed5486256f3aafd");
        keys.add("2643d51a664f8a58f3972ba999141bad");
        keys.add("f2f4071bfc355f0cf0f231ada1f71a92");
        keys.add("a7557c887a036c809d0801d68d057429");
        keys.add("c7a5a9d64258e4f338c7df1f8d4f7d40");
        keys.add("e5b4a83b87f78bc831c90eb212a88f0e");
        keys.add("9ceec3ca92b46ffaca60f45c35096921");
        keys.add("82293113285318a9878190a9f0999464");
        keys.add("ff4b36362a6d48c8b8db774d3421083a");
        keys.add("a1bc0ccc1ae70ed51af37f0fd3ad7fcf");
        keys.add("e72bd190fdd8d900747fd6c27757f524");
        keys.add("183f84dd6edb2738031fd2a84508774e");

        return keys;
    }
}