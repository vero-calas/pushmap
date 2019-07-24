package model;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.logging.Logger;

public class DownloadUtilies {

    private String[] urlList;
    private String[] zipFilesNames;
    private String carpetaActual;
    private String carpetaShapefiles;
    private static final Logger logr = Logger.getLogger("PushMap");

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
            	logr.warning("Error in creation of directory for shapefiles");
        logr.info("Successful directory for shapefiles creation");
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
            	logr.warning("The URL '" + this.urlList[i] + "' is not valid.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Se descomprimen los archivos
    public void decompressZips(){
        String zipFileUbication;
        FileOutputStream fos;
        ZipInputStream zis;
        FileInputStream fis;
        BufferedOutputStream dest;
        //Se descomprimen los archivos zip descargados
        for(int j = 0; j < this.zipFilesNames.length ; j++)
        {

            zipFileUbication = this.carpetaShapefiles + this.zipFilesNames[j];
            try {
            	logr.info("Decompressing zip files");
                // Create a ZipInputStream to read the zip file
                dest = null;
                fis = new FileInputStream( zipFileUbication );
                zis = new ZipInputStream( new BufferedInputStream( fis ) );

                // Loop over all of the entries in the zip file
                int count;
                byte data[] = new byte[ 8192  ];
                ZipEntry entry;

                while( ( entry = zis.getNextEntry() ) != null ){
                    if( !entry.isDirectory() ){
                        String destFN = this.carpetaShapefiles + File.separator + entry.getName();

                        // Write the file to the file system
                        fos = new FileOutputStream( destFN );
                        dest = new BufferedOutputStream( fos, 8192  );

                        while( (count = zis.read( data, 0, 8192  ) ) != -1 ){
                            dest.write( data, 0, count );
                        }

                        dest.flush();
                        dest.close();
                    }
                }
                logr.info("Successful decompress of zip files");
                zis.close();
            }
            catch( Exception e ){
            	logr.info("Error with decompress of zip files");
                e.printStackTrace();
            }
        }
    }

    public String[] getUrlList() {
        return urlList;
    }

    public String[] getZipFilesNames() {
        return zipFilesNames;
    }

    public String getCarpetaActual() {
        return carpetaActual;
    }

    public String getCarpetaShapefiles() {
        return carpetaShapefiles;
    }
}
