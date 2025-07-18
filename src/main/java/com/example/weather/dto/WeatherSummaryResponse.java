package com.example.weather.dto;

import lombok.Data;

/**
 * WeatherSummaryResponse DTO to structure the JSON response
 * 
 * @author TK
 *
 */

@Data
public class WeatherSummaryResponse {
    private String city;
    private double averageTemperature;
    private String hottestDay;
    private String coldestDay;
    
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public double getAverageTemperature() {
		return averageTemperature;
	}
	public void setAverageTemperature(double averageTemperature) {
		this.averageTemperature = averageTemperature;
	}
	public String getHottestDay() {
		return hottestDay;
	}
	public void setHottestDay(String hottestDay) {
		this.hottestDay = hottestDay;
	}
	public String getColdestDay() {
		return coldestDay;
	}
	public void setColdestDay(String coldestDay) {
		this.coldestDay = coldestDay;
	}
    
    
}