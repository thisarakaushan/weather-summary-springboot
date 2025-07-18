package com.example.weather.service;

import com.example.weather.dto.WeatherSummaryResponse;
import com.example.weather.util.WeatherDataProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class WeatherServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        // Mock WebClient chain
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Initialize WeatherService with mocked dependencies
        weatherService = new WeatherService(webClientBuilder);
    }

    @Test
    void testGetWeatherSummary_Success() throws ExecutionException, InterruptedException {
        // Arrange
        String city = "London";
        WeatherService.WeatherApiResponse apiResponse = new WeatherService.WeatherApiResponse();
        WeatherService.WeatherItem item = new WeatherService.WeatherItem();
        item.setDt_txt("2024-11-18 12:00:00");
        WeatherService.Main main = new WeatherService.Main();
        main.setTemp(15.0);
        item.setMain(main);
        apiResponse.setList(List.of(item));

        when(responseSpec.bodyToMono(WeatherService.WeatherApiResponse.class))
                .thenReturn(Mono.just(apiResponse));

        // Act
        CompletableFuture<WeatherSummaryResponse> future = weatherService.getWeatherSummary(city);
        WeatherSummaryResponse response = future.get();

        // Assert
        assertNotNull(response);
        assertEquals(city, response.getCity());
        assertEquals(15.0, response.getAverageTemperature());
        assertEquals("2024-11-18", response.getHottestDay());
        assertEquals("2024-11-18", response.getColdestDay());
        verify(responseSpec, times(1)).bodyToMono(WeatherService.WeatherApiResponse.class);
    }

    @Test
    void testGetWeatherSummary_Cached() throws ExecutionException, InterruptedException {
        // Arrange
        String city = "London";
        WeatherService.WeatherApiResponse apiResponse = new WeatherService.WeatherApiResponse();
        WeatherService.WeatherItem item = new WeatherService.WeatherItem();
        item.setDt_txt("2024-11-18 12:00:00");
        WeatherService.Main main = new WeatherService.Main();
        main.setTemp(15.0);
        item.setMain(main);
        apiResponse.setList(List.of(item));

        when(responseSpec.bodyToMono(WeatherService.WeatherApiResponse.class))
                .thenReturn(Mono.just(apiResponse));

        // Act: First call (hits API)
        CompletableFuture<WeatherSummaryResponse> future1 = weatherService.getWeatherSummary(city);
        WeatherSummaryResponse response1 = future1.get();

        // Act: Second call (should hit cache)
        CompletableFuture<WeatherSummaryResponse> future2 = weatherService.getWeatherSummary(city);
        WeatherSummaryResponse response2 = future2.get();

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1, response2);
        verify(responseSpec, times(1)).bodyToMono(WeatherService.WeatherApiResponse.class); // API called only once
    }

    @Test
    void testGetWeatherSummary_A1692b6e5b4c4c2ab00f3f2db6df3562piError() {
        // Arrange
        String city = "InvalidCity";
        when(responseSpec.bodyToMono(WeatherService.WeatherApiResponse.class))
                .thenReturn(Mono.error(new RuntimeException("API error")));

        // Act & Assert
        CompletableFuture<WeatherSummaryResponse> future = weatherService.getWeatherSummary(city);
        ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertTrue(exception.getCause() instanceof WeatherService.WeatherServiceException);
        assertEquals("Failed to fetch weather data for " + city, exception.getCause().getMessage());
        verify(responseSpec, times(1)).bodyToMono(WeatherService.WeatherApiResponse.class);
    }

    @Configuration
    @EnableCaching
    static class TestConfig {
        // Enable caching for tests
    }
}