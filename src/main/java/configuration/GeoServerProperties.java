package configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GeoServerProperties {

    static Properties properties = new Properties();

    static {
        try{
            InputStream input = GeoServerProperties.class.getResourceAsStream("/geoserver.properties");
            if(input == null){
                throw new ExceptionInInitializerError("No se pudo encontrar o abrir geoserver properties");
            }
            GeoServerProperties.properties.load(input);
        }catch (IOException io){
            io.printStackTrace();
        }
    }

    public final static String GEOSERVER_URL = properties.getProperty("geoserver.url");
    public final static String GEOSERVER_USER = properties.getProperty("geoserver.username");
    public final static String GEOSERVER_PASS = properties.getProperty("geoserver.password");
    public final static String GEOSERVER_WS = properties.getProperty("geoserver.workspace");
    public final static String GEOSERVER_DS = properties.getProperty("geoserver.datastore");
}