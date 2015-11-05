package libs;

/**
 * @deprecated use org.apache.commons.math3.complex.Complex instead.
 */
public class Complex {

    int real;
    int imag;

    public Complex(int real, int imag) {
        this.real = real;
        this.imag = imag;
    }

    public static Complex[] createArray(int[] real, int[] imag) throws Exception {
        if (real.length != imag.length) {
            throw new Exception("Real and imaginary arrays must be the same length.");
        }

        Complex[] complex = new Complex[real.length];
        for (int i = 0; i < real.length; i++) {
            Complex c = new Complex(real[i], imag[i]);

            complex[i] = c;
        }

        return complex;
    }

    public int getReal() {
        return real;
    }

    public void setReal(int real) {
        this.real = real;
    }

    public int getImag() {
        return imag;
    }

    public void setImag(int imag) {
        this.imag = imag;
    }

}
