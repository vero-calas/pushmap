package model;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    //Se descomprimen los archivos
    public void decompressZips(){
        String zipFileUbication;
        File newFile;
        FileOutputStream fos;
        File destDir;
        ZipInputStream zis;
        ZipEntry zipEntry;
        FileInputStream fis;
        BufferedOutputStream dest;
        int len;
        System.out.println("estoy por aca");
        //Se descomprimen los archivos zip descargados
        for(int j = 0; j < this.zipFilesNames.length ; j++)
        {

            zipFileUbication = this.carpetaShapefiles + this.zipFilesNames[j];
            System.out.println("ruta archivo: " + zipFileUbication);
            System.out.println("carpetaShapefiles: " + this.carpetaShapefiles);

            try {
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

                        String entryName = entry.getName();
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
                zis.close();
            }
            catch( Exception e ){
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
