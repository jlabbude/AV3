package com.unijorge.devsoft.AV3;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.model.Coordinate;
import com.github.prominence.openweathermap.api.model.air.pollution.AirPollutionDetails;
import com.github.prominence.openweathermap.api.model.air.pollution.AirPollutionRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller {
    static final Coordinate salvadorStart = Coordinate.of(-12.798580, -38.525089);
    static final Coordinate salvadorEnd = Coordinate.of(-13.017439, -38.216442);
    private static final double diff = 0.01;

    @GetMapping("/pollution")
    public List<AirPollutionRecord> collectData() {

        List<AirPollutionRecord> records = new ArrayList<>();

        NoiseMapGenerator noiseMapGenerator = new NoiseMapGenerator();

        for (; salvadorStart.getLatitude() > salvadorEnd.getLatitude(); salvadorStart.setLatitude(salvadorStart.getLatitude() - diff)) {

            for (; salvadorStart.getLongitude() < salvadorEnd.getLongitude(); salvadorStart.setLongitude(salvadorStart.getLongitude() + diff)) {

                AirPollutionDetails airPollutionDetails = new OpenWeatherMapClient("e2ce3793c593a0d749bd61edc88077c2")
                    .airPollution()
                    .current()
                    .byCoordinate(Coordinate.of(salvadorStart.getLatitude(), salvadorStart.getLongitude()))
                    .retrieve()
                    .asJava();

                noiseMapGenerator.generateNoiseMap(airPollutionDetails);

                records.addAll(airPollutionDetails.getAirPollutionRecords());

            }
        }

        try {
            ImageIO.write(, "png", new File("noise_map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return records;
    }
}