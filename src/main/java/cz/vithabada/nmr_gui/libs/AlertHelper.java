package cz.vithabada.nmr_gui.libs;


import javafx.scene.control.Alert;

/**
 * Helper class (factory) for JavaFX the most basic alert dialogs.
 *
 * @author Vit Habada
 */
public final class AlertHelper {

    /**
     * Displays an alert dialog with given title and description.
     *
     * @param alertType alert type constant.
     * @param title title of the dialog window.
     * @param text description displayed in the dialog window.
     */
    public static void showAlert(Alert.AlertType alertType, String title, String text) {
        Alert alert = createAlert(alertType, title, text);

        alert.showAndWait();
    }

    /**
     * Creates an alert dialog with given title and description.
     *
     * @param alertType alert type constant.
     * @param title title of the dialog window.
     * @param text description displayed in the dialog window.
     * @return prepared Alert instance.
     */
    public static Alert createAlert(Alert.AlertType alertType, String title, String text) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        return alert;
    }
}
