import java.awt.Point;
import java.time.Duration;
import java.time.LocalTime;

public class Fan extends Thread {
    private static int fanCounter = 1;
    private static final Object fanIdLock = new Object();

    private final int id;
    private final int eatingTimer;
    private VisualFan visualFan;

    public enum FanStatus { WAITING, WATCHING, EATING }

    private FanStatus status;
    private int seatIndex;

    public Fan(int eatingTime, VisualFan visualFan) {
        this.eatingTimer = eatingTime;
        this.visualFan = visualFan;
        synchronized (fanIdLock) {
            this.id = fanCounter++;
        }
    }

    public int getIdNum() {
        return this.id;
    }

    public void run() {
        while (true) {
            try {
                status = FanStatus.WAITING;
                System.out.println("[FAN #" + id + "] Tentando entrar na sala...");
                visualFan.moveAndWait(600, 550, 2, 25, 40); // até fila

                ExibitionScreen.EnterRoom.acquire();

                seatIndex = ExibitionScreen.seatManager.assignSeat();
                Point assento = ExibitionScreen.seatManager.getSeatPosition(seatIndex);

                visualFan.moveAndWait(assento.x, assento.y, 2, 30, 40); // até o assento
                System.out.println("[FAN #" + id + "] Sentou no assento " + (seatIndex + 1));

                synchronized (ExibitionScreen.Mutex) {
                    if (ExibitionScreen.EnterRoom.availablePermits() == 0) {
                        ExibitionScreen.Display.release();
                        System.out.println("[FAN #" + id + "] Último da sala. Iniciando filme.");
                    }
                }

                status = FanStatus.WATCHING;
                ExibitionScreen.IsWatching.acquire(); // bloqueia até o filme acabar

                // Após filme, libera o assento e vai lanchar
                ExibitionScreen.seatManager.releaseSeat(seatIndex);
                seatIndex = -1;

                visualFan.moveAndWait(200, 200, 0, 30, 40); // sai da sala
                visualFan.moveAndWait(750, 150, 1, 40, 40); // até lanche

                status = FanStatus.EATING;

                LocalTime initial = LocalTime.now();
                int lastPrintedSecond = -1;
                while (true) { 
                    LocalTime now = LocalTime.now();
                    Duration duration = Duration.between(initial, now);
                    float length = duration.toMillis() / 1000f;

                    if (length >= eatingTimer) {
                        break;
                    }
                    int currentSecond = (int) length;
                    if (currentSecond != lastPrintedSecond) {
                        int remainingTime = eatingTimer - currentSecond;
                        System.out.println("[FAN #" + id + "] Lanchando: " + remainingTime + "s restantes");
                        lastPrintedSecond = currentSecond;
                    }
                }

                visualFan.moveAndWait(600, 550, 3, 30, 40); // volta à fila
                System.out.println("[FAN #" + id + "] Retornou à fila.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}