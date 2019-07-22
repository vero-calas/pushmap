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


			String[] urlList = {"gore_caletas_pesqueras_ddw84.shp", "gore_faenas_minerasactivas_ddw84.shp", "gore_riesgo_tsunami_ddw84.shp", "mma_reservas_marinas_ddw84.shp"};
			String carpetaActual = System.getProperty("user.dir");
			String carpetaShapefiles = carpetaActual.replace("\\","/") + "shapefiles/";
			carpetaShapefiles = carpetaShapefiles.substring(1, carpetaShapefiles.length());
			carpetaShapefiles = carpetaShapefiles.substring(1, carpetaShapefiles.length());
			String urlFinal;
			for(int i = 0; i < urlList.length ; i++)
			{
				urlFinal = carpetaShapefiles + urlList[i];
				System.out.println(urlFinal);
				//shape2db.loadData(urlFinal);
			}
			//shape2db.loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }

		System.out.println(
				"scheduling rdy");
	}



}


