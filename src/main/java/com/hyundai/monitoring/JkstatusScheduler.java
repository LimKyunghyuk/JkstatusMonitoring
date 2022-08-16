package com.hyundai.monitoring;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JkstatusScheduler {

	@Scheduled(fixedDelay=3000)
	public void test() {
		System.out.println("dely 3000");
	}
}
