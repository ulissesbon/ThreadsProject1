import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SimulationScreen extends JFrame {

    public static Semaphore Display;
    public static Semaphore Mutex;
    public static Semaphore IsWatching;
    public static Semaphore EnterRoom;

    private JLayeredPane layeredPane;
    private BufferedImage backgroundImage;
    private BufferedImage[][] maleSprites;
    private BufferedImage[][] femaleSprites;

    public SimulationScreen(int capacity, float movieTime) {
        setTitle("Simulação do Cinema");

        setSize(1280, 960); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Display = new Semaphore(0);
        Mutex = new Semaphore(1);
        IsWatching = new Semaphore(0, true);
        EnterRoom = new Semaphore(capacity, true);

        try {
            backgroundImage = ImageIO.read(new File("background_betav1.png"));
            BufferedImage maleSpriteSheet = ImageIO.read(new File("Male1.png"));
            BufferedImage femaleSpriteSheet = ImageIO.read(new File("Female1.png"));

            int rows = 12, cols = 8;
            int spriteWidth = maleSpriteSheet.getWidth() / cols;
            int spriteHeight = maleSpriteSheet.getHeight() / rows;

            maleSprites = new BufferedImage[rows][cols];
            for (int y = 0; y < rows; y++)
                for (int x = 0; x < cols; x++)
                    maleSprites[y][x] = maleSpriteSheet.getSubimage(x * spriteWidth, y * spriteHeight, spriteWidth, spriteHeight);

            femaleSprites = new BufferedImage[rows][cols];
            for (int y = 0; y < rows; y++)
                for (int x = 0; x < cols; x++)
                    femaleSprites[y][x] = femaleSpriteSheet.getSubimage(x * spriteWidth, y * spriteHeight, spriteWidth, spriteHeight);



        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar imagem de fundo.");
            return;
        }

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int imgW = backgroundImage.getWidth();
                int imgH = backgroundImage.getHeight();

                int centerX = (getWidth() - imgW) / 2;
                int centerY = (getHeight() - imgH) / 2;

                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                g.drawImage(backgroundImage, centerX, centerY, this);
            }
        };

        mainPanel.setLayout(null); // para posicionamento absoluto no layeredPane
        add(mainPanel, BorderLayout.CENTER);

        layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);
        layeredPane.setBounds(0, 0, backgroundImage.getWidth(), backgroundImage.getHeight());

        layeredPane.setLocation(
            (getWidth() - backgroundImage.getWidth()) / 2,
            (getHeight() - backgroundImage.getHeight()) / 2
        );

        mainPanel.add(layeredPane);
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(Color.DARK_GRAY);

        JLabel tempoLancheLabel = new JLabel("Tempo de lanche:");
        tempoLancheLabel.setForeground(Color.WHITE);
        JTextField tempoLancheField = new JTextField(5);
        JButton adicionarFanButton = new JButton("Adicionar Fã");

        adicionarFanButton.addActionListener((ActionEvent e) -> {
            try {
                int tempoLanche = Integer.parseInt(tempoLancheField.getText());
                Fan fan = new Fan(tempoLanche); // completar com seus parâmetros
                fan.start();

                BufferedImage[][] spriteSet = Math.random() < 0.5 ? maleSprites : femaleSprites;

                int startX = 800;
                int startY = 550;

                VisualFan visualFan = new VisualFan(spriteSet, startX, startY);
                layeredPane.add(visualFan, JLayeredPane.PALETTE_LAYER);
                layeredPane.repaint();

                new Thread(() -> {
                    visualFan.moveAnimated(600, 550, 2, 20, 30);  // até a fila (para esquerda)
                    visualFan.moveAnimated(200, 400, 2, 30, 30);  // até a sala
                    try {
                        Thread.sleep((long)(movieTime * 1000)); // simula tempo do filme
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    visualFan.moveAnimated(200, 200, 0, 30, 30);  // saída da sala (para cima)
                    visualFan.moveAnimated(750, 150, 1, 40, 30);  // até lanchonete
                    try {
                        Thread.sleep(tempoLanche * 1000L);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    visualFan.moveAnimated(600, 550, 3, 30, 30);  // volta para a fila
                }).start();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tempo de lanche inválido.");
            }
        });

        controlPanel.add(tempoLancheLabel);
        controlPanel.add(tempoLancheField);
        controlPanel.add(adicionarFanButton);

        add(controlPanel, BorderLayout.SOUTH);
        Demonstrator demonstrator = new Demonstrator(capacity, movieTime);
        demonstrator.start();
    }
}
