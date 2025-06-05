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
    
    public void waitToGetThere(int x, int y){
        while(true){ 
            if(this.visualFan.getX() == x && this.visualFan.getY() == y){
                break;
            }
            try {
                Thread.sleep(500); // para não travar a CPU
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        while (true) {
            try {
                ExibitionScreen.Line.release();
                visualFan.moveAnimated(500, 515, 0, 100, 100, null);
                // waitToGetThere(500, 515);
                System.out.println("[FAN #" + id + "] Tentando entrar na sala...");
                
                status = FanStatus.WAITING;

                seatIndex = ExibitionScreen.seatManager.assignSeat();
                Point assento = ExibitionScreen.seatManager.getSeatPosition(seatIndex);

                down();
                // visualFan.moveTo(300, 515, 5, 100);
                // waitToGetThere(300, 515);
                // visualFan.moveAndWait(assento.x, assento.y, 0, 5, 100);
                visualFan.moveToAndWait(assento.x, assento.y, 5, 100);
                // visualFan.moveTo(assento.x, assento.y, 5, 100);
                // waitToGetThere(assento.x, assento.y);
                ExibitionScreen.Line.acquire();

                System.out.println("[FAN #" + id + "] Sentou no assento " + (seatIndex + 1));
                Demonstrator.EnterRoom.acquire();

                if (Demonstrator.EnterRoom.availablePermits() == 0) {
                    System.out.println("[FAN #" + id + "] Último a entrar. Iniciando filme.");
                    Demonstrator.Display.release();
                }
                up();

                status = FanStatus.WATCHING;
                ExibitionScreen.IsWatching.acquire(); // bloqueia até o filme acabar

//              === FILME FINALIZADO ===
//                     vai lanchar
                ExibitionScreen.seatManager.releaseSeat(seatIndex);
                seatIndex = -1;
                visualFan.moveToAndWait(415, 215, 5, 100);
                
                ExibitionScreen.FreeRoom.release();

                status = FanStatus.EATING;
                eating();

                System.out.println("[FAN #" + id + "] Retornou à fila.");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}