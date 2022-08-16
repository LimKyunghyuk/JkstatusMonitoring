package com.hyundai.monitoring.jkstatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class JkstatusLogger {

	private Logger logger = LogManager.getLogger("JkstatusLoggerMobileAutoway");
	
	public void start() {
		
		logger.debug("hello!");
		
	}
}
