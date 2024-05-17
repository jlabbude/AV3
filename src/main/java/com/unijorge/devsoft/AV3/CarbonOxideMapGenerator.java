package com.unijorge.devsoft.AV3;

import lombok.Getter;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class CarbonOxideMapGenerator implements NoiseMapper{

    @Getter
    public static BufferedImage newImage = image;


    @Override
    public void noiseMapMapper(String json, int longitude, int latitude, String component) throws ParseException {

        Color color = mapValueToColor(getPollutionIndexInComponents(json, component));

        getNewImage().setRGB(longitude, latitude, color.getRGB());

    }

    @Override
    public Color mapValueToColor(double value) {
        double minValue = 0;
        double maxValue = 16000;

        double normalizedValue = (value - minValue) / (maxValue - minValue);

        int r = (int) (255 * Math.pow(normalizedValue, 0.5));  // Adjust sensitivity with power function
        int g = 255 - (int) (255 * normalizedValue);
        int b = 0;

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return new Color(r, g, b);
    }


    @Override
    public BufferedImage setTransparency(BufferedImage image) {
        return NoiseMapper.super.setTransparency(image);
    }

    @Override
    public double getPollutionIndexInComponents(String json, String component) throws ParseException {
        return NoiseMapper.super.getPollutionIndexInComponents(json, component);
    }
}