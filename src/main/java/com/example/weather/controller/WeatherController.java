package com.example.weather.controller;

import com.example.weather.dto.WeatherSummaryResponse;
import com.example.weather.service.WeatherService;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;

/**
 * WeatherController to expose the /weather endpoint, 
 * handling the city query parameter and returning the JSON response.
 * 
 * @author TK
 *
 */
@RestController
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

//    @GetMapping("/weather")
//    public Mono<ResponseEntity<WeatherSummaryResponse>> getWeatherSummary(@RequestParam String city) {
//        if (city == null || city.trim().isEmpty()) {
//            return Mono.just(ResponseEntity.badRequest().build());
//        }
//        return weatherService.getWeatherSummary(city)
//                .map(ResponseEntity::ok)
//                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()));
//    }
    
    @GetMapping("/weather")
    public CompletableFuture<ResponseEntity<WeatherSummaryResponse>> getWeatherSummary(@RequestParam String city) {
        if (city == null || city.trim().isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
        }
        return weatherService.getWeatherSummary(city)
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(500).build());
    }
}