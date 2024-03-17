package com.unijorge.devsoft.AV3;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class NoiseMapGenerator {

        /**
         * 805 queries (1 por pixel),
         * 60 queries por minuto,
         * 14 keys necessárias para processar
         * o mapa sem esperar o rate limit acabar.
         * Atualmente 1 pixel = ~ 1km²
         * (aproximadamente a área de um bairro pequeno/médio)
         */
    public static final int IMAGE_WIDTH = 35;

    public static final int IMAGE_HEIGHT = 24;

    public BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    public void noiseMapMapper(String json, int longitude, int latitude) throws ParseException {

        Color color = mapValueToColor(getPollutionIndex(json));

        getImage().setRGB(longitude, latitude, color.getRGB());

    }

    private Color mapValueToColor(int value) {

        int cor = (value * (255/5));

        return new Color(cor, 0, 0);

    }

    public BufferedImage getImage(){
        return this.image;
    }

    public static BufferedImage setTransparency (BufferedImage image) {
        BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = transparentImage.createGraphics();
        g.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        g.drawImage(image, null, 0, 0);
        g.dispose();
        return transparentImage;
    }

    public int getPollutionIndex(String json) throws ParseException {

        org.json.simple.parser.JSONParser parser = new JSONParser();

        JSONObject jsonObject = (JSONObject) parser.parse(json);

        JSONArray jsonArray = (JSONArray) jsonObject.get("list");

        JSONObject listObject = (JSONObject) jsonArray.getFirst();

        JSONObject mainObject = (JSONObject) listObject.get("main");

        return ((Long) mainObject.get("aqi")).intValue();
    }

//    public int pollutionIndexMapper(String pollutionIndex) {
//        Map<String, Integer> indexMap = new HashMap<>();
//
//        indexMap.put("GOOD", 1);
//        indexMap.put("FAIR", 2);
//        indexMap.put("MODERATE", 3);
//        indexMap.put("POOR", 3);
//        indexMap.put("VERY_POOR", 4);
//
//        return indexMap.get(pollutionIndex);
//    }

}