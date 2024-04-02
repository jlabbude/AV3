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
    public Color mapValueToColor(int value) {

        int cor = (int) (value * (255d / 2000d));


        int cor2 = 255 - (int) (255 * (value / 2000d));


        return new Color(255, cor, cor2);

    }

    @Override
    public BufferedImage setTransparency(BufferedImage image) {
        return NoiseMapper.super.setTransparency(image);
    }

    @Override
    public int getPollutionIndexInComponents(String json, String component) throws ParseException {
        return NoiseMapper.super.getPollutionIndexInComponents(json, component);
    }
}