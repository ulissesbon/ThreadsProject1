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
            visualFan.showStatusIcon("eatingIcon.png");
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

        // Enquanto o filme ainda não começou, o fã pode dormir
        while (!ExibitionScreen.isFilmRunning.get()) {
            synchronized (ExibitionScreen.isFilmRunning) {
                //ExibitionScreen.isFilmRunning.wait();
                Thread.onSpinWait(); // FAN dorme até filme começar
            }
        }

        ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] O filme começou!");
        System.out.println("[FAN #" + id + "] O filme começou!");
    }

    public void run() {
        down();
        int fila = ExibitionScreen.Line.availablePermits();
        up();
        if (id >= Demonstrator.capacidade){
            visualFan.moveToAndWait(500 + ((fila) * 30), 515, 5, 50);
        } else{
            visualFan.moveToAndWait(500, 515, 5, 50);
        }
        while (true) {
            try {
                ExibitionScreen.Line.release();
                ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Tentando entrar na sala...");
                System.out.println("[FAN #" + id + "] Tentando entrar na sala...");
                
                status = FanStatus.WAITING;

                Demonstrator.EnterRoom.acquire();
                ExibitionScreen.Line.acquire();
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

                status = FanStatus.WATCHING;
                whatching();

//              === FILME FINALIZADO ===

                ExibitionScreen.seatManager.releaseSeat(seatIndex);
                seatIndex = -1;
                visualFan.moveToAndWait(415, 215, 5, 100);
                
                visualFan.moveToAndWait(555 + (id * 30), 215, 5, 100);
                status = FanStatus.EATING;
                eating();

                visualFan.moveToAndWait(550, 350, 5, 100);

                down();
                fila = ExibitionScreen.Line.availablePermits();
                up();
                visualFan.moveToAndWait(500 + (fila * 35), 515, 5, 50);
                ExibitionScreen.exibitionScreenInstance.addLog("[FAN #" + id + "] Retornou à fila.");
                System.out.println("[FAN #" + id + "] Retornou à fila.");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}