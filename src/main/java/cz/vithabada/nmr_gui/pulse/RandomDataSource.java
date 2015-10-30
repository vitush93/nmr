package cz.vithabada.nmr_gui.pulse;

import java.util.ArrayList;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import libs.Complex;
import libs.Invokable;

public class RandomDataSource extends Pulse {

    final int length;

    Timeline clock;

    public RandomDataSource(int len) {
        this.onFetch = new ArrayList<>();
        this.length = len;
    }

    @Override
    public void start() {

        clock = new Timeline(new KeyFrame(Duration.seconds(0), new EventHandler<ActionEvent>() {

            private Complex[] generate() {
                Complex[] arr = new Complex[length];
                for (int i = 0; i < length; i++) {
                    arr[i] = new Complex((int) (Math.random() * 10), (int) (Math.random() * 10));
                }

                return arr;
            }

            @Override
            public void handle(ActionEvent event) {
                for (Invokable<Complex[]> inv : onFetch) {
                    inv.invoke(RandomDataSource.this, generate());
                }
            }

        }), new KeyFrame(Duration.seconds(1)));

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    @Override
    public void stop() {
        clock.stop();
    }
}
