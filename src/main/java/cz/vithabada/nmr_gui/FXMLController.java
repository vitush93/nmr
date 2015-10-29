package cz.vithabada.nmr_gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentNavigableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class FXMLController implements Initializable {
    
    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        
        DB db;
        db = DBMaker.fileDB(new File("database"))
                .encryptionEnable("password")
                .make();

        ConcurrentNavigableMap<Integer, String> map = db.treeMap("collectionName");

        map.put(1, "one");
        map.put(2, "two");
        
        db.commit();
        
        ConcurrentNavigableMap<Integer, String> map2 = db.treeMap("collectionName");
        
        for(Integer key : map2.keySet()) {
            System.out.println(map2.get(key));
        }
        
        db.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final XYChart.Series real = new XYChart.Series();
        real.setName("Real");
        
        real.getData().add(new XYChart.Data("1", 23));
        real.getData().add(new XYChart.Data("2", 14));
        real.getData().add(new XYChart.Data("3", 15));
        
        final XYChart.Series imag = new XYChart.Series();
        imag.setName("Imaginary");
        
        imag.getData().add(new XYChart.Data("1", 12));
        imag.getData().add(new XYChart.Data("2", 8));
        imag.getData().add(new XYChart.Data("3", 2));
        
        lineChart.getData().add(real);
        lineChart.getData().add(imag);
    }
}
