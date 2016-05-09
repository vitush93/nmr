package cz.vithabada.nmr_gui.model;


import cz.vithabada.nmr_gui.forms.Parameters;
import org.apache.commons.math3.complex.Complex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Represents collected experiment data as plain text to be stored in file.
 *
 * @author Vit Habada
 */
public class PlainTextData {

    /**
     * Measured experiment data.
     */
    private Complex[] data;

    /**
     * @param data experiment data.
     */
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

    /**
     * Save data to file.
     *
     * @param file File to be saved.
     */
    public void toFile(Parameters parameters, File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.print(parameters);
            pw.print("\n\n");
            pw.print(this.toString());
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
