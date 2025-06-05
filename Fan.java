import java.awt.Point;
import java.time.Duration;
import java.time.LocalTime;

public class Fan extends Thread {
    private static int fanCounter = 1;
    private static final Object fanIdLock = new Object();

    private final int id;
    private final int eatingTimer;

    public enum FanStatus { WAITING, WATCHING, EATING }
    public VisualFan visualFan;

    private FanStatus status;
    private int seatIndex;

    public Fan(int eatingTime, VisualFan visualFan) {
        this.eatingTimer = eatingTime;
        this.visualFan = visualFan;
        synchronized (fanIdLock) {
            this.id = fanCounter++;
        }
    }

    public Fan(int eatingTime) {
        this.eatingTimer = eatingTime;
        synchronized (fanIdLock) {
            this.id = fanCounter++;
        }
    }

    public int getIdNum() {
        return this.id;
    }
    
    public void down(){
        try {
            ExibitionScreen.Mutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void up(){
        ExibitionScreen.Mutex.release();
    }

    public void eating(){
        // função para deixar o fã lanchando pelo tempo determinado
        LocalTime initial = LocalTime.now();
        int lastPrintedSecond = -1;
        while (true) { 
            LocalTime now = LocalTime.now();
            Duration duration = Duration.between(initial, now);
            float length = duration.toMillis() / 1000f;

            if (length >= (float) this.eatingTimer) {
                break;
            }

            int currentSecond = (int) length;
            if (currentSecond != lastPrintedSecond) {
                int remainingTime = this.eatingTimer - currentSecond;
                System.out.println("[FAN #" + id + "] Lanchando: " + remainingTime + "s restantes");
                lastPrintedSecond = currentSecond;
            }
        }
    }

    public void run() {
        while (true) {
            try {
                ExibitionScreen.Line.release();
                visualFan.moveAnimated(500, 515, 0, 100, 100, null);
                status = FanStatus.WAITING;
                System.out.println("[FAN #" + id + "] Tentando entrar na sala...");
                
                down();
                Demonstrator.EnterRoom.acquire();
                // TODO: realizar a caminhada até o assento
                // while (true) { 
                //visualFan.moveAnimated(430, 515, 0, 100, 50, null);

                //visualFan.moveAndWait(95, 435, 1, 100, 50);
                //     if (visualFan.getX() == 400 && visualFan.getY() == 500)
                //         break;
                // }
                up();

                seatIndex = ExibitionScreen.seatManager.assignSeat();
                Point assento = ExibitionScreen.seatManager.getSeatPosition(seatIndex);

                System.out.println("[FAN #" + id + "] Sentou no assento " + (seatIndex + 1));

                down();
                if (Demonstrator.EnterRoom.availablePermits() == 0) {
                    System.out.println("[FAN #" + id + "] Último da sala. Iniciando filme.");
                    Demonstrator.Display.release();
                }
                up();

                status = FanStatus.WATCHING;
                ExibitionScreen.IsWatching.acquire(); // bloqueia até o filme acabar

//              === FILME FINALIZADO ===
//                     vai lanchar
                
                ExibitionScreen.seatManager.releaseSeat(seatIndex);
                seatIndex = -1;
                
                status = FanStatus.EATING;
                eating();

                System.out.println("[FAN #" + id + "] Retornou à fila.");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}