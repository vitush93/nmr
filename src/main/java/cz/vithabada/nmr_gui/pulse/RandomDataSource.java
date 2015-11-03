package cz.vithabada.nmr_gui.pulse;

import java.util.logging.Level;
import java.util.logging.Logger;
import libs.Complex;

public class RandomDataSource implements Pulse<Complex[]> {

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

            synchronized (this) {
                data = new Complex[length];

                for (int i = 0; i < length; i++) {
                    data[i] = new Complex((int) (Math.random() * 10), (int) (Math.random() * 10));
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(RandomDataSource.class.getName()).log(Level.SEVERE, null, ex);
            }
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
