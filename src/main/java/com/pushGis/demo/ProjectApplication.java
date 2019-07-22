package com.pushGis.demo;

import model.Shp2Pgsql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
			
			String[] urlList = {"http://walker.dgf.uchile.cl/geoserver/chile/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=chile:gore_caletas_pesqueras_ddw84&maxFeatures=50&outputFormat=SHAPE-ZIP", 
					"http://walker.dgf.uchile.cl/geoserver/chile/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=chile:gore_faenas_minerasactivas_ddw84&outputFormat=SHAPE-ZIP", 
					"http://walker.dgf.uchile.cl/geoserver/chile/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=chile:mma_reservas_marinas_ddw84&outputFormat=SHAPE-ZIP", 
					"http://walker.dgf.uchile.cl/geoserver/chile/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=chile:gore_riesgo_tsunami_ddw84&outputFormat=SHAPE-ZIP"};
			String[] servicesNames = {"gore_caletas_pesqueras_ddw84.zip", "gore_faenas_minerasactivas_ddw84.zip", "mma_reservas_marinas_ddw84.zip", "gore_riesgo_tsunami_ddw84.zip"};
			String carpetaActual = System.getProperty("user.dir");
			String carpetaShapefiles = carpetaActual.replace("\\","/") + "/shapefiles/";
			
			
			
			carpetaShapefiles = carpetaShapefiles.substring(1, carpetaShapefiles.length());
			carpetaShapefiles = carpetaShapefiles.substring(1, carpetaShapefiles.length());
			
			//Creacion de carpeta shapefiles en caso de que no exista
			File dir = new File(carpetaShapefiles);
			if (!dir.exists())
				  if (!dir.mkdir())
					  System.out.println("Error al crear la carpeta shapefiles **"); // no se pudo crear la carpeta de destino
			System.out.println("Se crea la carpeta con éxito");
			
			//Se descargan las capas con servicios WFS
			File file;
			URLConnection conn;
			InputStream in;
			OutputStream out;
			int i, j;
			for(i = 0; i < urlList.length ; i++)
			{	
				try {
					file = new File(carpetaShapefiles + servicesNames[i]);
					conn = new URL(urlList[i]).openConnection();
					conn.connect();
					in = conn.getInputStream();
					out = new FileOutputStream(file);
					
					int b = 0;
					while (b != -1) {
					  b = in.read();
					  if (b != -1)
					    out.write(b);
					}
					out.close();
					in.close();
				} catch (MalformedURLException e) {
					  System.out.println("la url: " + urlList[i] + " no es valida!");
					} catch (IOException e) {
					  e.printStackTrace();
					}
			}
			
			
			
			/*
			String urlFinal;
			for(int j = 0; i < urlList.length ; i++)
			{
				urlFinal = carpetaShapefiles + urlList[i];
				System.out.println(urlFinal);
				//shape2db.loadData(urlFinal);
			}
			//shape2db.loadData();
			 */
        } catch (Exception e) {
            e.printStackTrace();
        }

		System.out.println(
				"scheduling rdy");
	}



}


