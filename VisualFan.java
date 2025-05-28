import java.awt.image.BufferedImage;
import javax.swing.*;

public class VisualFan extends JLabel {
    private BufferedImage[][] spriteSet;
    private int spriteIndex = 0;
    private static final int SPRITE_SIZE = 32;

    public VisualFan(BufferedImage[][] spriteSet, int x, int y) {
        this.spriteSet = spriteSet;
        setBounds(x, y, SPRITE_SIZE, SPRITE_SIZE);
        setIcon(new ImageIcon(spriteSet[0][0]));
    }

    public void moveAnimated(int targetX, int targetY, int directionRow, int steps, int delayMs, Runnable onFinish) {
        int startX = getX();
        int startY = getY();
        int dx = (targetX - startX) / steps;
        int dy = (targetY - startY) / steps;

        Timer timer = new Timer(delayMs, null);
        final int[] step = {0};

        timer.addActionListener(e -> {
            if (step[0] >= steps) {
                ((Timer) e.getSource()).stop();
                setLocation(targetX, targetY);
                setIcon(new ImageIcon(spriteSet[directionRow][0])); // parado
                if (onFinish != null) onFinish.run();
                return;
            }

            int x = getX() + dx;
            int y = getY() + dy;

            setLocation(x, y);
            spriteIndex = (spriteIndex + 1) % spriteSet[directionRow].length;
            setIcon(new ImageIcon(spriteSet[directionRow][spriteIndex]));

            step[0]++;
        });

        timer.start();
    }
}
