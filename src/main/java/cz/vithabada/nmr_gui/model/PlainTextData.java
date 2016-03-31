package cz.vithabada.nmr_gui.model;


import org.apache.commons.math3.complex.Complex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class PlainTextData {

    Complex[] data;

    public PlainTextData(Complex[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(data.length);
        for (Complex c : data) {
            sb.append(System.lineSeparator());
            sb.append(c.toString());
        }

        return sb.toString();
    }

    public void toFile(File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.print(this.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
