package com.hyundai.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hyundai.monitoring.jkstatus.JkstatusLogger;



@Component
public class JkstatusScheduler {

	@Autowired
	JkstatusLogger jkstatusLogger; 

	@Scheduled(fixedDelay=60000)
	public void log() {
		jkstatusLogger.start();
	}
	

}
