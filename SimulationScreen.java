import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Semaphore;
import javax.swing.*;

public class SimulationScreen extends JFrame {

    public static Semaphore Mutex;
    public static Semaphore Display;
    public static Semaphore EnterRoom;
    public static Semaphore IsWatching;

    public enum FanStatus { WAITING, WATCHING, EATING }

    private static int fanCounter = 1;  // contagem de fãs começa por 1

    private JLayeredPane layeredPane;

    public SimulationScreen(int capacity, float movieTime) {
        setTitle("Simulação do Cinema");
        setSize( 1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        Display = new Semaphore(0);
        Mutex = new Semaphore(1);
        IsWatching = new Semaphore(0);
        EnterRoom = new Semaphore(capacity, true);

        // Imagem de fundo
        layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 1024, 768);

        JLabel background = new JLabel(new ImageIcon("background_betav1.png"));
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
                int id = fanCounter++;
                String label = "F" + id;
                Color cor = switch (id % 4) {
                    case 0 -> Color.RED;
                    case 1 -> Color.GREEN;
                    case 2 -> Color.BLUE;
                    default -> Color.YELLOW;
                };

                VisualFan visual = new VisualFan(label, cor);
                layeredPane.add(visual, JLayeredPane.PALETTE_LAYER);
                layeredPane.revalidate();
                layeredPane.repaint();

                VisualFanThreaded fanThread = new VisualFanThreaded(id, tempoLanche, movieTime, visual, layeredPane);
                fanThread.start();

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
