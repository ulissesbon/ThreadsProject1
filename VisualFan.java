import java.awt.image.BufferedImage;
import javax.swing.*;

public class VisualFan extends JLabel {

    private static final int FRAME_WIDTH = 32;  // ou spriteWidth se você preferir dinâmico
    private static final int FRAME_HEIGHT = 32;
    private BufferedImage[][] spriteSet;
    private int rowIndex = 0;

    public VisualFan(BufferedImage[][] spriteSet, int initialX, int initialY) {
        this.spriteSet = spriteSet;
        setBounds(initialX, initialY, FRAME_WIDTH, FRAME_HEIGHT);
        setIcon(new ImageIcon(spriteSet[0][0]));
    }

    public void moveAnimated(int targetX, int targetY, int directionRow, int steps, int delayMs) {
        int dx = (targetX - getX()) / steps;
        int dy = (targetY - getY()) / steps;

        for (int i = 0; i < steps; i++) {
            int finalI = i;
            SwingUtilities.invokeLater(() -> {
                int x = getX() + dx;
                int y = getY() + dy;
                setLocation(x, y);

                int col = finalI % spriteSet[directionRow].length;
                setIcon(new ImageIcon(spriteSet[directionRow][col]));
            });

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            setLocation(targetX, targetY);
            setIcon(new ImageIcon(spriteSet[directionRow][0])); // parada
        });
    }
}
