package com.example.weather.service;

import com.example.weather.dto.WeatherSummaryResponse;
import com.example.weather.util.WeatherDataProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * WeatherService to fetch data from OpenWeatherMap asynchronously and cache the
 * results. This service will use WebClient for HTTP calls and @Async for
 * non-blocking execution.
 * 
 * Caches the response in weatherCache with the city as the key. Maps the API
 * response to WeatherData objects and processes them using
 * WeatherDataProcessor.
 * 
 * @author TK
 *
 */
@Service
public class WeatherService {

	private final WebClient webClient;

	@Value("${openweathermap.api.key}")
	private String apiKey;

	@Value("${openweathermap.api.url}")
	private String apiUrl;

	public WeatherService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://api.openweathermap.org").build();
	}

//    @Cacheable(value = "weatherCache", key = "#city")
//    public Mono<WeatherSummaryResponse> getWeatherSummary(String city) {
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/data/2.5/forecast")
//                        .queryParam("q", city)
//                        .queryParam("appid", apiKey)
//                        .queryParam("units", "metric")
//                        .build())
//                .retrieve()
//                .bodyToMono(WeatherApiResponse.class)
//                .map(response -> {
//                    List<WeatherDataProcessor.WeatherData> weatherData = response.getList().stream()
//                            .map(item -> new WeatherDataProcessor.WeatherData(
//                                    item.getDt_txt(),
//                                    item.getMain().getTemp()))
//                            .collect(Collectors.toList());
//                    return WeatherDataProcessor.processWeatherData(city, weatherData);
//                })
//                .onErrorResume(e -> Mono.error(new WeatherServiceException("Failed to fetch weather data for " + city, e)));
//    }

	@Async
	@Cacheable(value = "weatherCache", key = "#city")
	public CompletableFuture<WeatherSummaryResponse> getWeatherSummary(String city) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/data/2.5/forecast").queryParam("q", city)
						.queryParam("appid", apiKey).queryParam("units", "metric").build())
				.retrieve().bodyToMono(WeatherApiResponse.class).map(response -> {
					List<WeatherDataProcessor.WeatherData> weatherData = response.getList().stream().map(
							item -> new WeatherDataProcessor.WeatherData(item.getDt_txt(), item.getMain().getTemp()))
							.collect(Collectors.toList());
					return WeatherDataProcessor.processWeatherData(city, weatherData);
				}).toFuture().exceptionally(e -> {
					throw new WeatherServiceException("Failed to fetch weather data for " + city, e);
				});
	}

	// Inner classes to map OpenWeatherMap API response
	public static class WeatherApiResponse {
		private List<WeatherItem> list;

		public List<WeatherItem> getList() {
			return list;
		}

		public void setList(List<WeatherItem> list) {
			this.list = list;
		}
	}

	public static class WeatherItem {
		private String dt_txt;
		private Main main;

		public String getDt_txt() {
			return dt_txt;
		}

		public void setDt_txt(String dt_txt) {
			this.dt_txt = dt_txt;
		}

		public Main getMain() {
			return main;
		}

		public void setMain(Main main) {
			this.main = main;
		}
	}

	public static class Main {
		private double temp;

		public double getTemp() {
			return temp;
		}

		public void setTemp(double temp) {
			this.temp = temp;
		}
	}

	public static class WeatherServiceException extends RuntimeException {
		public WeatherServiceException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}