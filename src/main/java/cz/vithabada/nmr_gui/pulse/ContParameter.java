package cz.vithabada.nmr_gui.pulse;

/**
 * @author Vit Habada
 */
public class ContParameter {

    /**
     * Parameter identifier constants.
     */
    public static final int
            AMP_GAIN = 0,
            TAU = 1,
            REPETITION_DELAY = 2,
            AMPLITUDE = 3,
            PTS_FREQ = 4;

    /**
     * Parameter identifier.
     */
    private int id;

    /**
     * Displayed parameter name.
     */
    private String name;

    /**
     * @param id parameter identifier.
     * @param name parameter name.
     */
    public ContParameter(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
