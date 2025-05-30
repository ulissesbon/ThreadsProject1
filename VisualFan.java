import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class VisualFan extends JLabel {
    private BufferedImage[][] spriteSet;
    private int spriteIndex = 0;
    private double scale;
    private static final int SPRITE_ORIGINAL_SIZE = 32;

    public VisualFan(BufferedImage[][] spriteSet, int x, int y) {
        this(spriteSet, x, y, 1.5); // valor padrão de escala
    }

    public VisualFan(BufferedImage[][] spriteSet, int x, int y, double scale) {
        this.spriteSet = spriteSet;
        this.scale = scale;

        int scaledSize = (int)(SPRITE_ORIGINAL_SIZE * scale);
        setBounds(x, y, scaledSize, scaledSize);

        BufferedImage resized = resizeSprite(spriteSet[0][0]);
        setIcon(new ImageIcon(resized));
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
                BufferedImage stoppedSprite = resizeSprite(spriteSet[directionRow][0]);
                setIcon(new ImageIcon(stoppedSprite));
                return;
            }

            int x = getX() + dx;
            int y = getY() + dy;

            setLocation(x, y);
            spriteIndex = (spriteIndex + 1) % spriteSet[directionRow].length;
            BufferedImage nextSprite = resizeSprite(spriteSet[directionRow][spriteIndex]);
            setIcon(new ImageIcon(nextSprite));

            step[0]++;
        });

        timer.start();
    }

    private BufferedImage resizeSprite(BufferedImage original) {
        int width = (int)(original.getWidth() * scale);
        int height = (int)(original.getHeight() * scale);

        Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        return resized;
    }

    public void moveAndWait(int targetX, int targetY, int directionRow, int steps, int delayMs) {
        final Object lock = new Object();

        Runnable onFinish = () -> {
            synchronized (lock) {
                lock.notify(); // acorda a thread
            }
        };

        synchronized (lock) {
            moveAnimated(targetX, targetY, directionRow, steps, delayMs, onFinish);
            try {
                lock.wait(); // bloqueia até a animação terminar
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
