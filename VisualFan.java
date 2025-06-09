import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class VisualFan extends JLabel {
    public static final int DIRECTION_DOWN = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_UP = 3;

    private static final int SHEET_ROW_ANIM_DOWN = 0;
    private static final int SHEET_ROW_ANIM_SIDE = 1;
    private static final int SHEET_ROW_ANIM_UP = 3;

    private BufferedImage[][] spriteSetOriginal;
    private BufferedImage[][] spriteSetMirrored;
    private BufferedImage[][] currentSpriteSetToUse;
    private BufferedImage[][] spriteWalking1;
    private BufferedImage[][] spriteWalking2;
    private int spriteIndex = 0;
    private double scale;
    private static final int SPRITE_DEFAULT_WIDTH_OR_HEIGHT = 32; // Fallback if sprite is null
    private boolean useFirstSprite = true;

    public VisualFan(BufferedImage[][] spriteSetOriginal, BufferedImage[][] spriteSetMirrored, int x, int y, double scale) {
        this.spriteSetOriginal = spriteSetOriginal;
        this.spriteSetMirrored = spriteSetMirrored;
        this.scale = scale;
        this.currentSpriteSetToUse = this.spriteSetOriginal; // Default

        BufferedImage initialSprite = null;
        if (this.currentSpriteSetToUse != null &&
            SHEET_ROW_ANIM_DOWN < this.currentSpriteSetToUse.length &&
            this.currentSpriteSetToUse[SHEET_ROW_ANIM_DOWN] != null &&
            this.currentSpriteSetToUse[SHEET_ROW_ANIM_DOWN].length > 0) {
            initialSprite = this.currentSpriteSetToUse[SHEET_ROW_ANIM_DOWN][0];
        }
        
        ImageIcon icon = new ImageIcon(resizeSprite(initialSprite));
        setIcon(icon);

        int scaledWidth = icon.getIconWidth() > 0 ? icon.getIconWidth() : (int)(SPRITE_DEFAULT_WIDTH_OR_HEIGHT * scale);
        int scaledHeight = icon.getIconHeight() > 0 ? icon.getIconHeight() : (int)(SPRITE_DEFAULT_WIDTH_OR_HEIGHT * scale);
        setBounds(x, y, scaledWidth, scaledHeight);
    }

    public VisualFan(BufferedImage[][] spriteSetOriginal, BufferedImage[][] spriteSetMirrored, BufferedImage[][] spriteWalking1, BufferedImage[][] spriteWalking2, int x, int y, double scale) {
        this.spriteSetOriginal = spriteSetOriginal;
        this.spriteSetMirrored = spriteSetMirrored;
        this.spriteWalking1 = spriteWalking1;
        this.spriteWalking2 = spriteWalking2;
        this.scale = scale;
        this.currentSpriteSetToUse = this.spriteSetOriginal;

        BufferedImage initialSprite = null;
        if (this.currentSpriteSetToUse != null &&
            SHEET_ROW_ANIM_DOWN < this.currentSpriteSetToUse.length &&
            this.currentSpriteSetToUse[SHEET_ROW_ANIM_DOWN] != null &&
            this.currentSpriteSetToUse[SHEET_ROW_ANIM_DOWN].length > 0) {
            initialSprite = this.currentSpriteSetToUse[SHEET_ROW_ANIM_DOWN][0];
        }
        
        ImageIcon icon = new ImageIcon(resizeSprite(initialSprite));
        setIcon(icon);

        int scaledWidth = icon.getIconWidth() > 0 ? icon.getIconWidth() : (int)(SPRITE_DEFAULT_WIDTH_OR_HEIGHT * scale);
        int scaledHeight = icon.getIconHeight() > 0 ? icon.getIconHeight() : (int)(SPRITE_DEFAULT_WIDTH_OR_HEIGHT * scale);
        setBounds(x, y, scaledWidth, scaledHeight);
    }

    public VisualFan(BufferedImage[][] spriteSetOriginal, BufferedImage[][] spriteSetMirrored, int x, int y) {
        this(spriteSetOriginal, spriteSetMirrored, x, y, 1.5);
    }

    public void setCurrentSpriteSheet(BufferedImage[][] SpriteSetToUse){
        this.currentSpriteSetToUse = SpriteSetToUse;
        repaint();
    }

    public void moveToAndWait(int targetX, int targetY, int diffBetweenSteps, int delayMs) {
        Point current = new Point(getX(), getY());
        Point destiny = new Point(targetX, targetY);

        while (Math.abs(destiny.x - current.x) > diffBetweenSteps || Math.abs(destiny.y - current.y) > diffBetweenSteps) {
            int dx = Integer.compare(destiny.x - current.x, 0) * diffBetweenSteps;
            int dy = Integer.compare(destiny.y - current.y, 0) * diffBetweenSteps;

            Point next = new Point(current.x + dx, current.y + dy);
            setLocation(next);
            current = next;

            long now = System.nanoTime();
            long waitUntil = now + (delayMs * 1_000_000L);
            while (System.nanoTime() < waitUntil) {
                // busy-wait até atingir o tempo de "delay" por passo
            }
        }

        setLocation(destiny);
    }

    public void _moveToAndWait(int targetX, int targetY, int diffBetweenSteps, int delayMs) {
        final Object lock = new Object();

        Point destinyPoint = new Point(targetX, targetY);
        Point[] currentPoint = { new Point(getX(), getY()) };

        final boolean[] finished = {false}; // status de conclusão

        setCurrentSpriteSheet(spriteSetOriginal);

        Timer timer = new Timer(delayMs, null);
        timer.addActionListener(e -> {
            Point current = currentPoint[0];
            int dx = destinyPoint.x - current.x;
            int dy = destinyPoint.y - current.y;

            if (Math.abs(dx) <= diffBetweenSteps && Math.abs(dy) <= diffBetweenSteps) {
                setLocation(destinyPoint);
                setCurrentSpriteSheet(spriteSetOriginal);
                timer.stop();
                synchronized (lock) {
                    finished[0] = true;
                    lock.notify(); // acorda a thread esperando
                }
                return;
            }

            int stepX = Integer.compare(dx, 0) * diffBetweenSteps;
            int stepY = Integer.compare(dy, 0) * diffBetweenSteps;

            Point nextPoint = new Point(current.x + stepX, current.y + stepY);
            setLocation(nextPoint);
            currentPoint[0] = nextPoint;

            if (useFirstSprite) {
                setCurrentSpriteSheet(spriteWalking1);
            } else {
                setCurrentSpriteSheet(spriteWalking2);
            }
            useFirstSprite = !useFirstSprite;
        });

        synchronized (lock) {
            timer.start();
            while (!finished[0]) {
                try {
                    lock.wait(); // espera até o movimento terminar
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    ex.printStackTrace();
                }
            }
        }
    }

    private BufferedImage resizeSprite(BufferedImage original) {
        if (original == null) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        int width = (int)(original.getWidth() * scale);
        int height = (int)(original.getHeight() * scale);
        if (width <= 0 || height <= 0) {
             return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    public static BufferedImage[][] loadSpriteSheet(String filename, int rows, int cols) {
        try {
            File imageFile = new File(filename);
            if (!imageFile.exists()) {
                System.err.println("Sprite sheet file not found: " + filename);
                return null;
            }
            BufferedImage spriteSheet = ImageIO.read(imageFile);
            if (spriteSheet == null) {
                System.err.println("Failed to read sprite sheet: " + filename);
                return null;
            }
            if (rows <= 0 || cols <= 0 || spriteSheet.getWidth() < cols || spriteSheet.getHeight() < rows) {
                System.err.println("Invalid rows/cols or sprite sheet dimensions for: " + filename);
                return null;
            }
            int spriteWidth = spriteSheet.getWidth() / cols;
            int spriteHeight = spriteSheet.getHeight() / rows;
            if (spriteWidth <= 0 || spriteHeight <= 0) {
                 System.err.println("Calculated spriteWidth/spriteHeight is zero or negative for: " + filename);
                return null;
            }
            BufferedImage[][] sprites = new BufferedImage[rows][cols];
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    sprites[y][x] = spriteSheet.getSubimage(x * spriteWidth, y * spriteHeight, spriteWidth, spriteHeight);
                }
            }
            return sprites;
        } catch (Exception e) {
            System.err.println("Exception while loading sprite sheet: " + filename);
            e.printStackTrace();
            return null;
        }
    }

    public void showStatusIcon(String iconPath) {
    try {
        BufferedImage iconImage = ImageIO.read(new File(iconPath));
        JLabel iconLabel = new JLabel(new ImageIcon(iconImage));
        iconLabel.setSize(iconImage.getWidth(), iconImage.getHeight());

        int iconX = this.getX() + (this.getWidth() - iconLabel.getWidth()) / 2;
        int iconY = this.getY() + 20 - iconLabel.getHeight();

        iconLabel.setLocation(iconX, iconY);
        iconLabel.setName("statusIcon");

        Container parent = this.getParent();
        if (parent instanceof JLayeredPane) {
            JLayeredPane pane = (JLayeredPane) parent;
            removeStatusIcon(); // remove anterior se houver
            pane.add(iconLabel, JLayeredPane.PALETTE_LAYER);
            //pane.repaint();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public void removeStatusIcon() {
    Container parent = this.getParent();
    if (parent instanceof JLayeredPane) {
        JLayeredPane pane = (JLayeredPane) parent;
        for (Component comp : pane.getComponents()) {
            if (comp instanceof JLabel && "statusIcon".equals(comp.getName())) {
                pane.remove(comp);
                pane.repaint();
                break;
            }
        }
    }
}

}