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
                System.out.println("[FAN #" + id + "] Lanchando: " + remainingTime + "s restantes");
                lastPrintedSecond = currentSecond;
            }
        }
        visualFan.removeStatusIcon();
    }
    
    public void whatching(){
        long lastUpdate = System.nanoTime();
        final long frameDuration = 100_000_000; // 100ms = 10 fps
        
        ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Assistindo ao filme...");
        System.out.println("[FAN #" + id + "] Assistindo ao filme...");

        while (ExibitionScreen.isFilmRunning.get()) {
            visualFan.showStatusIcon("watchingIcon.png");
            long now = System.nanoTime();

            if (now - lastUpdate >= frameDuration) {
                lastUpdate = now;

            }
        visualFan.removeStatusIcon();
        }
        ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Filme terminou.");
        System.out.println("[FAN #" + id + "] Filme terminou.");
    }

    public void waitingMovie() throws InterruptedException {
        ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Sentado. Aguardando início do filme...");
        System.out.println("[FAN #" + id + "] Sentado. Aguardando início do filme...");

        // fã dorme no aguardo do filme
        while (!ExibitionScreen.isFilmRunning.get()) {
            synchronized (ExibitionScreen.isFilmRunning) {
                Thread.onSpinWait();
            }
        }

        ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] O filme começou!");
        System.out.println("[FAN #" + id + "] O filme começou!");
    }

    public void run() {
        while (true) {
            try {
                ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Tentando entrar na sala...");
                System.out.println("[FAN #" + id + "] Tentando entrar na sala...");
                visualFan.moveToAndWait(500, 515, 5, 50);
                
                this.status = FanStatus.WAITING;

                Demonstrator.EnterRoom.acquire();
                seatIndex = ExibitionScreen.seatManager.assignSeat();
                Point assento = ExibitionScreen.seatManager.getSeatPosition(seatIndex);
                visualFan.moveToAndWait(assento.x, assento.y, 5, 0);

                down();
                if (Demonstrator.EnterRoom.availablePermits() == 0) {
                    ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Último a entrar. Iniciando filme.");
                    System.out.println("[FAN #" + id + "] Último a entrar. Iniciando filme.");
                    ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Sentou no assento " + (seatIndex + 1));
                    System.out.println("[FAN #" + id + "] Sentou no assento " + (seatIndex + 1));
                    ExibitionScreen.isFilmRunning.set(true);
                    
                    Demonstrator.Display.release();
                }
                up();

                waitingMovie();
                
                this.status = FanStatus.WATCHING;
                whatching();

//              === FILME FINALIZADO ===
                ExibitionScreen.seatManager.releaseSeat(seatIndex);
                seatIndex = -1;
                visualFan.moveToAndWait(415, 215, 5, 100);
                Demonstrator.EnterRoom.release();
                
                this.status = FanStatus.EATING;
                eating();

                ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Retornou à fila.");
                System.out.println("[FAN #" + id + "] Retornou à fila.");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}