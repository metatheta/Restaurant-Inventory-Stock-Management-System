import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JFrame {
    private JButton testButton;
    private JPanel mainPanel;

    public static void main(String[] args) {
        Main window = new Main();
        window.setContentPane(window.mainPanel);
        window.setTitle("Restaurant Inventory Stock Management System");
        window.setSize(1366, 768);
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
