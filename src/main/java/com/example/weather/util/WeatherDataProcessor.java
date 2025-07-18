package com.example.weather.util;

import com.example.weather.dto.WeatherSummaryResponse;
import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WeatherDataProcessor processes a list of WeatherData objects from API.
 * Filters data for the last 7 days, groups by date, and computes daily average temperatures.
 * Calculates the overall average temperature and identifies the hottest and coldest days.
 * 
 * @author TK
 *
 */
public class WeatherDataProcessor {

    public static WeatherSummaryResponse processWeatherData(String city, List<WeatherData> weatherData) {
        // Filter data for the last 7 days
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        // Group by date and calculate daily average temperature
        Map<LocalDate, Double> dailyAvgTemps = weatherData.stream()
                .filter(data -> {
                    LocalDate date = LocalDate.parse(data.getDate().substring(0, 10));
                    return !date.isBefore(sevenDaysAgo) && !date.isAfter(today);
                })
                .collect(Collectors.groupingBy(
                        data -> LocalDate.parse(data.getDate().substring(0, 10)),
                        Collectors.averagingDouble(WeatherData::getTemperature)
                ));

        // Compute overall average temperature
        double averageTemperature = dailyAvgTemps.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Find hottest and coldest days
        Map.Entry<LocalDate, Double> hottest = dailyAvgTemps.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .orElse(null);
        Map.Entry<LocalDate, Double> coldest = dailyAvgTemps.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .orElse(null);

        WeatherSummaryResponse response = new WeatherSummaryResponse();
        response.setCity(city);
        response.setAverageTemperature(Math.round(averageTemperature * 10.0) / 10.0);
        response.setHottestDay(hottest != null ? hottest.getKey().toString() : null);
        response.setColdestDay(coldest != null ? coldest.getKey().toString() : null);

        return response;
    }

    // Inner class to represent weather data from API
    public static class WeatherData {
        private String date;
        private double temperature;

        public WeatherData(String date, double temperature) {
            this.date = date;
            this.temperature = temperature;
        }

        public String getDate() {
            return date;
        }

        public double getTemperature() {
            return temperature;
        }
    }
}