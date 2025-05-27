import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Semaphore;
import javax.swing.*;

public class SimulationScreen extends JFrame {

    public static Semaphore Display;
    public static Semaphore Mutex;
    public static Semaphore IsWatching;
    public static Semaphore EnterRoom;

    public enum FanStatus { WAITING, WATCHING, EATING }

    private static int fanCounter = 0;

    private JLayeredPane layeredPane;

    public SimulationScreen(int capacity, float movieTime) {
        setTitle("Simulação do Cinema");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Inicializa semáforos com base no código original
        Display = new Semaphore(0);
        Mutex = new Semaphore(1);
        IsWatching = new Semaphore(0);
        EnterRoom = new Semaphore(capacity, true);

        // Imagem de fundo
        JLabel background = new JLabel(new ImageIcon("Gemini_Generated_Image_6op4f56op4f56op4 - Copia.png"));
        background.setBounds(0, 0, 1000, 562);
        layeredPane.add(background, JLayeredPane.DEFAULT_LAYER);

        add(layeredPane);

        // Painel inferior de controle
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBounds(0, 562, 1000, 50);
        controlPanel.setBackground(Color.LIGHT_GRAY);

        layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 1000, 562);


        JLabel tempoLancheLabel = new JLabel("Tempo de lanche:");
        JTextField tempoLancheField = new JTextField(5);
        JButton adicionarFanButton = new JButton("Adicionar Fã");

        adicionarFanButton.addActionListener((ActionEvent e) -> {
            try {
                int tempoLanche = Integer.parseInt(tempoLancheField.getText());

                // Cria visual
                String label = "F" + fanCounter;
                Color cor = switch (fanCounter % 4) {
                    case 0 -> Color.RED;
                    case 1 -> Color.GREEN;
                    case 2 -> Color.BLUE;
                    default -> Color.YELLOW;
                };

                VisualFan visual = new VisualFan(label, cor);
                layeredPane.add(visual, JLayeredPane.PALETTE_LAYER);
                layeredPane.repaint();

                // Anima entrada até a fila
                new Thread(() -> {
                    visual.moveTo(new Point(700, 480), 30, 15); // ponto da fila
                    visual.moveTo(new Point(250, 330), 30, 15); // entrada da sala
                    try {
                        Thread.sleep(3000); // simula tempo do filme
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    visual.moveTo(new Point(250, 170), 30, 15); // saída da sala
                    visual.moveTo(new Point(800, 100), 30, 15); // lanchonete
                    try {
                        Thread.sleep(tempoLanche * 1000L);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    visual.moveTo(new Point(700, 480), 30, 15); // volta para a fila
                }).start();

                // Cria a thread lógica
                Fan fan = new Fan(fanCounter++, tempoLanche);
                fan.start();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tempo de lanche inválido.");
            }
        });

        controlPanel.add(tempoLancheLabel);
        controlPanel.add(tempoLancheField);
        controlPanel.add(adicionarFanButton);
        add(controlPanel);

        // Inicia o demonstrador com base nos parâmetros passados da tela 1
        Demonstrator demonstrator = new Demonstrator(capacity, movieTime);
        demonstrator.start();
    }
}
