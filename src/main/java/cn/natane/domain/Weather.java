package cn.natane.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Weather {
    private String cityName;
    private LocalDateTime dateTime;
    private String general;
    private String UV;
    private String advice;
    private List<DailyWeather> dailyWeatherList;




}
