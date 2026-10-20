package com.curapaste;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "1m")
public class CurapasteApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurapasteApplication.class, args);
	}

}
