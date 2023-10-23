package cn.natane.service;

import cn.natane.domain.Weather;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface WeatherService {

    Map<String, String> getCitiesCodes(Integer regionCode);

    Weather getWeather(Integer cityCode) throws IOException;

}
