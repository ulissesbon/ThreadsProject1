import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ExibitionScreen extends JFrame {

    public static Semaphore Display;
    public static Semaphore Mutex;
    public static Semaphore IsWatching;
    public static Semaphore EnterRoom;
    public static Semaphore Line;

    private JLayeredPane layeredPane;
    private BufferedImage backgroundImage;
    private BufferedImage[][] maleSprites;
    private BufferedImage[][] femaleSprites;
    private BufferedImage[][] maleSpritesMirrored;
    private BufferedImage[][] femaleSpritesMirrored;

    public static SeatManager seatManager;

    private int fanCount = 0;

    public ExibitionScreen(int capacity, float movieTime) {
        setTitle("EXIBIÇÃO");

        setSize(1280, 960); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Line = new Semaphore(0, true);
        Display = new Semaphore(0);
        Mutex = new Semaphore(1);
        IsWatching = new Semaphore(0, true);
        EnterRoom = new Semaphore(capacity, true);
        
        seatManager = new SeatManager(ASSENTOS);

        try {
            backgroundImage = ImageIO.read(new File("background[1.0].png"));
            BufferedImage maleSpriteSheet = ImageIO.read(new File("zMale1.png"));
            BufferedImage femaleSpriteSheet = ImageIO.read(new File("zFemale1.png"));
            BufferedImage maleSpriteSheetMirrored = ImageIO.read(new File("zMaleMirrored.png"));
            BufferedImage femaleSpriteSheetMirrored = ImageIO.read(new File("zFemaleMirrored.png"));

            int rows = 12, cols = 8;
            int spriteWidth = (maleSpriteSheet.getWidth() / cols);
            int spriteHeight = (maleSpriteSheet.getHeight() / rows) - 5;

            maleSprites = new BufferedImage[rows][cols];
            for (int y = 0; y < rows; y++)
                for (int x = 0; x < cols; x++)
                    maleSprites[y][x] = maleSpriteSheet.getSubimage(x , y , spriteWidth, spriteHeight);

            femaleSprites = new BufferedImage[rows][cols];
            for (int y = 0; y < rows; y++)
                for (int x = 0; x < cols; x++)
                    femaleSprites[y][x] = femaleSpriteSheet.getSubimage(x, y, spriteWidth, spriteHeight);

            maleSpritesMirrored = new BufferedImage[rows][cols];
            for (int y = 0; y < rows; y++)
                for (int x = 0; x < cols; x++)
                    maleSpritesMirrored[y][x] = maleSpriteSheetMirrored.getSubimage(x , y , spriteWidth, spriteHeight);

            femaleSpritesMirrored = new BufferedImage[rows][cols];
            for (int y = 0; y < rows; y++)
                for (int x = 0; x < cols; x++)
                    femaleSpritesMirrored[y][x] = femaleSpriteSheetMirrored.getSubimage(x, y, spriteWidth, spriteHeight);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar imagem de fundo.");
            return;
        }

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel() {   // centraliza a imagem e deixa o fundo preto
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
        tempoLancheField.addActionListener(e -> adicionarFanButton.doClick());

        // demonstrador é criado antes dos fãs
        Demonstrator demonstrator = new Demonstrator(capacity, movieTime);
        demonstrator.start();

        adicionarFanButton.addActionListener((ActionEvent e) -> {
            try {
                if (fanCount >= 10) {
                    JOptionPane.showMessageDialog(this, "Limite de 10 fãs atingido.");
                    return;
                }
                fanCount++;

                int tempoLanche = Integer.parseInt(tempoLancheField.getText());

                Line.release();

                BufferedImage[][] spriteSet = Math.random() < 0.5 ? maleSprites : femaleSprites;
                VisualFan visualFan = new VisualFan(spriteSet, 980, 500, 2.5);
                layeredPane.add(visualFan, JLayeredPane.PALETTE_LAYER);
                layeredPane.repaint();

                // Criar e iniciar thread Fan
                Fan fan = new Fan(tempoLanche, visualFan);
                fan.start();
                
                visualFan.moveAnimated( (510 + fanCount * 35), 500, 2, 20, 40, null);

                adicionarFanButton.setEnabled(false);   // bloqueio de 1 segundo para criação de novos fãs
                Timer delayTimer = new Timer(1000, ev -> adicionarFanButton.setEnabled(true));
                delayTimer.setRepeats(false);
                delayTimer.start();

                // SENTAR NOS ASSENTOS
                // IR COMER E VOLTAR PRA FILA
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tempo de lanche inválido.");
            }
        });

        controlPanel.add(tempoLancheLabel);
        controlPanel.add(tempoLancheField);
        controlPanel.add(adicionarFanButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    public static final Point[] ASSENTOS = {
        // assentos de baixo
        new Point(112, 207), new Point(178, 207), new Point(244, 207),
        new Point(310, 207), new Point(376, 207),
        // assentos de cima
        new Point(112, 299), new Point(178, 299), new Point(244, 299),
        new Point(310, 299), new Point(376, 299)
    };

}
