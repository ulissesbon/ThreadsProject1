// VisualFan.java

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class VisualFan extends JLabel {
    // Semantic Directions (to be used by Fan.java)
    public static final int DIRECTION_DOWN = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_UP = 3;

    // Actual row indices in the sprite sheets for animations
    // These MUST correspond to how your sprite sheets are organized.
    // Example: If your side-view animation (left/right) is in the 2nd row of the sheet (index 1)
    private static final int SHEET_ROW_ANIM_DOWN = 0;
    private static final int SHEET_ROW_ANIM_SIDE = 1; // Assuming row 1 (0-indexed) is for side-facing frames
    private static final int SHEET_ROW_ANIM_UP = 3;   // Assuming row 3 is for up-facing frames

    private BufferedImage[][] spriteSetOriginal;
    private BufferedImage[][] spriteSetMirrored;
    private BufferedImage[][] currentSpriteSetToUse;
    private int spriteIndex = 0;
    private double scale;
    private static final int SPRITE_DEFAULT_WIDTH_OR_HEIGHT = 32; // Fallback if sprite is null

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

    public VisualFan(BufferedImage[][] spriteSetOriginal, BufferedImage[][] spriteSetMirrored, int x, int y) {
        this(spriteSetOriginal, spriteSetMirrored, x, y, 1.5);
    }

    private BufferedImage[][] getEffectiveSpriteSet() {
        return (this.currentSpriteSetToUse == null) ? new BufferedImage[][]{{null}} : this.currentSpriteSetToUse;
    }

    public void moveAnimated(int targetX, int targetY, int semanticDirection, int steps, int delayMs, Runnable onFinish) {
        int startX = getX();
        int startY = getY();

        if (steps <= 0) {
            setLocation(targetX, targetY);
            if (onFinish != null) onFinish.run();
            return;
        }

        int dx = (targetX - startX) / steps;
        // int dy = (targetY - startY) / steps; // dy not directly used for sheet choice here

        int actualSheetRow; // The row index to use from the chosen sprite sheet

        switch (semanticDirection) {
            case DIRECTION_LEFT:
                this.currentSpriteSetToUse = this.spriteSetMirrored; // Use mirrored sheet for left-facing
                actualSheetRow = SHEET_ROW_ANIM_SIDE;
                break;
            case DIRECTION_RIGHT:
                this.currentSpriteSetToUse = this.spriteSetOriginal;  // Use original sheet for right-facing
                actualSheetRow = SHEET_ROW_ANIM_SIDE;
                break;
            case DIRECTION_UP:
                this.currentSpriteSetToUse = this.spriteSetOriginal;
                actualSheetRow = SHEET_ROW_ANIM_UP;
                break;
            case DIRECTION_DOWN:
            default: // Default to down
                this.currentSpriteSetToUse = this.spriteSetOriginal;
                actualSheetRow = SHEET_ROW_ANIM_DOWN;
                break;
        }

        // Fallback if the chosen primary sheet for a direction is null (e.g. mirrored not loaded)
        if (this.currentSpriteSetToUse == null) {
            if (semanticDirection == DIRECTION_LEFT && this.spriteSetOriginal != null) {
                this.currentSpriteSetToUse = this.spriteSetOriginal; // Try original if mirrored failed
            } else if (semanticDirection == DIRECTION_RIGHT && this.spriteSetMirrored != null) {
                this.currentSpriteSetToUse = this.spriteSetMirrored; // Try mirrored if original failed
            } else { // For up/down or if both options for side view failed
                this.currentSpriteSetToUse = (this.spriteSetOriginal != null) ? this.spriteSetOriginal : this.spriteSetMirrored;
            }
        }
        // If still null, no sprites are available.
        if (this.currentSpriteSetToUse == null && (this.spriteSetOriginal != null || this.spriteSetMirrored != null)) {
             //This case implies one of them should have been picked
             this.currentSpriteSetToUse = (this.spriteSetOriginal != null) ? this.spriteSetOriginal : this.spriteSetMirrored;
             // And pick a default animation row if the intended one caused issues
             if (this.currentSpriteSetToUse == this.spriteSetMirrored && semanticDirection == DIRECTION_RIGHT) actualSheetRow = SHEET_ROW_ANIM_SIDE;
             else if (this.currentSpriteSetToUse == this.spriteSetOriginal && semanticDirection == DIRECTION_LEFT) actualSheetRow = SHEET_ROW_ANIM_SIDE;
             // else actualSheetRow is already set
        }


        final int[] step = {0};
        Timer timer = new Timer(delayMs, null);

        // Final actualSheetRow to be used in lambda, needs to be effectively final
        final int animationRow = actualSheetRow;

        timer.addActionListener(e -> {
            BufferedImage[][] effectiveSpriteSet = getEffectiveSpriteSet(); // Gets currentSpriteSetToUse

            if (effectiveSpriteSet == null ||
                animationRow < 0 || animationRow >= effectiveSpriteSet.length ||
                effectiveSpriteSet[animationRow] == null ||
                effectiveSpriteSet[animationRow].length == 0) {
                // Invalid animation sequence, stop and finalize
                ((Timer) e.getSource()).stop();
                setLocation(targetX, targetY); // Move to final destination
                // Optionally set a default/error icon here
                if (onFinish != null) onFinish.run();
                return;
            }

            if (step[0] >= steps) {
                ((Timer) e.getSource()).stop();
                setLocation(targetX, targetY);
                BufferedImage stoppedSprite = resizeSprite(effectiveSpriteSet[animationRow][0]);
                setIcon(new ImageIcon(stoppedSprite));
                if (onFinish != null) onFinish.run();
                return;
            }

            int currentX = getX() + dx; // dx is calculated based on startX, targetX, steps
            int currentY = getY() + ((targetY - startY) / steps); // Recalculate dy for precision
            setLocation(currentX, currentY);

            spriteIndex = (spriteIndex + 1) % effectiveSpriteSet[animationRow].length;
            BufferedImage nextSprite = resizeSprite(effectiveSpriteSet[animationRow][spriteIndex]);
            setIcon(new ImageIcon(nextSprite));

            step[0]++;
        });
        timer.start();
    }

    private BufferedImage resizeSprite(BufferedImage original) {
        if (original == null) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); // Transparent placeholder
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

    public void moveAndWait(int targetX, int targetY, int semanticDirection, int steps, int delayMs) {
        final Object lock = new Object();
        Runnable onFinishLocal = () -> {
            synchronized (lock) { lock.notify(); }
        };
        synchronized (lock) {
            moveAnimated(targetX, targetY, semanticDirection, steps, delayMs, onFinishLocal);
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
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
}