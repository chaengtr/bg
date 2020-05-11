package management;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertManagement {

    public void alertFaultWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("ผิดพลาด");
        alert.setContentText("กรุณาตรวจสอบความถูกต้อง");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public boolean alertConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("ยืนยัน");
        alert.setContentText("กด OK เพื่อยืนยัน");
        alert.setHeaderText(null);

        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }

    public boolean alertSignOutConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("ออกจากระบบ");
        alert.setContentText("กด OK เพื่อออกจากระบบ");
        alert.setHeaderText(null);

        Optional<ButtonType> option = alert.showAndWait();
        if (option.get() == ButtonType.OK) {
            return true;
        }
        return false;
    }
}
