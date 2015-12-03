package libs;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class FFT {

    public static Complex[] transform(Complex[] data) {
        if ((data.length & (data.length - 1)) != 0) {
            data = pad(data);
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        return fft.transform(data, TransformType.FORWARD);
    }

    public static Complex[] modul(Complex[] data) {
        if ((data.length & (data.length - 1)) != 0) {
            data = pad(data);
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] transformed = fft.transform(data, TransformType.FORWARD);

        Complex[] arr = new Complex[data.length];

        for (int i = 0; i < data.length; i++) {
            double re = transformed[i].getReal();
            double im = transformed[i].getImaginary();

            arr[i] = new Complex(Math.sqrt(re*re + im*im));
        }

        return arr;
    }

    private static Complex[] pad(Complex[] data) {
        int padLength = padLength(data.length);

        Complex[] padded = new Complex[padLength];
        System.arraycopy(data, 0, padded, 0, data.length);

        for (int i = data.length; i < padLength; i++) {
            padded[i] = Complex.ZERO;
        }

        return padded;
    }

    public static int padLength(int length) {
        length--;
        length |= length >> 1;
        length |= length >> 2;
        length |= length >> 4;
        length |= length >> 8;
        length |= length >> 16;
        length++;

        return length;
    }
}
