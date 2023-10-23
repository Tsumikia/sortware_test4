package cn.natane.domain;

import lombok.Data;

@Data
public class DailyWeather {
    private String data;
    private String temperature;
    private String description;
}
