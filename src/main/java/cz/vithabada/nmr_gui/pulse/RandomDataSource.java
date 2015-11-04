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

        int count = 0;
        while (true) {
            if (!running) {
                break;
            }

            synchronized (this) {
                generateData();
                onFetch.invoke(this, data);
                onRefresh.invoke(this, count++);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomDataSource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        synchronized (this) {
            generateData();
            onComplete.invoke(this, data);
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
    public Complex[] getData() {
        return data;
    }
}
