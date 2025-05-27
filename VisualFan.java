import java.awt.*;
import javax.swing.*;

public class VisualFan extends JLabel {
    private static final int SIZE = 32;

    public VisualFan(String label, Color color) {
        setText(label);
        setOpaque(true);
        setBackground(color);
        setHorizontalAlignment(SwingConstants.CENTER);
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 10));
        setBounds(900, 480, SIZE, SIZE); // ponto inicial (entrada)
    }

    public void moveTo(Point target, int steps, int delayMs) {
        Point current = getLocation();
        int dx = (target.x - current.x) / steps;
        int dy = (target.y - current.y) / steps;

        for (int i = 0; i < steps; i++) {
            final int x = current.x + dx * i;
            final int y = current.y + dy * i;
            SwingUtilities.invokeLater(() -> setLocation(x, y));
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> setLocation(target.x, target.y));
    }

    public void delayMovementTo(Point target, int steps, int delayMs) {
        moveTo(target, steps, delayMs);
        try {
            Thread.sleep(steps * delayMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
