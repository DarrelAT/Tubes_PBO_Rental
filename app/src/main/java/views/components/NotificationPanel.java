package views.components;

import javax.swing.*;
import java.awt.*;

public class NotificationPanel extends JPanel {
    private JLabel lblMessage;

    public NotificationPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        lblMessage = new JLabel("");
        lblMessage.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(lblMessage);
        setVisible(false);
    }

    public void showSuccess(String message) {
        setBackground(new Color(212, 237, 218));
        lblMessage.setForeground(new Color(21, 87, 36));
        lblMessage.setText("✔ " + message);
        setVisible(true);
    }

    public void showError(String message) {
        setBackground(new Color(248, 215, 218));
        lblMessage.setForeground(new Color(114, 28, 36));
        lblMessage.setText("✖ " + message);
        setVisible(true);
    }

    public void clear() {
        setVisible(false);
    }
}