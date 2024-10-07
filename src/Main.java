import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TwoPassAssemblerGUI gui;
            try {
                gui = new TwoPassAssemblerGUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            gui.setVisible(true);
        });
    }
}