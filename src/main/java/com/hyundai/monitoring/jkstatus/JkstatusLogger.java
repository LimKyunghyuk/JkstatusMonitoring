package com.hyundai.monitoring.jkstatus;

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

	private static Logger logger = LogManager.getLogger("JkstatusLoggerMobileAutoway");
	JkstatusParser jkstatusParser;
	final static String XML_NAME = "jkstatus.xml";
	private XMLConfiguration config;

	public JkstatusLogger() {
		jkstatusParser = new JkstatusParser();
	}
	public void start() {
		
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<XMLConfiguration>(
				XMLConfiguration.class).configure(params.xml().setFileName(XML_NAME));

		try {
			config = builder.getConfiguration();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<HierarchicalConfiguration<ImmutableNode>> wasList = config.configurationsAt("exception.pair");
		for(HierarchicalConfiguration<ImmutableNode> service : wasList) {
			
			System.out.println(">"+service.getString("keyword"));
		}
		
		
		String url = "http://127.0.0.1/jkstatus?cmd=list&w=server&mime=prop";
		System.out.println(jkstatusParser.getJsonAsString(url, null));
		System.out.println("================");
	}
	
	public static void main(String[] args) {
		
		System.out.println("!");
		org.apache.logging.log4j.core.config.Configurator.setLevel(JkstatusLogger.class,
				org.apache.logging.log4j.Level.DEBUG);

		
		JkstatusLogger j = new JkstatusLogger();
		j.start();
		System.out.print("-end-");
	}
}
