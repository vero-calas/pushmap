package com.pushGis.demo;

import model.Shp2Pgsql;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import model.Csv2Shape;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@SpringBootApplication
@EnableScheduling
public class ProjectApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ProjectApplication.class, args);
	}

	@Scheduled(fixedDelay = 1800000)
	public void scheduleFixedDelayTask() {

        try {
            //Csv2Shape.createShape();
			Shp2Pgsql shape2db = new Shp2Pgsql();
			shape2db.loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }

		System.out.println(
				"scheduling rdy");
	}



}


