package com.hyundai.monitoring.jkstatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	
	
	public String getJsonAsString(String url, List<String> exceptionList) {

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
	
	public List<String> parse(String url, List<String> exceptionList) {
		
		List<Pair> pairList = parseJkstatus(url);
		
		// 예외 키워드 제거
		for(int i = pairList.size() - 1 ; 0 <= i  ; i--) {
			for(String exceptionKeyword : exceptionList) {
				if(pairList.get(i).getKey().contains(exceptionKeyword)) {
					pairList.remove(i);	
				}
			}
		}
		
		return convertString(pairList);
	}
	
	public List<Pair> parseJkstatus(String url) {

		List<Pair> resultPairList = new ArrayList<Pair>();
		
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
				return resultPairList;
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String inputLine;
			
			while((inputLine = reader.readLine()) != null){
				resultPairList.add(parseLine(inputLine));	
			}
			
			reader.close();
			
		} catch (Exception e) {
			logger.error(e.toString());
		}
		
		return resultPairList;
		
	}
	
	public Pair parseLine(String inputLine){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String[] pair = inputLine.split("=");
		
		String key = "";
		Object value = "";
		
		if(pair != null && pair.length != 0) {
			
			if(pair.length == 1) { 					// 값이 없으면
				key = pair[0];
				value = "";
			}else {
				
				try {								// 값이 숫자면
					key = pair[0];
					value = Long.parseLong(pair[1]);
				}catch(NumberFormatException e) {	// 값이 숫자가 아니라면
					key = pair[0];
					value = pair[1];
				}
			}
			
			resultMap.put(key, value);
		}
		
		return new Pair(key, value);
	}
	
	public List<String> convertString(List<Pair> pairList) {
		
		Map<String, JSONObject> jsonMap = new HashMap<String, JSONObject>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		String formatedNow = now.format(formatter);
		
		for(int i = 0 ; i < pairList.size() ; i++ ) {

			Pair pair = pairList.get(i);
			String[] tokenAry = pair.getKey().split("[.]", 3);
		
			// worker.<rowName>.key 형태의 데이터만 변환함
			if(tokenAry.length == 3) {
				logger.debug(pair.getKey() + " ====> tokenAry.length:" + tokenAry.length + ", [0]" + tokenAry[0] + ", [1]" + tokenAry[1] + "[2]" + tokenAry[2]);
				
				String key = tokenAry[1];
				String value = tokenAry[2].replace(".","_");
				
				JSONObject json = new JSONObject();
				json.put("rowName", key);
				json.put("occurDatetime", formatedNow);
				json.put("type", "jkstatus");
				
				if(!jsonMap.containsKey(key)) {
					jsonMap.put(key, json);
				}else {
					jsonMap.get(key).put(value, pair.getValue());
				}
			}
		}
		
		return toJsonAsString(jsonMap);
	}

	List<String> toJsonAsString(Map<String, JSONObject> jsonMap){
		
		List<String> rtnList = new ArrayList<String>();
		
		for(String logKey : jsonMap.keySet()) {
			rtnList.add(jsonMap.get(logKey).toString());
		}
		
		return rtnList;
	}
}
