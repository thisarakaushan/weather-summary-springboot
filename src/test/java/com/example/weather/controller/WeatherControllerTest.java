package com.example.weather.controller;

import com.example.weather.dto.WeatherSummaryResponse;
import com.example.weather.service.WeatherService;
import com.example.weather.service.WeatherService.WeatherServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    private WeatherSummaryResponse response;

    @BeforeEach
    void setUp() {
        response = new WeatherSummaryResponse();
        response.setCity("London");
        response.setAverageTemperature(24.3);
        response.setHottestDay("2025-07-18");
        response.setColdestDay("2025-07-18");
    }

    @Test
    void testGetWeatherSummary_Success() {
        // Arrange
        when(weatherService.getWeatherSummary("London"))
                .thenReturn(CompletableFuture.completedFuture(response));

        // Act
        CompletableFuture<ResponseEntity<WeatherSummaryResponse>> future = 
                weatherController.getWeatherSummary("London");
        ResponseEntity<WeatherSummaryResponse> result = future.join();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(weatherService, times(1)).getWeatherSummary("London");
    }

    @Test
    void testGetWeatherSummary_InvalidCity() {
        // Act
        CompletableFuture<ResponseEntity<WeatherSummaryResponse>> future = 
                weatherController.getWeatherSummary("");
        ResponseEntity<WeatherSummaryResponse> result = future.join();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNull(result.getBody());
        verify(weatherService, never()).getWeatherSummary(anyString());
    }

    @Test
    void testGetWeatherSummary_ServiceError() {
        // Arrange
        when(weatherService.getWeatherSummary("London"))
                .thenReturn(CompletableFuture.failedFuture(
                        new WeatherServiceException("API error", null)));

        // Act
        CompletableFuture<ResponseEntity<WeatherSummaryResponse>> future = 
                weatherController.getWeatherSummary("London");
        ResponseEntity<WeatherSummaryResponse> result = future.join();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
        verify(weatherService, times(1)).getWeatherSummary("London");
    }
}