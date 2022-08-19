package com.hyundai.monitoring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hyundai.monitoring.jkstatus.JkstatusLogger;

@Configuration
public class ApplicationConfig {

	@Bean
	public JkstatusLogger jkstatusLogger() {
		return new JkstatusLogger();
	}
}
