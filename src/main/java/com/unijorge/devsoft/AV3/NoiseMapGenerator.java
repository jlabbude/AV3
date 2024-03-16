package com.unijorge.devsoft.AV3;

import com.github.prominence.openweathermap.api.enums.AirQualityIndex;
import com.github.prominence.openweathermap.api.model.air.pollution.AirPollutionDetails;
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

        public static final int IMAGE_WIDTH = 31;
        public static final int IMAGE_HEIGHT = 22;

        public BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    public void generateNoiseMap(AirPollutionDetails airPollutionDetails) {

        double longitude = salvadorStart.getLongitude();
        double latitude = salvadorEnd.getLatitude();

        Color color = mapValueToColor();

        //todo turn double into int
        getImage().setRGB(longitude, latitude, color.getRGB());
    }

    private Color mapValueToColor(int value) {
        int gray = (255 * value / 4);
        return new Color(gray, gray, gray);
    }

    public BufferedImage getImage(){
        return this.image;
    }

}