package com.unijorge.devsoft.AV3;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.lang.NonNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class NoiseMapper {

    /**
     * 805 queries (1 por pixel),
     * 60 queries por minuto,
     * 14 keys necessárias para processar
     * o mapa sem esperar o rate limit acabar.
     * Atualmente 1 pixel = ~ 1km²
     * (aproximadamente a área de um bairro pequeno/médio)
     */

    //TODO:atualizar numeros

    public static final int IMAGE_WIDTH = 35;

    public static final int IMAGE_HEIGHT = 24;

    public static BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    public abstract void noiseMapMapper(String json, int longitude, int latitude) throws ParseException;

    public abstract Color mapValueToColor(int value);

    public abstract int getPollutionIndex(String json) throws ParseException;

    public abstract BufferedImage setTransparency(BufferedImage image);

}
