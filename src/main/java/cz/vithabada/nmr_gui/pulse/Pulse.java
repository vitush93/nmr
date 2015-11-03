package cz.vithabada.nmr_gui.pulse;

public interface Pulse<T> {
    
    void start();
    
    void stop();
    
    T getData();
}
