package cz.vithabada.nmr_gui.pulse;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.complex.Complex;

/**
 * Mock data source.
 *
 * @author Vit Habada
 */
public class RandomDataSource extends Pulse<Complex[]> {

    /**
     * Sample size after each simulated scan.
     */
    private final int length;

    /**
     * Flag that indicates whether the experiment is running.
     */
    private boolean running;

    /**
     * Collected data.
     */
    private volatile Complex[] data;

    /**
     * @param len desired sample size.
     */
    public RandomDataSource(int len) {
        this.length = len;
        this.running = false;
    }

    /**
     * Starts the experiment simulation.
     */
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

    /**
     * Generates data sample.
     */
    private synchronized void generateData() {
        data = new Complex[length];

        for (int i = 0; i < length; i++) {
            data[i] = new Complex((int) (Math.random() * 10), (int) (Math.random() * 10));
        }
    }

    /**
     * Stops the simulated experiment.
     */
    @Override
    public void stop() {
        running = false;
    }

    /**
     * Retrieves collected data.
     *
     * @return mock data from the simulated experiment.
     */
    @Override
    public Complex[] getData() {
        return data;
    }
}
