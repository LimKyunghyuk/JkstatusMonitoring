package com.hyundai.monitoring.jkstatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
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
		
		pool.setDefaultMaxPerRoute(10);
		pool.setMaxTotal(100);		
		
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

	PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
	CloseableHttpClient httpClient;
	
	public void parserJkstatus(String url) {
				
		HttpGet httpGet = new HttpGet(url);
		
		if(httpClient == null) {
			logger.debug(">>> httpClient init");
			httpClient = HttpClients.custom().setConnectionManager(pool).build();	
		}
			

		try {
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			System.out.println(httpResponse.getStatusLine().getStatusCode());
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String inputLine;
			StringBuffer response = new StringBuffer();
			while((inputLine = reader.readLine()) != null){
				response.append(inputLine);
				System.out.println(inputLine);
			}
			reader.close();
			System.out.println(response.toString());
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public static void main(String[] args) throws Exception {

		org.apache.logging.log4j.core.config.Configurator.setLevel(JkstatusParser.class,
				org.apache.logging.log4j.Level.DEBUG);

		long srartTime = System.currentTimeMillis();
		JkstatusParser a = new JkstatusParser();
		String[] serverName = { "server1", "server2", "server3", "server4", "server5", "server6" };
//		JkstatusVo[] JkstatusList = a.parserJkstatus("http://10.14.81.161/jkstatus?cmd=list&w=server&mime=prop",
//				serverName);
//
//		for (JkstatusVo Jkstatus : JkstatusList) {
//			logger.debug(Jkstatus);
//		}

		for(int i = 0 ;i<3; i++) 
		a.parserJkstatus("http://10.14.81.161/jkstatus?cmd=list&w=server&mime=prop");

				
		long endTime = System.currentTimeMillis();

		logger.debug("Done : " + (endTime - srartTime) + "ms");
	}

}
