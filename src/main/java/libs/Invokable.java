package libs;

public interface Invokable<T> {

    void invoke(Object sender, T value);
}
