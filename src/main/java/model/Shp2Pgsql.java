package model;

import configuration.GeoServerProperties;
import configuration.SQLProperties;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.identity.FeatureId;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Logger;

public class Shp2Pgsql {

    private DataStore dataStore;
    private static final Logger logr = Logger.getLogger("PushMap");

    public void loadData(String carpetaShapefile, String nameFile){
        try {
            //Apertura del shapefile
            File shp = new File(carpetaShapefile + nameFile);
            DataStore inputDataStore = DataStoreFinder.getDataStore(Collections.singletonMap("url",shp.toURI().toURL()));
            String fileName = inputDataStore.getTypeNames()[0];
            SimpleFeatureType inputType = inputDataStore.getSchema(fileName);
            FeatureSource<SimpleFeatureType, SimpleFeature> source = inputDataStore.getFeatureSource(fileName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> inputFeatureCollection = source.getFeatures();

            //Conexión con la base de datos
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype",SQLProperties.DB_TYPE);
            params.put("host", SQLProperties.DB_HOST);
            params.put("port", SQLProperties.DB_PORT);
            params.put("schema", SQLProperties.DB_SCHEMA);
            params.put("database", SQLProperties.DB_NAME);
            params.put("user", SQLProperties.DB_USER);
            params.put("passwd", SQLProperties.DB_PASS);
            this.dataStore = DataStoreFinder.getDataStore(params);

            //Generación de estructura para almacenar Features en la bd
            FeatureCollection<SimpleFeatureType,SimpleFeature> data = readData(inputFeatureCollection);

            //Generación de la tabla y almacenar en la base de datos
            
            //Carga datos en base de datos
            writeData(data);
            
            String datoStr = data.getSchema().getGeometryDescriptor().getType().toString();
            datoStr = datoStr.split(" ")[1];
            datoStr = datoStr.split("<")[0];
            datoStr = datoStr.toLowerCase();

            //Publicacion en geoserver(?)
            logr.info("Shapefile publication in process...");
            boolean ok = publishLayer(nameFile.replace(".shp", ""),GeoServerProperties.GEOSERVER_WS,GeoServerProperties.GEOSERVER_DS, datoStr);
            if(ok) {
            	logr.info("Pulish success!");
            }
            else {
            	logr.info("The layer is already published");
            }
            	

            //Finalizar conexión con base de datos
            this.dataStore.dispose();
            inputDataStore.dispose();

        } catch (IOException e) {
        	logr.warning("Error with load data of: " + nameFile + " file");
            e.printStackTrace();
        }
    }

    public boolean publishLayer(String name, String workspace, String dataStore, String geomType){
    	logr.info("The layer: " + name + ", is procesing for publish");
        boolean published = false;

        String restURL = GeoServerProperties.GEOSERVER_URL;
        String username = GeoServerProperties.GEOSERVER_USER;
        String password = GeoServerProperties.GEOSERVER_PASS;

        try{
            GeoServerRESTReader reader = new GeoServerRESTReader(restURL,username,password);
            GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(restURL,username,password);

            if(reader.existsLayer(workspace,name,true)){
                return published;
            }else{
            	logr.info("The layer is not published");
                GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
                fte.setName(name);
                fte.setTitle(name);
                fte.setSRS("EPSG:4326");

                GSLayerEncoder layerEncoder = new GSLayerEncoder();
                
                if (geomType.equals("point")) {
                	layerEncoder.setDefaultStyle("point");
                } else if (geomType.equals("linestring")) {
                    layerEncoder.setDefaultStyle("line");
                } else if (geomType.equals("multipolygon")) {
                    layerEncoder.setDefaultStyle("polygon");
                } else if (geomType.equals("multilinestring")) {
                    layerEncoder.setDefaultStyle("line");
                } else if (geomType.equals("multipoint")) {
                    layerEncoder.setDefaultStyle("point");
                }

                logr.info("Publishing...");
                published = publisher.publishDBLayer(workspace,dataStore,fte,layerEncoder);
                return published;
            }
        }catch (MalformedURLException m){
        	logr.warning("*Error with publish process*");
            m.printStackTrace();
            return published;
        }
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> readData(FeatureCollection<SimpleFeatureType, SimpleFeature> inputFeatureCollection){
        ArrayList<SimpleFeature> list = new ArrayList<>();
        SimpleFeature temp;
        SimpleFeatureIterator itr = (SimpleFeatureIterator) inputFeatureCollection.features();
        try{
            while(itr.hasNext()){
                temp = itr.next();
                list.add(temp);
            }
        }finally {
            itr.close();
        }

        //Estructura final
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = DataUtilities.collection(list);
        return collection;
    }

    public void writeData(FeatureCollection<SimpleFeatureType, SimpleFeature> features){
    	logr.info("Writing data in database initialized.");
        if(this.dataStore == null){
            throw new IllegalStateException("DataStore está vacía.");
        }

        //Obtención del schema
        SimpleFeatureType schema = features.getSchema();

        try{
            Transaction transaction = new DefaultTransaction("create");

            //Se obtienen todas las tablas de la bd para comparar y ver si existe la tabla
            String[] typeNames = dataStore.getTypeNames();
            String schemaName = schema.getName().getLocalPart();
            boolean created = false;
            for(String name : typeNames){
                if(schemaName.equalsIgnoreCase(name))
                    created = true;
            }
            if(!created){
                //Se crea el schema, en caso de que no exista en la bd
                dataStore.createSchema(schema);
            }
            else {
                dataStore.removeSchema(schema.getTypeName());
                dataStore.createSchema(schema);
                logr.info("The data is already created, but will be created again.");
            }

            //Proceso de escritura de features
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(schemaName);
            if(featureSource instanceof SimpleFeatureSource){
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

                featureStore.setTransaction(transaction);
                try{
                    //Se agregan todas las features a la base de datos
                    List<FeatureId> listaIDs = featureStore.addFeatures(features);
                    transaction.commit();
                    logr.info("The data is being writing in database.");
                }catch(Exception ex){
                    //Si no se logra, se realiza un rollback
                    ex.printStackTrace();
                    transaction.rollback();
                }finally {
                    transaction.close();
                }
            }else{
            	logr.warning("Error with the write of data in database.");
                return;
            }

        }catch (IOException e){
        	logr.warning("Error with the write of data in database.");
            e.printStackTrace();
        }
    }
}

