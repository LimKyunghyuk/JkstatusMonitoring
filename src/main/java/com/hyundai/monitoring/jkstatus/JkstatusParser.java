package com.hyundai.monitoring.jkstatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
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

import net.minidev.json.JSONObject;

public class JkstatusParser {

	private static Logger logger = LogManager.getLogger(JkstatusParser.class);
	public final static int TIME_OUT = 5000;

	JkstatusVo[] jkstatusVO;

	PoolingHttpClientConnectionManager pool;
	CloseableHttpClient httpClient;
	
	JkstatusParser(){
		pool = new PoolingHttpClientConnectionManager();	
	}
	
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

	
	
	public String parserJkstatus(String url) {
		
		JSONObject json = new JSONObject();
		
		HttpGet httpGet = new HttpGet(url);
		
		RequestConfig requestConfig = RequestConfig.custom()
	                .setSocketTimeout(TIME_OUT)
	                .setConnectTimeout(TIME_OUT)
	                .setConnectionRequestTimeout(TIME_OUT)
	                .build();
		httpGet.setConfig(requestConfig);
	        
		if(httpClient == null) {
			logger.debug("httpClient init pool");
			httpClient = HttpClients.custom().setConnectionManager(pool).build();	
		}
			
		try {
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			if(200 != httpResponse.getStatusLine().getStatusCode()) {
				return json.toString();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String inputLine;
			StringBuffer response = new StringBuffer();
			while((inputLine = reader.readLine()) != null){
				response.append(inputLine);
				
				String[] pair = inputLine.split("=");
				
				if(pair != null && pair.length != 0) {
					
					if(pair.length == 1) {
						json.put(pair[0], "");
					}else {
						
						try {
							Long number = Long.parseLong(pair[1]); 
							json.put(pair[0], number);		
						}catch(NumberFormatException e) {
							json.put(pair[0], pair[1]);
						}
					}
				}
			}
			reader.close();

		} catch (Exception e) {
			logger.error(e.toString());
		}
		
		return json.toString();
		
	}
	
	public static void main(String[] args) throws Exception {

		org.apache.logging.log4j.core.config.Configurator.setLevel(JkstatusParser.class,
				org.apache.logging.log4j.Level.DEBUG);

		long srartTime = System.currentTimeMillis();
		JkstatusParser sample = new JkstatusParser();
		logger.debug(sample.parserJkstatus("http://127.0.0.1/jkstatus?cmd=list&w=server&mime=prop"));
				
		long endTime = System.currentTimeMillis();

		logger.debug("Done : " + (endTime - srartTime) + "ms");
	}

}
