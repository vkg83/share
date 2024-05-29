package com.vkg.finance.share;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

//@SpringBootTest
class ShareApplicationTests {

	@Test
	void contextLoads() {
		LocalDate today = LocalDate.now();
		for (int i = 0; i < 10; i++) {
			var date = today.minusDays(i);
			LocalDate lastT = date.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
			LocalDate lastF = lastT.minusDays(6);
			System.out.println("F = " + lastF + ", T = "+ lastT);
		}
	}

}
