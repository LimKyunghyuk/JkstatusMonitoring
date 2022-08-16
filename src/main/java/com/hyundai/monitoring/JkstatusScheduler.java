package com.hyundai.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hyundai.monitoring.jkstatus.JkstatusLogger;



@Component
public class JkstatusScheduler {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	JkstatusLogger jkstatusLogger; 
	
	@Scheduled(fixedDelay=3000)
	public void log() {
		jkstatusLogger.start();
	}
}
