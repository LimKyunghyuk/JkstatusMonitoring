package com.hyundai.monitoring.jkstatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
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
	public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss"; // 2022-08-19 00:47:46
	
	Jkstatus[] jkstatusVO;

	PoolingHttpClientConnectionManager pool;
	CloseableHttpClient httpClient;
	
	JkstatusParser(){
		pool = new PoolingHttpClientConnectionManager();	
	}
	
	private void init(String[] serversName) {

		jkstatusVO = new Jkstatus[serversName.length];

		for (int i = 0; i < serversName.length; i++) {
			jkstatusVO[i] = new Jkstatus();
			jkstatusVO[i].setName(serversName[i]);
		}
		
		pool.setDefaultMaxPerRoute(10);
		pool.setMaxTotal(100);		
		
	}

	public Jkstatus[] getJkstatus(String url, String[] serversName) {

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

	
	
	public String getJsonAsString(String url, String[] exceptionList) {
		
		JSONObject json = new JSONObject();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		String formatedNow = now.format(formatter);
		json.put("occurDatetime", formatedNow);
		json.put("type", "jkstatus");
		json.put("ms", TIME_OUT);
		
		
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
			long start = System.currentTimeMillis();
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			if(200 != httpResponse.getStatusLine().getStatusCode()) {
				return json.toString();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			String[] pair;
			
			String keyword = "";
			Object value = "";
			
			long cnt = 0L; 
			
			while((inputLine = reader.readLine()) != null){
				response.append(inputLine);
				
				pair = inputLine.split("=");
				
				if(pair != null && pair.length != 0) {
					
					if(pair.length == 1) { 					// 값이 없으면
						keyword = pair[0];
						value = "";
					}else {
						
						try {								// 값이 숫자면
							keyword = pair[0];
							value = Long.parseLong(pair[1]);
						}catch(NumberFormatException e) {	// 값이 숫자가 아니라면
							keyword = pair[0];
							value = pair[1];
						}
					}
					
					if(exceptionList == null) {
						json.put(keyword, value);
						logger.debug(cnt + ": put(" + keyword + ", "+ value+")");
						cnt++;
					}else {
						
						Boolean isExistence = false;
						
						for(String exception : exceptionList) {
							
							if(keyword.contains(exception)) {
								isExistence = true;
								break;
							}
						}
						
						if(!isExistence) {
							json.put(keyword, value);
							logger.debug(cnt + ": put(" + keyword + ", "+ value+")");
							cnt++;
						}
					}
				}
			}
			
			reader.close();
			logger.debug(" count : " + cnt);

			long end = System.currentTimeMillis();
			json.put("ms", end - start);
			
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
		
		String [] exceptionList = {"dautowayauth", "dappstoreapp", "dappstore", "dportal", "dserver"};
		
		logger.debug(sample.getJsonAsString("http://127.0.0.1/jkstatus?cmd=list&w=server&mime=prop", exceptionList));
				
		long endTime = System.currentTimeMillis();

		logger.debug("Done : " + (endTime - srartTime) + "ms");
	}

}
