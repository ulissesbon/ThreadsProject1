import java.awt.*;
import javax.swing.*;

public class VisualFanThreaded extends Thread {

    private int id;
    private int eatingTime;
    private float movieLength;
    private VisualFan visual;
    private JLayeredPane layeredPane;

    public enum FanStatus { WAITING, WATCHING, EATING }
    private FanStatus status;

    public VisualFanThreaded(int id, int eatingTime, float movieLength, VisualFan visual, JLayeredPane layeredPane) {
        this.id = id;
        this.eatingTime = eatingTime;
        this.movieLength = movieLength;
        this.visual = visual;
        this.layeredPane = layeredPane;
    }

    private void log(String msg) {
        System.out.println("[FAN #" + id + "] " + msg);
    }

    @Override
    public void run() {
        while (true) {
            try {
                log("tentando entrar no auditório...");
                SimulationScreen.EnterRoom.acquire();

                // Movimento até a fila
                visual.delayMovementTo(new Point(700, 480), 30, 15); // fila
                status = FanStatus.WAITING;
                log("entrou na fila. Status: " + status);

                // Movimento até entrada da sala
                visual.delayMovementTo(new Point(250, 330), 30, 15); // entrada
                log("entrou na sala.");

                synchronized (SimulationScreen.Mutex) {
                    if (SimulationScreen.EnterRoom.availablePermits() == 0) {
                        log("foi o último a entrar. Liberando o demonstrador.");
                        SimulationScreen.Display.release();
                    }
                }

                log("aguardando o início do filme...");
                SimulationScreen.IsWatching.acquire();

                status = FanStatus.WATCHING;
                log("assistindo ao filme. Status: " + status);
                Thread.sleep((long)(movieLength * 1000));

                // Saída
                visual.delayMovementTo(new Point(250, 170), 30, 15); // saída da sala
                visual.delayMovementTo(new Point(800, 100), 30, 15); // lanchonete

                status = FanStatus.EATING;
                log("lanchando. Status: " + status);
                for (int t = eatingTime; t > 0; t--) {
                    log("tempo restante de lanche: " + t + "s");
                    Thread.sleep(1000);
                }

                // Volta para a fila
                visual.delayMovementTo(new Point(700, 480), 30, 15);
                status = FanStatus.WAITING;
                log("voltou para a fila. Status: " + status);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
