import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JFrame {
    private JPanel mainPanel;
    private JButton oneButton;
    private JButton button1;
    private JButton button2;
    private JButton button3;

    public static void main(String[] args) {
        Main window = new Main();
        window.setContentPane(window.mainPanel);
        window.setTitle("Restaurant Inventory Stock Management System");
        window.setExtendedState(MAXIMIZED_BOTH);
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}
