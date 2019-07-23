package model;

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

public class Shp2Pgsql {

    private DataStore dataStore;

    public void loadData(String url){

        try {
            //Apertura del shapefile
            File shp = new File(url);
            DataStore inputDataStore = DataStoreFinder.getDataStore(Collections.singletonMap("url",shp.toURI().toURL()));
            String fileName = inputDataStore.getTypeNames()[0];
            SimpleFeatureType inputType = inputDataStore.getSchema(fileName);
            FeatureSource<SimpleFeatureType, SimpleFeature> source = inputDataStore.getFeatureSource(fileName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> inputFeatureCollection = source.getFeatures();

            //Conexión con la base de datos
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype", "postgis");
            params.put("host", "127.0.0.1");
            params.put("port", 5432);
            params.put("schema", "public");
            params.put("database", "postgres");
            params.put("user", "postgres");
            params.put("passwd", "secret");
            this.dataStore = DataStoreFinder.getDataStore(params);

            //Generación de estructura para almacenar Features en la bd
            FeatureCollection<SimpleFeatureType,SimpleFeature> data = readData(inputFeatureCollection);

            //Generación de la tabla y almacenar en la base de datos
            writeData(data);

            //Publicacion en geoserver(?)
            //Ver si ya está publicado o no

            System.out.println("Publicación en geoserver...en proceso");
            boolean ok = publishLayer("resultados","PushMap","pushmap");
            if(ok)
                System.out.println("PUBLICADO");
            else
                System.out.println("NO PUBLICADO");
            //Finalizar conexión con base de datos
            this.dataStore.dispose();
            inputDataStore.dispose();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean publishLayer(String name, String workspace, String dataStore){

        boolean published = false;

        String restURL = "http://localhost:8080/geoserver/";
        String username = "admin";
        String password = "geoserver";

        try{
            GeoServerRESTReader reader = new GeoServerRESTReader(restURL,username,password);
            GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(restURL,username,password);

            if(reader.existsLayer(workspace,name,true)){
                System.out.println("Ya esta publicado!");
                return published;
            }else{
                System.out.println("NO está publicado");
                System.out.println();
                GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
                fte.setName(name);
                fte.setTitle(name);
                fte.setSRS("EPSG:4326");

                GSLayerEncoder layerEncoder = new GSLayerEncoder();
                layerEncoder.setDefaultStyle("point");

                published = publisher.publishDBLayer(workspace,dataStore,fte,layerEncoder);
                return published;
            }
        }catch (MalformedURLException m){
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
                System.out.println("Ya esta creado pero se creo de nuevo");
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
                    System.out.println("Realiza la escritura en la base de datos");
                }catch(Exception ex){
                    //Si no se logra, se realiza un rollback
                    ex.printStackTrace();
                    transaction.rollback();
                }finally {
                    transaction.close();
                }
            }else{
                System.out.println("Error en la escritura en la base de datos");
                return;
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

