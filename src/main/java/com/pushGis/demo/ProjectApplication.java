package com.pushGis.demo;

import model.DownloadUtilies;
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

			//--------------------------------------------------------------------------
            //Descarga de archivos por WFS
			System.out.println("downloading wfs files");
			String[] urlList = {"http://walker.dgf.uchile.cl/geoserver/chile/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=chile:gore_caletas_pesqueras_ddw84&maxFeatures=50&outputFormat=SHAPE-ZIP",
					"http://walker.dgf.uchile.cl/geoserver/chile/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=chile:gore_faenas_minerasactivas_ddw84&outputFormat=SHAPE-ZIP", 
					"http://walker.dgf.uchile.cl/geoserver/chile/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=chile:mma_reservas_marinas_ddw84&outputFormat=SHAPE-ZIP", 
					"http://walker.dgf.uchile.cl/geoserver/chile/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=chile:gore_riesgo_tsunami_ddw84&outputFormat=SHAPE-ZIP"};
			String[] servicesNames = {"gore_caletas_pesqueras_ddw84.zip", "gore_faenas_minerasactivas_ddw84.zip", "mma_reservas_marinas_ddw84.zip", "gore_riesgo_tsunami_ddw84.zip"};

			DownloadUtilies downloadUtilies = new DownloadUtilies(urlList,servicesNames);
			downloadUtilies.createFolder();
			downloadUtilies.downloadLayers();
			downloadUtilies.decompressZips();

			//--------------------------------------------------------------------------
			//Convertir de csv a shapefile
			System.out.println("Converting csv to shp");
			Csv2Shape.createShape(downloadUtilies.getCarpetaActual(),downloadUtilies.getCarpetaShapefiles(),"voluntarios.csv");

            //--------------------------------------------------------------------------
            //Subida de shapefiles a postgis
			//System.out.println("loading wfs layers");
			Shp2Pgsql shape2db = new Shp2Pgsql();
			String carpetaShapefiles = downloadUtilies.getCarpetaShapefiles();
			for(int j = 0; j < urlList.length ; j++)
			{
				shape2db.loadData(carpetaShapefiles, servicesNames[j].replace(".zip",".shp"));
			}

            //System.out.println("loading data ayni");
			shape2db.loadData(carpetaShapefiles, "voluntarios.shp");


        } catch (Exception e) {
            e.printStackTrace();
        }

		System.out.println(
				"scheduling rdy");
	}



}


