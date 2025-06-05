import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ExibitionScreen extends JFrame {

    
    public static Semaphore Mutex;
    public static Semaphore IsWatching;
    public static Semaphore Line;
    public static Semaphore FreeRoom;

    public static ExibitionScreen exibitionScreenInstance;

    private JTextArea logArea;
    private JScrollPane logScrollPane;
    private JLayeredPane layeredPane;
    private BufferedImage backgroundImage;
    private BufferedImage[][] maleSpritesOriginal;
    private BufferedImage[][] maleSpritesMirrored;
    private BufferedImage[][] femaleSpritesOriginal;
    private BufferedImage[][] femaleSpritesMirrored;

    private VisualFan visualFan;

    public static SeatManager seatManager;

    private int fanCount = 0;

    public ExibitionScreen(int capacity, int movieTime) {
        exibitionScreenInstance = this;

        setTitle("EXIBIÇÃO");

        setSize(1280, 960);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Line = new Semaphore(0, true);
        
        Mutex = new Semaphore(1, true);
        IsWatching = new Semaphore(0, true);
        FreeRoom = new Semaphore(0, true);
        

        seatManager = new SeatManager(ASSENTOS);

        try {
            backgroundImage = ImageIO.read(new File("background[1.0].png")); // Assuming the background with cantina is the one to use

            int rows = 12, cols = 8;
            maleSpritesOriginal = VisualFan.loadSpriteSheet("zMale1.png", rows, cols);
            maleSpritesMirrored = VisualFan.loadSpriteSheet("zMaleMirrored.png", rows, cols);
            femaleSpritesOriginal = VisualFan.loadSpriteSheet("zFemale1.png", rows, cols);
            femaleSpritesMirrored = VisualFan.loadSpriteSheet("zFemaleMirrored.png", rows, cols);


        } catch (Exception e) {
            e.printStackTrace(); // Print error for debugging
            JOptionPane.showMessageDialog(this, "Erro ao carregar imagem de fundo ou sprites.");
            return;
        }

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    int imgW = backgroundImage.getWidth();
                    int imgH = backgroundImage.getHeight();
                    int centerX = (getWidth() - imgW) / 2;
                    int centerY = (getHeight() - imgH) / 2;
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.drawImage(backgroundImage, centerX, centerY, this);

                    // debug visual
                    // g.setColor(Color.RED);
                    // for (int x = 0; x <= imgW; x += 25) {
                    //     for (int y = 0; y <= imgH; y += 25) {
                    //         int px = centerX + x;
                    //         int py = centerY + y;
                    //         g.fillOval(px - 2, py - 2, 5, 5);
                    //     }
                    // }
                }
            }
        };

        mainPanel.setLayout(null);
        add(mainPanel, BorderLayout.CENTER);

        if (backgroundImage != null) {
            layeredPane = new JLayeredPane();
            layeredPane.setOpaque(false);
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int bgImgW = backgroundImage.getWidth();
            int bgImgH = backgroundImage.getHeight();
            int offsetX = (panelWidth - bgImgW) / 2;
            int offsetY = (panelHeight - bgImgH) / 2;

            layeredPane.setBounds(offsetX, offsetY, bgImgW, bgImgH);
            mainPanel.add(layeredPane);
        }

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setBounds(20, 0, 475, 115); // Posição (x, y) e tamanho (width, height)

        // Adiciona ao layeredPane (ou diretamente ao mainPanel se não usar layeredPane)
        layeredPane.add(logScrollPane, JLayeredPane.PALETTE_LAYER); 

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(Color.DARK_GRAY);

        JLabel tempoLancheLabel = new JLabel("Tempo de lanche:");
        tempoLancheLabel.setForeground(Color.WHITE);
        JTextField tempoLancheField = new JTextField("5", 5);
        JButton adicionarFanButton = new JButton("Adicionar Fã");
        tempoLancheField.addActionListener(e -> adicionarFanButton.doClick());


        Demonstrator demonstrator = new Demonstrator(capacity, movieTime);
        demonstrator.start();

        adicionarFanButton.addActionListener((ActionEvent e) -> {
            try {
                if (fanCount >= 10) {
                    JOptionPane.showMessageDialog(this, "Limite de 10 fãs atingido.");
                    return;
                }

                int tempoLanche;
                try {
                    tempoLanche = Integer.parseInt(tempoLancheField.getText());
                    if (tempoLanche <= 0) {
                        JOptionPane.showMessageDialog(this, "Tempo de lanche deve ser positivo.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Tempo de lanche inválido.");
                    return;
                }

                fanCount++;

                BufferedImage[][] originalSprites;
                BufferedImage[][] mirroredSprites;
                if (Math.random() < 0.5) {
                    originalSprites = maleSpritesOriginal;
                    mirroredSprites = maleSpritesMirrored;
                } else {
                    originalSprites = femaleSpritesOriginal;
                    mirroredSprites = femaleSpritesMirrored;
                }

                visualFan = new VisualFan(maleSpritesOriginal, maleSpritesMirrored, 1000, 515,2.5);
                
                Fan fan = new Fan(tempoLanche, visualFan);
                if (layeredPane != null) {
                    layeredPane.add(visualFan, JLayeredPane.PALETTE_LAYER);
                    layeredPane.repaint();
                }
                
                fan.start();

                adicionarFanButton.setEnabled(false);
                Timer delayTimer = new Timer(500, ev -> adicionarFanButton.setEnabled(true));
                delayTimer.setRepeats(false);
                delayTimer.start();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao adicionar fã: " + ex.getMessage());
            }
        });

        controlPanel.add(tempoLancheLabel);
        controlPanel.add(tempoLancheField);
        controlPanel.add(adicionarFanButton);

        add(controlPanel, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                if (layeredPane != null && backgroundImage != null) {
                    int panelWidth = mainPanel.getWidth();
                    int panelHeight = mainPanel.getHeight();
                    int bgImgW = backgroundImage.getWidth();
                    int bgImgH = backgroundImage.getHeight();
                    int offsetX = (panelWidth - bgImgW) / 2;
                    int offsetY = (panelHeight - bgImgH) / 2;
                    layeredPane.setBounds(offsetX, offsetY, bgImgW, bgImgH);
                }
            }
        });
    }

    public static final Point[] ASSENTOS = {
        new Point(95, 435),  new Point(161, 435), new Point(227, 435),
        new Point(296, 435), new Point(363, 435),
        new Point(95, 290),  new Point(161, 290), new Point(227, 290),
        new Point(296, 290), new Point(363, 290)
        
    };

    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto-scroll para o final
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

}