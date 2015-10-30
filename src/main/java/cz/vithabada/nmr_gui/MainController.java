package cz.vithabada.nmr_gui;

import cz.vithabada.nmr_gui.pulse.RandomDataSource;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentNavigableMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import libs.Complex;
import libs.Invokable;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MainController implements Initializable {

    @FXML
    private LineChart<String, Number> lineChart;
    
    @FXML
    private NumberAxis xAxis;
    
    @FXML
    private NumberAxis yAxis;

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

        for (Integer key : map2.keySet()) {
            System.out.println(map2.get(key));
        }

        db.close();
    }

    @FXML
    private void handleStart(ActionEvent event) {
        lineChart.setVisible(true);

        final RandomDataSource source = new RandomDataSource(10);
        source.onFetch.add(new Invokable<Complex[]>() {

            @Override
            public void invoke(Object sender, Complex[] value) {
                lineChart.getData().clear();
                
                final XYChart.Series real = new XYChart.Series();
                real.setName("Real");

                final XYChart.Series imag = new XYChart.Series();
                imag.setName("Imaginary");

                int count = 0;
                for (Complex c : value) {
                    real.getData().add(new XYChart.Data(count, c.getReal()));
                    imag.getData().add(new XYChart.Data(count, c.getImag()));

                    count++;
                }

                lineChart.getData().add(real);
                lineChart.getData().add(imag);
            }
        });
        
        Task task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                source.start();
                
                return null;
            }
            
        };
        
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }
}
