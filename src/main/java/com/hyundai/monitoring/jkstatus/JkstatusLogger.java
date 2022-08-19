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
		
		String url = "http://127.0.0.1/jkstatus?cmd=list&w=server&mime=prop";
		logger.trace(jkstatusParser.parserJkstatus(url));
		
	}
}
