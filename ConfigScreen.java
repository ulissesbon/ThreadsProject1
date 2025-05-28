import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class ConfigScreen extends JFrame {

    private JTextField tempoFilmeField;
    private JTextField capacidadeField;

    public ConfigScreen() {
        setTitle("Configuração da Simulação");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela

        // === Criação dos componentes ===
        JLabel tempoFilmeLabel = new JLabel("Tempo do Filme:");
        JLabel capacidadeLabel = new JLabel("Capacidade da Sala:");

        tempoFilmeField = new JTextField(10);
        capacidadeField = new JTextField(10);

        // Adiciona placeholders
        addPlaceholder(tempoFilmeField, "Ex: 5 (segundos)");
        addPlaceholder(capacidadeField, "Máximo: 10");

        JButton iniciarButton = new JButton("Iniciar Simulação");

        iniciarButton.addActionListener((ActionEvent e) -> {
            try {
                String textoTempo = tempoFilmeField.getText();
                String textoCapacidade = capacidadeField.getText();

                if (textoTempo.equals("Ex: 10 segundos") || textoCapacidade.equals("Ex: 5 lugares")) {
                    JOptionPane.showMessageDialog(this, "Preencha todos os campos corretamente.");
                    return;
                }

                int tempoFilme = Integer.parseInt(textoTempo);
                int capacidade = Integer.parseInt(textoCapacidade);

                if (tempoFilme <= 0 || capacidade <= 0) {
                    JOptionPane.showMessageDialog(this, "Os valores devem ser maiores que zero.");
                    return;
                }
                if (capacidade > 10) {
                    JOptionPane.showMessageDialog(this, "Além da capacidade permitida!");
                    return;
                }

                // Abre a tela de simulação
                SimulationScreen sim = new SimulationScreen(capacidade, tempoFilme);
                sim.setVisible(true);
                dispose(); // Fecha a tela de configuração

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Insira valores numéricos válidos.");
            }
        });

        // === Monta o painel ===
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(tempoFilmeLabel);
        panel.add(tempoFilmeField);
        panel.add(capacidadeLabel);
        panel.add(capacidadeField);
        panel.add(new JLabel()); // Espaço vazio
        panel.add(iniciarButton);

        add(panel);

        getRootPane().setDefaultButton(iniciarButton);
    }

    // Método utilitário para simular placeholder em JTextField
    private void addPlaceholder(JTextField field, String placeholder) {
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }
}
