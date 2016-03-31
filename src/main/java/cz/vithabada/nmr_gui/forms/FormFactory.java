package cz.vithabada.nmr_gui.forms;


import com.dooapp.fxform.FXForm;
import com.dooapp.fxform.builder.FXFormBuilder;
import com.dooapp.fxform.view.skin.FXMLSkin;

import java.util.ResourceBundle;

/**
 * Factory class for JavaFX forms.
 *
 * @author Vit Habada
 */
public class FormFactory {

    /**
     * Pulse parameters to be initialized.
     */
    private Parameters parameters;

    /**
     * Reference to the JavaFX resource bundle.
     */
    private ResourceBundle resourceBundle;

    /**
     * Path to the FXML resource directory.
     */
    private String fxmlPath;

    /**
     *
     * @param rb JavaFX resource bundle.
     * @param parameters Pulse parameters.
     * @param fxmlPath Path to the FXML resource directory.
     */
    private FormFactory(ResourceBundle rb, Parameters parameters, String fxmlPath) {
        this.parameters = parameters;
        this.resourceBundle = rb;
        this.fxmlPath = fxmlPath;
    }

    /**
     * @return Prepared FXForm instance.
     */
    private FXForm create() {
        FXForm form = new FXFormBuilder<>().resourceBundle(resourceBundle).build();
        form.setSkin(new FXMLSkin(form, getClass().getResource(fxmlPath)));
        form.setMinWidth(300);
        form.setSource(parameters);

        return form;
    }

    /**
     * Exposed factory method for FXForm.
     *
     * @param rb JavaFX resource bundle.
     * @param parameters Pulse parameters.
     * @param fxmlPath Path to the FXML resource directory.
     * @return FXForm instance.
     */
    public static FXForm create(ResourceBundle rb, Parameters parameters, String fxmlPath) {
        return new FormFactory(rb, parameters, fxmlPath).create();
    }
}
