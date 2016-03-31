package cz.vithabada.nmr_gui.libs;

public interface Invokable<T> {

    void invoke(Object sender, T value);
}