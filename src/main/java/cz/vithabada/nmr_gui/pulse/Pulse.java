package cz.vithabada.nmr_gui.pulse;

import java.util.List;
import libs.Complex;
import libs.Invokable;

public abstract class Pulse {

    public List<Invokable<Complex[]>> onFetch;

    public abstract void start();

    public abstract void stop();

}
