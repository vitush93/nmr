package cz.vithabada.nmr_gui.pulse;

import cz.vithabada.nmr_gui.libs.Invokable;

/**
 * Defines contract for all pulse series classes.
 *
 * @param <T> type of the collected data - typically array of Complex.
 * @author Vit Habada
 */
public abstract class Pulse<T> {

    /**
     * Invokes when status update is needed.
     */
    public Invokable<Number> onRefresh;

    /**
     * Invokes after each scan when data is collected.
     */
    public Invokable<T> onFetch;

    /**
     * Invokes when the experiment is complete.
     */
    public Invokable<T> onComplete;

    /**
     * Invokes when error occurs during scan.
     */
    public Invokable<Void> onError;

    /**
     * Starts the experiment.
     */
    public abstract void start();

    /**
     * Stops the experiment.
     */
    public abstract void stop();

    /**
     * Retrieves data from the experiment.
     *
     * @return measured experiment data.
     */
    public abstract T getData();
}
