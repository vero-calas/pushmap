package model;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadUtilies {

    private String[] urlList;
    private String[] zipFilesNames;
    private String carpetaActual;
    private String carpetaShapefiles;

    public DownloadUtilies(String[] urlList, String[] zipFilesNames) {
        this.urlList = urlList;
        this.zipFilesNames = zipFilesNames;
        this.carpetaActual = System.getProperty("user.dir");
        this.carpetaShapefiles = this.carpetaActual.replace("\\","/") + "/shapefiles/";
    }

    //Creacion de carpeta shapefiles en caso de que no exista
    public void createFolder(){
        File dir = new File(this.carpetaShapefiles);
        if (!dir.exists())
            if (!dir.mkdir())
                System.out.println("Error al crear la carpeta shapefiles **"); // no se pudo crear la carpeta de destino
        System.out.println("Se crea la carpeta con éxito");
    }

    //Se descargan las capas con servicios WFS
    public void downloadLayers(){
        File file;
        URLConnection conn;
        InputStream in;
        OutputStream out;
        int i, j;
        for(i = 0; i < this.urlList.length ; i++)
        {
            try {
                file = new File(this.carpetaShapefiles + this.zipFilesNames[i]);
                conn = new URL(this.urlList[i]).openConnection();
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
                System.out.println("la url: " + this.urlList[i] + " no es valida!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
