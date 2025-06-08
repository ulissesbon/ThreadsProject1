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
        visualFan.showStatusIcon("eatingIcon.png");
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
                ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Lanchando: " + remainingTime + "s restantes");
                lastPrintedSecond = currentSecond;
            }
        }
        visualFan.removeStatusIcon();
    }
    
    public void whatching(){
        // função para deixar o fã lanchando pelo tempo determinado
        visualFan.showStatusIcon("watchingIcon.png");
        LocalTime initial = LocalTime.now();
        int lastPrintedSecond = -1;
        while (true) { 
            LocalTime now = LocalTime.now();
            Duration duration = Duration.between(initial, now);
            float length = duration.toMillis() / 1000f;

            if (Demonstrator.Display.availablePermits() > 0) {
                break;
            }
        }
        visualFan.removeStatusIcon();
    }

    public void run() {
        while (true) {
            try {
                visualFan.moveAnimated(500, 515, 0, 100, 100, null);
                ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Tentando entrar na sala...");
                
                status = FanStatus.WAITING;

                seatIndex = ExibitionScreen.seatManager.assignSeat();
                Point assento = ExibitionScreen.seatManager.getSeatPosition(seatIndex);

                down();
                Demonstrator.EnterRoom.acquire();
                visualFan.moveToAndWait(assento.x, assento.y, 5, 100);
                if (Demonstrator.EnterRoom.availablePermits() < 1) {
                    ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Último a entrar. Iniciando filme.");
                    ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Sentou no assento " + (seatIndex + 1));
                    Demonstrator.Display.release();
                }
                up();
                // visualFan.moveAndWait(assento.x, assento.y, 0, 5, 100);
                // visualFan.moveTo(assento.x, assento.y, 5, 100);

                status = FanStatus.WATCHING;
                whatching();
//              === FILME FINALIZADO ===
//                     vai lanchar
                ExibitionScreen.seatManager.releaseSeat(seatIndex);
                seatIndex = -1;
                visualFan.moveToAndWait(415, 215, 5, 100);
                
                status = FanStatus.EATING;
                eating();

                ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Retornou à fila.");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}