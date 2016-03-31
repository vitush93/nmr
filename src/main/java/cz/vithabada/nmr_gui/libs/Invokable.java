package cz.vithabada.nmr_gui.libs;

/**
 * A very simple no handler event imitation.
 *
 * @param <T> type of parameter to pass for invocation.
 * @author Vit Habada
 */
public interface Invokable<T> {

    /**
     * Do some magic with given value.
     *
     * @param sender reference to caller.
     * @param value invocation parameter.
     */
    void invoke(Object sender, T value);
}