package cz.vithabada.nmr_gui.pulse;

import libs.Invokable;

import java.util.ArrayList;
import java.util.List;

public abstract class Pulse<T> {

    public List<Invokable<T>> onFetch = new ArrayList<>();

    public List<Invokable<T>> onComplete = new ArrayList<>();

    public List<Invokable<Void>> onError = new ArrayList<>();
    
    public abstract void start();
    
    public abstract void stop();
    
    public abstract T getData();
}
