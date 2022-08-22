package com.hyundai.monitoring.jkstatus;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JkstatusLogger {

	private static Logger elkLogger = LogManager.getLogger("elkLogger");
	private static Logger logger = LogManager.getLogger(JkstatusLogger.class);
	
	final static String XML_NAME = "jkstatus.xml";
	JkstatusParser jkstatusParser;
	
	private XMLConfiguration config;

	public JkstatusLogger() {
		
		initConfiguration();
		initJkstatusParser();
	}
	
	private void initConfiguration() {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
				XMLConfiguration.class).configure(params.xml().setFileName(XML_NAME));

		try {
			config = builder.getConfiguration();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private void initJkstatusParser() {
		jkstatusParser = new JkstatusParser();
	}
	
	public void start() {
		
		String url = config.getString("jkstatus-url");
		logger.debug("jkstatus-url>" + url);

		// 예외 키워드 지정
		List<String> exceptionList = new ArrayList<String>();  
		List<HierarchicalConfiguration<ImmutableNode>> exceptionListInConfig = config.configurationsAt("exception.pair");
		for(HierarchicalConfiguration<ImmutableNode>  exceptionInConfig: exceptionListInConfig) {
			exceptionList.add(exceptionInConfig.getString("keyword"));
		}
		
		List<String> logList = jkstatusParser.parse(url, exceptionList);
		
		if(logList.size() == 0) {
			logger.debug("There are no logs.");
			return;
		}
			
		for(int i = 0 ; i < logList.size() ; i++) {
			elkLogger.trace(logList.get(i));
			logger.debug("[" + i + "]" + logList.get(i));
		}
		
		
	}
	
	public static void main(String[] args) {
		
		org.apache.logging.log4j.core.config.Configurator.setLevel(JkstatusLogger.class,
				org.apache.logging.log4j.Level.DEBUG);
		org.apache.logging.log4j.core.config.Configurator.setLevel(JkstatusParser.class,
				org.apache.logging.log4j.Level.DEBUG);

		long srartTime = System.currentTimeMillis();
		
		JkstatusLogger jkstatusLogger = new JkstatusLogger();
		jkstatusLogger.start();
		
		long endTime = System.currentTimeMillis();
		logger.debug("Done : " + (endTime - srartTime) + "ms");
		
	}
}
