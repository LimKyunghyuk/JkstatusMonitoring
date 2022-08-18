package com.hyundai.monitoring.jkstatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JkstatusParser {

	private static Logger logger = LogManager.getLogger(JkstatusParser.class);
	public final static int TIME_OUT = 5000;

	JkstatusVo[] jkstatusVO;

	private void init(String[] serversName) {

		jkstatusVO = new JkstatusVo[serversName.length];

		for (int i = 0; i < serversName.length; i++) {
			jkstatusVO[i] = new JkstatusVo();
			jkstatusVO[i].setName(serversName[i]);
		}
	}

	public JkstatusVo[] parserJkstatus(String url, String[] serversName) {

		init(serversName);

		try {
			Document doc = Jsoup.connect(url).timeout(TIME_OUT).get();
			String[] test = doc.select("body").text().split(" ");

			for (int i = 0; i < jkstatusVO.length; i++) {
				for (int j = 0; j < test.length; j++) {
					if (test[j].contains("worker." + jkstatusVO[i].getName() + ".activation")) {
						jkstatusVO[i].setAct(test[j].split("=")[1]);
					}
					if (test[j].contains("worker." + jkstatusVO[i].getName() + ".state")) {
						jkstatusVO[i].setState(test[j].split("=")[1]);
					}
					if (test[j].contains("worker." + jkstatusVO[i].getName() + ".busy")) {
						jkstatusVO[i].setBusy(Long.parseLong(test[j].split("=")[1]));
					}
					if (test[j].contains("worker." + jkstatusVO[i].getName() + ".max_busy")) {
						jkstatusVO[i].setMaxBusy(Long.parseLong(test[j].split("=")[1]));
					}
					if (test[j].contains("worker." + jkstatusVO[i].getName() + ".connected")) {
						jkstatusVO[i].setCon(Long.parseLong(test[j].split("=")[1]));
					}
					if (test[j].contains("worker." + jkstatusVO[i].getName() + ".max_connected")) {
						jkstatusVO[i].setMaxCon(Long.parseLong(test[j].split("=")[1]));
					}
				}
			}

		} catch (Exception e) {
			logger.error(e.toString());
		}

		return jkstatusVO;
	}

	public static void main(String[] args) throws Exception {

		org.apache.logging.log4j.core.config.Configurator.setLevel(JkstatusParser.class,
				org.apache.logging.log4j.Level.DEBUG);

		long srartTime = System.currentTimeMillis();
		JkstatusParser a = new JkstatusParser();
		String[] serverName = { "server1", "server2", "server3", "server4", "server5", "server6" };
		JkstatusVo[] JkstatusList = a.parserJkstatus("http://10.14.81.161/jkstatus?cmd=list&w=server&mime=prop",
				serverName);
		
		for (JkstatusVo Jkstatus : JkstatusList) {
			logger.debug(Jkstatus);
		}

		long endTime = System.currentTimeMillis();

		logger.debug("Done : " + (endTime - srartTime) + "ms");
	}

}
