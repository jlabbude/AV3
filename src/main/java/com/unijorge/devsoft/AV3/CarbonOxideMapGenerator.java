package com.unijorge.devsoft.AV3;

import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class CarbonOxideMapGenerator extends NoiseMapper{

        /**
         * 805 queries (1 por pixel),
         * 60 queries por minuto,
         * 14 keys necessárias para processar
         * o mapa sem esperar o rate limit acabar.
         * Atualmente 1 pixel = ~ 1km²
         * (aproximadamente a área de um bairro pequeno/médio)
         */

    @Getter
    public static BufferedImage newImage = image;


    @Override
    public void noiseMapMapper(String json, int longitude, int latitude) throws ParseException {

        Color color = mapValueToColor(getPollutionIndex(json));

        getNewImage().setRGB(longitude, latitude, color.getRGB());

    }

    @Override
    public Color mapValueToColor(int value) {

        int cor = (int) (value * (255d / 2000d));


        int cor2 = 255 - (int) (255 * (value / 2000d));


        return new Color(255, cor, cor2);

    }

    public BufferedImage setTransparency(BufferedImage image) {
        BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = transparentImage.createGraphics();
        g.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        g.drawImage(image, null, 0, 0);
        g.dispose();
        return transparentImage;
    }

    @Override
    public int getPollutionIndex(String json) throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);

        JSONArray listArray = (JSONArray) jsonObject.get("list");
        JSONObject firstElement = (JSONObject) listArray.getFirst();
        JSONObject components = (JSONObject) firstElement.get("components");


        //solução ruim, mas estou com sono demais para dar troubleshoot nisso agora
        Object coValue = components.get("co");
        if (coValue instanceof Long) {
            return ((Long) coValue).intValue();
        } else if (coValue instanceof Double) {
            return ((Double) coValue).intValue();
        } else {
            throw new IllegalArgumentException("Invalid type for co value in JSON data");
        }
    }

}