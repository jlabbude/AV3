package com.unijorge.devsoft.AV3.mapgen;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface NoiseMapper {

    /**
     * 805 queries (1 por pixel),
     * 60 queries por minuto,
     * 14 keys necessárias para processar
     * o mapa sem esperar o rate limit acabar.<br>
     * Atualmente 1 pixel = ~ 1km²<br>
     * (aproximadamente a área de um bairro pequeno/médio)
     */

    //TODO:atualizar números

    int IMAGE_WIDTH = 60;

    int IMAGE_HEIGHT = 30;

    BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);

    default void noiseMapMapper(String json, int longitude, int latitude, String component) throws ParseException{
        Color color = mapValueToColor(getPollutionIndexInComponents(json, component));

        image.setRGB(longitude, latitude, color.getRGB());
    }

    //mapValueToColor precisa ser abstrato para habilitar que subclasses
    //implementem diferentes formas de calcular a cor baseado no valor máximo retornado
    Color mapValueToColor(double value);

    /**
    * Dentro do elemento components encontrado no JSON retornado pelo método asJSON() da classe OpenWeatherMapClient,
     * é possível encontrar os seguintes componentes:<br><br>
     * "co": monóxido de carbono<br>
     * "no": monóxido de nitrogênio<br>
     * "no2": dióxido de nitrogênio<br>
     * "o3": ozônio<br>
     * "so2": dióxido de enxofre<br>
     * "nh3": amônia<br>
     * "pm2_5": partículas de até 2,5 micrômetros<br>
     * "pm10": partículas de até 10 micrômetros<br><br>
     * Utilize um desses componentes como String no método abaixo para obter o índice de poluição correspondente.
    */

    default double getPollutionIndexInComponents(String json, String component) throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);

        JSONArray listArray = (JSONArray) jsonObject.get("list");
        JSONObject firstElement = (JSONObject) listArray.getFirst();
        JSONObject components = (JSONObject) firstElement.get("components");

        return ((Number) components.get(component)).doubleValue();
    }

    default BufferedImage setTransparency(BufferedImage image){
        BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = transparentImage.createGraphics();
        g.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        g.drawImage(image, null, 0, 0);
        g.dispose();
        return transparentImage;
    }

}
