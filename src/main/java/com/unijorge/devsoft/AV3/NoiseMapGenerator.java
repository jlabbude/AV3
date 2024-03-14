package com.unijorge.devsoft.AV3;

import com.github.prominence.openweathermap.api.enums.AirQualityIndex;
import com.github.prominence.openweathermap.api.model.air.pollution.AirPollutionRecord;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

import static com.unijorge.devsoft.AV3.Controller.salvadorEnd;
import static com.unijorge.devsoft.AV3.Controller.salvadorStart;

public class NoiseMapGenerator {
    private static final int IMAGE_WIDTH = 100;  // Adjust these values to match your grid size
    private static final int IMAGE_HEIGHT = 100;

    public void generateNoiseMap(List<AirPollutionRecord> records) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

        for (AirPollutionRecord record : records) {
            double longitude = salvadorStart.getLongitude();
            double latitude = salvadorEnd.getLatitude();
            AirQualityIndex value = record.getAirQualityIndex();

            Color color = mapValueToColor(value);
            image.setRGB(longitude, latitude, color.getRGB());
        }

        try {
            ImageIO.write(image, "png", new File("noise_map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Color mapValueToColor(double value) {
        int gray = (int) (255 * value / 100);  // Assuming value is in range 0-100
        return new Color(gray, gray, gray);
    }
}