package cz.vithabada.nmr_gui.forms;


import com.dooapp.fxform.FXForm;
import com.dooapp.fxform.builder.FXFormBuilder;
import com.dooapp.fxform.view.skin.FXMLSkin;

import java.util.ResourceBundle;

public class FormFactory {

    Parameters parameters;
    ResourceBundle resourceBundle;
    String fxmlPath;

    public FormFactory(ResourceBundle rb, Parameters parameters, String fxmlPath) {
        this.parameters = parameters;
        this.resourceBundle = rb;
        this.fxmlPath = fxmlPath;
    }

    public FXForm create() {
        FXForm form = new FXFormBuilder<>().resourceBundle(resourceBundle).build();
        form.setSkin(new FXMLSkin(form, getClass().getResource(fxmlPath)));
        form.setMinWidth(300);
        form.setSource(parameters);

        return form;
    }

    public static FXForm create(ResourceBundle rb, Parameters parameters, String fxmlPath) {
        return new FormFactory(rb, parameters, fxmlPath).create();
    }
}
