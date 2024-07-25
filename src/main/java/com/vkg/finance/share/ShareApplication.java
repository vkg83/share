package com.vkg.finance.share;

import com.vkg.finance.share.stock.service.SimpleInvestmentSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShareApplication implements CommandLineRunner {
	@Autowired
	private SimpleInvestmentSimulator investmentSimulator;

	public static void main(String[] args) {
		SpringApplication.run(ShareApplication.class, args);
	}

	@Override
	public void run(String... args) {
		investmentSimulator.simulate();
	}
}
