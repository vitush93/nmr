package cz.vithabada.nmr_gui.pulse;

public class ContParameter {
    public static final int
            AMP_GAIN = 0,
            TAU = 1,
            REPETITION_DELAY = 2,
            AMPLITUDE = 3,
            PTS_FREQ = 4;

    private int id;
    private String name;

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
