package com.hyundai.monitoring.jkstatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JkstatusLogger {

	private Logger logger = LogManager.getLogger("JkstatusLoggerMobileAutoway");
	JkstatusParser jkstatusParser;
	
	public JkstatusLogger() {
		jkstatusParser = new JkstatusParser();
	}
	public void start() {
		
		logger.debug("hello!");
		jkstatusParser.parserJkstatus("http://10.14.81.161/jkstatus?cmd=list&w=server&mime=prop");
		
	}
}
