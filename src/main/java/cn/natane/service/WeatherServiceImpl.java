package cn.natane.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cn.natane.domain.DailyWeather;
import cn.natane.domain.Weather;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Override
    public Map<String, String> getCitiesCodes(Integer regionCode) {
        Map<String, String> citiesCodes = new HashMap<>();

        try {
            // 构建请求参数
            String requestBody = "theRegionCode=" + regionCode;

            // 发送 POST 请求
            URL url = new URL("http://ws.webxml.com.cn/WebServices/WeatherWS.asmx/getSupportCityString");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 读取返回数据
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // 解析返回的 XML 数据
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(response.toString())));

                NodeList nodeList = document.getElementsByTagName("string");

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);
                    String data = element.getTextContent();
                    String[] parts = data.split(",");
                    if (parts.length == 2) {
                        citiesCodes.put(parts[0], parts[1]);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return citiesCodes;
    }

    public Weather getWeather(Integer cityCode) throws IOException {
        try {
            // 构建 POST 请求
            URL url = new URL("http://ws.webxml.com.cn/WebServices/WeatherWS.asmx/getWeather");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 构建请求体
            String requestBody = "theCityCode=" + cityCode + "&theUserID=";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应流
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }


                    String responseData = response.toString();
//                    System.out.println("Response Data: " + responseData);

                    JSONObject json = XML.toJSONObject(responseData);

                    JSONArray jsonArray = json.getJSONObject("ArrayOfString").getJSONArray("string");
                    List<String> data = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (jsonArray.get(i) instanceof String) {
                            data.add(jsonArray.getString(i));
                        }else {
                            data.add(String.valueOf(jsonArray.getInt(i)));
                        }
                    }
//                    for (String datum : data) {
//                        System.out.println(datum);
//                    }
//                    System.out.println(data.size());

                    Weather weather = new Weather();

                    weather.setCityName(data.get(1));

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(data.get(3), formatter);
                    // 提取年月日部分
                    LocalDateTime datePart = localDateTime.toLocalDate().atStartOfDay();
                    weather.setDateTime(datePart);

                    weather.setGeneral(data.get(4));
                    weather.setUV(data.get(5));
                    weather.setAdvice(data.get(6));


                    int dailyWeatherListSize = (data.size() - 7) / 5;
                    List<DailyWeather> dailyWeatherList = new ArrayList<>(dailyWeatherListSize);
                    for (int i = 7; i < data.size(); i += 5) {
                        DailyWeather dailyWeather = new DailyWeather();
                        dailyWeather.setData(data.get(i));
                        dailyWeather.setTemperature(data.get(i + 1));
                        dailyWeather.setDescription(data.get(i + 2));
                        dailyWeatherList.add(dailyWeather);
                    }
                    weather.setDailyWeatherList(dailyWeatherList);

                    return weather;






                } catch (IOException e) {
                    // 处理读取响应流时的异常
                    e.printStackTrace();
                }
            } else {
                // 处理响应码不为 HTTP_OK 的情况
                System.out.println("HTTP Response Code: " + responseCode);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Weather parseWeather(Document document) {
        Weather weather = new Weather();

        NodeList stringNodes = document.getElementsByTagName("string");
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Node stringNode = stringNodes.item(i);
            if (stringNode.getNodeType() == Node.ELEMENT_NODE) {
                Element stringElement = (Element) stringNode;
                String stringValue = stringElement.getTextContent();

                // 根据位置解析对应的字段
                switch (i) {
                    case 0:
                        weather.setCityName(stringValue);
                        break;
                    case 3:
                        weather.setDateTime(LocalDateTime.parse(stringValue));
                        break;
                    case 4:
                        weather.setGeneral(stringValue);
                        break;
                    case 5:
                        weather.setUV(stringValue);
                        break;
                    case 6:
                        weather.setAdvice(stringValue);
                        break;
                    // 添加其他字段的解析
                }
            }
        }

        return weather;
    }
}
