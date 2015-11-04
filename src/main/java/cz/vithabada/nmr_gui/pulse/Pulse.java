package cz.vithabada.nmr_gui.pulse;

import libs.Invokable;

public abstract class Pulse<T> {

    public Invokable<Number> onRefresh;

    public Invokable<T> onFetch;

    public Invokable<T> onComplete;

    public Invokable<Void> onError;

    public abstract void start();

    public abstract void stop();

    public abstract T getData();
}
