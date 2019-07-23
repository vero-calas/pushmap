package configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SQLProperties {
    static Properties properties = new Properties();

    static {
        try{
            InputStream input = SQLProperties.class.getResourceAsStream("/sql.properties");
            if(input == null){
                throw new ExceptionInInitializerError("No se pudo encontrar o abrir sql properties");
            }
            SQLProperties.properties.load(input);
        }catch (IOException io){
            io.printStackTrace();
        }
    }

    public final static String DB_TYPE = properties.getProperty("DB.type");
    public final static String DB_HOST = properties.getProperty("DB.host");
    public final static String DB_PORT = properties.getProperty("DB.port");
    public final static String DB_SCHEMA = properties.getProperty("DB.schema");
    public final static String DB_NAME = properties.getProperty("DB.name");
    public final static String DB_USER = properties.getProperty("DB.username");
    public final static String DB_PASS = properties.getProperty("DB.password");
}
