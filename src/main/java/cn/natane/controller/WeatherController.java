package cn.natane.controller;

import cn.natane.domain.Weather;
import cn.natane.service.WeatherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.json.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class WeatherController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WeatherService weatherService;

    private static Pattern pattern = Pattern.compile("\\d+");

    @PostMapping("/getcities")
    public Map<String, String> getCities(@RequestBody String selectedRegion) {
        return getMatcher(selectedRegion);
    }

    private Map<String, String> getMatcher(@RequestBody String selectedRegion) {
        Matcher matcher = pattern.matcher(selectedRegion);
        if (matcher.find()) {
            int regionCode = Integer.parseInt(matcher.group());
            Map<String, String> citiesMap = weatherService.getCitiesCodes(regionCode);
            return citiesMap;
        } else {
            // Handle the case where no match is found
            return null;
        }
    }

    @PostMapping("/getWeather")
    public Weather getWeather(@RequestBody String selectedCity) throws IOException {
        Matcher matcher = pattern.matcher(selectedCity);
        if (matcher.find()) {
            int cityCode = Integer.parseInt(matcher.group());
            Weather weather = weatherService.getWeather(cityCode);
//            System.out.println(weather);
            return weather;
        } else {
            // Handle the case where no match is found
            return new Weather();
        }
    }

    public static void main(String[] args) throws JsonProcessingException {}

}
