package cz.vithabada.nmr_gui.pulse;

import java.util.logging.Level;
import java.util.logging.Logger;

import libs.Complex;
import libs.Invokable;

public class RandomDataSource extends Pulse<Complex[]> {

    final int length;

    boolean running;

    volatile Complex[] data;

    public RandomDataSource(int len) {
        this.length = len;
        this.running = false;
    }

    @Override
    public void start() {
        running = true;

        while (true) {
            if (!running) {
                break;
            }

            generateData();

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomDataSource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        generateData();

        for (Invokable<Complex[]> event : onComplete) {
            event.invoke(this, getData());
        }
    }

    synchronized void generateData() {
        data = new Complex[length];

        for (int i = 0; i < length; i++) {
            data[i] = new Complex((int) (Math.random() * 10), (int) (Math.random() * 10));
        }
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public synchronized Complex[] getData() {
        return data;
    }
}
