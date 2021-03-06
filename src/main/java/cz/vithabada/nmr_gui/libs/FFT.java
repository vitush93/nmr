package cz.vithabada.nmr_gui.libs;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * Provides cleaner interface for the apache FFT impl.
 *
 * @author Vit Habada
 */
public class FFT {

    /**
     * Performs forward FFT transform on given Complex data.
     *
     * @param data data to be transformed.
     * @return transformed data.
     */
    public static Complex[] transform(Complex[] data) {
        if ((data.length & (data.length - 1)) != 0) {
            data = pad(data);
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        return fft.transform(data, TransformType.FORWARD);
    }

    public static Complex[] fixFFTdata(Complex[] transformed) {
        Complex[] temp = new Complex[transformed.length];
        for (int i = 0; i < transformed.length; i++) {
            temp[(transformed.length / 2 + i) % transformed.length] = transformed[i % transformed.length];
        }

        ArrayUtils.reverse(temp);

        return temp;
    }

    /**
     * Computes the FFT modulus on given data.
     *
     * @param data data to transform.
     * @return transformed data.
     */
    public static Complex[] modulFFT(Complex[] data) {
        if ((data.length & (data.length - 1)) != 0) {
            data = pad(data);
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] transformed = fft.transform(data, TransformType.FORWARD);

        return modul(transformed);
    }

    public static Complex[] modul(Complex[] data) {
        Complex[] arr = new Complex[data.length];

        for (int i = 0; i < data.length; i++) {
            double re = data[i].getReal();
            double im = data[i].getImaginary();

            arr[i] = new Complex(Math.sqrt(re * re + im * im));
        }

        return arr;
    }

    public static double dataMax(Complex[] data) {
        if (data.length == 0) {
            throw new RuntimeException("Provided data set is empty");
        }
        double modul_max = Double.MIN_VALUE;
        for (Complex c : data) {
            if (c.getReal() > modul_max) {
                modul_max = c.getReal();
            }
        }

        return modul_max;
    }

    public static double dataIntegral(Complex[] data) {
        double modul_int = 0;
        for (Complex c : data) {
            modul_int += c.getReal();
        }

        return modul_int;
    }

    public static double[] extractReal(Complex[] data) {
        return extract(data, true);
    }

    public static double[] extractImaginary(Complex[] data) {
        return extract(data, false);
    }

    private static double[] extract(Complex[] data, boolean real) {
        double[] arr = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            Complex c = data[i];
            if (real) {
                arr[i] = c.getReal();
            } else {
                arr[i] = c.getImaginary();
            }
        }

        return arr;
    }

    /**
     * Prepend given dataset with zeros to the closest 2^n length.
     *
     * @param data data to pad.
     * @return zero-padded dataset of the closest length 2^n.
     */
    private static Complex[] pad(Complex[] data) {
        int padLength = padLength(data.length);

        Complex[] padded = new Complex[padLength];
        System.arraycopy(data, 0, padded, 0, data.length);

        for (int i = data.length; i < padLength; i++) {
            padded[i] = Complex.ZERO;
        }

        return padded;
    }

    /**
     * Rounds given number to the closest upper power of 2.
     *
     * @param number original number to round.
     * @return number rounded to the closest upper power of 2.
     */
    private static int padLength(int number) {
        number--;
        number |= number >> 1;
        number |= number >> 2;
        number |= number >> 4;
        number |= number >> 8;
        number |= number >> 16;
        number++;

        return number;
    }
}
