package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.scheduling.annotation.Scheduled;


public class Csv2Shape {

    public static void createShape() throws Exception {
        //Apertura del fichero
        File file = new File("/home/shalini/Escritorio/locations.csv");

        //Se crea un listado de features a partir de la lectura del archivo
        List<SimpleFeature> features = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        SimpleFeatureType TYPE = null;
        try {
            String line = reader.readLine();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("location:Point,");

            String[] headers = line.split("\\,");
            for (String header : headers) {
                stringBuilder.append("").append(header).append(":String,");
            }

            TYPE = DataUtilities.createType("Location", stringBuilder.substring(0, stringBuilder.toString().length() - 1));
            GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);

            for (line = reader.readLine(); line != null; line = reader.readLine()) {
                String split[] = line.split("\\,");


                double latitude = Double.parseDouble(split[0]);
                double longitude = Double.parseDouble(split[1]);
                //String name = split[2];
                Object[] o = new Object[split.length+1];
                for (int i = 1; i < o.length; i++) {
                    o[i] = split[i-1];
                }
                o[0] = factory.createPoint(new Coordinate(longitude, latitude));
                System.out.println(o[0]);
                System.out.println(o[1]);
                System.out.println(o[2]);
                System.out.println(o[3]);
                System.out.println(o[4]);
                System.out.println();
                //SimpleFeature feature = SimpleFeatureBuilder.build(TYPE, o, null);
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(o);
                SimpleFeature feature = featureBuilder.buildFeature(null);
                features.add(feature);
            }
        } finally {
            reader.close();
        }
        File newFile = new File("/home/shalini/Escritorio/resultados.shp");

        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

        Map<String, Serializable> create = new HashMap<String, Serializable>();
        create.put("url", newFile.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) factory.createNewDataStore(create);
        newDataStore.createSchema(TYPE);
        newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);

        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
        SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
        featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore.getFeatureSource(typeName);

        featureStore.setTransaction(transaction);
        try {
            featureStore.addFeatures(collection);
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            transaction.rollback();
        } finally {
            transaction.close();
        }
    }

}
