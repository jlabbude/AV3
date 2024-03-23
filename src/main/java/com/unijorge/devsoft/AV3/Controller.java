package com.unijorge.devsoft.AV3;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.exception.NoDataFoundException;
import com.github.prominence.openweathermap.api.model.Coordinate;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @GetMapping("/pollution")
    public ResponseEntity<Resource> collectData(CarbonOxideMapGenerator carbonOxideMapGenerator) {

        final Coordinate saoPauloEnd = Coordinate.of(-23.764444, -46.927778);
        final Coordinate saoPauloStart = Coordinate.of(-23.406389, -46.281944);
        final double diff = 0.001;

        int keyIndex = 0;
        int rateLimiter = 0;
        List<String> keys = getKeys();

        for (int latitudeImage = 0;
             saoPauloStart.getLatitude() > saoPauloEnd.getLatitude();
             saoPauloStart.setLatitude(saoPauloStart.getLatitude() - diff),
             latitudeImage++) {

            saoPauloStart.setLongitude(-46.281944);

            for (int longitudeImage = 0;
                 saoPauloStart.getLongitude() > saoPauloEnd.getLongitude();) {

                String json;
                try {
                     json = new OpenWeatherMapClient(keys.get(keyIndex))
                            .airPollution()
                            .current()
                            .byCoordinate(Coordinate.of(saoPauloStart.getLatitude(), saoPauloStart.getLongitude()))
                            .retrieve()
                            .asJSON();

                    saoPauloStart.setLongitude(saoPauloStart.getLongitude() - diff);
                    longitudeImage++;

                } catch (Exception e) {
                    if (e instanceof SocketTimeoutException) {

                        logger.debug("Connection timed out com a API", e);

                        continue;
                    } else if (e instanceof NoDataFoundException) {

                            logger.debug("No data found", e);

                            continue;
                    }
                    else {
                        throw new RuntimeException(e);
                    }
                }

                rateLimiter++;

                if(rateLimiter == 60) { keyIndex++; rateLimiter = 0; }

                if(keyIndex == keys.size()) { keyIndex = 0; }

                //String json = "[{\"coordinate\":{\"latitude\":40.71,\"longitude\":-74.01},\"airPollutionRecords\":[{\"forecastTime\":\"2024-03-14T17:16:37\", \"airQualityIndex\":\"FAIR\", \"o3\":94.41, \"co\":440.6, \"no\":8.05, \"no2\":37.01, \"so2\":8.94, \"pm2_5\":21.59, \"pm10\":27.09, \"nh3\":3.86, \"carbonMonoxide\":440.6, \"sulphurDioxide\":8.94, \"nitrogenDioxide\":37.01, \"coarseParticulateMatter\":27.09, \"fineParticlesMatter\":21.59, \"nitrogenMonoxide\":8.05, \"ozone\":94.41, \"ammonia\":3.86}]}]";
                //String json = "{\"coord\":{\"lon\":-38.1852,\"lat\":-12.7847},\"list\":[{\"main\":{\"aqi\":1},\"components\":{\"co\":297.07,\"no\":0.08,\"no2\":0.35,\"o3\":37.19,\"so2\":0.77,\"pm2_5\":1.03,\"pm10\":4.8,\"nh3\":0.05},\"dt\":1710688227}]}";


                logger.debug("json: {}", json);
                logger.debug("json: {}", latitudeImage);
                logger.debug("json: {}", longitudeImage);

                try { carbonOxideMapGenerator.noiseMapMapper(json, longitudeImage, latitudeImage); }
                catch (ParseException e) { throw new RuntimeException(e); }
            }
        }

        try { ImageIO.write(carbonOxideMapGenerator.setTransparency(carbonOxideMapGenerator.getNewImage()), "png", new File("noise_map.png")); }
        catch (IOException e) { e.getCause(); }


        org.springframework.core.io.Resource resource = new FileSystemResource("noise_map.png");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);

    }

    private List<String> getKeys() {
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