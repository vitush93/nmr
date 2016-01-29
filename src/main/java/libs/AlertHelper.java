package libs;


import javafx.scene.control.Alert;

public final class AlertHelper {

    public static void showAlert(Alert.AlertType alertType, String title, String text) {
        Alert alert = createAlert(alertType, title, text);

        alert.showAndWait();
    }

    public static Alert createAlert(Alert.AlertType alertType, String title, String text) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        return alert;
    }
}
