import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ConfigScreen extends JFrame {

    private JTextField movieLenghtField;
    private JTextField theatherCapacityField;

    public ConfigScreen() {
        setTitle("Configuração da Simulação");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela

        // === Criação dos componentes ===
        JLabel movieLenghtLabel = new JLabel("Tempo do Filme (segundos):");
        JLabel theatherCapacityLabel = new JLabel("Capacidade da Sala:");

        movieLenghtField = new JTextField(10);
        theatherCapacityField = new JTextField(10);

        JButton iniciarButton = new JButton("Iniciar Simulação");

        iniciarButton.addActionListener((ActionEvent e) -> {
            try {
                int movieLenght = Integer.parseInt(movieLenghtField.getText());
                int theatherCapacity = Integer.parseInt(theatherCapacityField.getText());

                if (movieLenght <= 0 || theatherCapacity <= 0) {
                    JOptionPane.showMessageDialog(this, "Os valores devem ser maiores que zero.");
                    return;
                }

                // Abre a tela de simulação (iremos criar depois)
                SimulationScreen sim = new SimulationScreen(theatherCapacity, movieLenght);
                sim.setVisible(true);
                dispose(); // Fecha a tela de configuração

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Insira valores numéricos válidos.");
            }
        });

        // === Monta o painel ===
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(movieLenghtLabel);
        panel.add(movieLenghtField);
        panel.add(theatherCapacityLabel);
        panel.add(theatherCapacityField);
        panel.add(new JLabel()); // Espaço vazio
        panel.add(iniciarButton);

        add(panel);
    }
}
