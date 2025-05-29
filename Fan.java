public class Fan extends Thread {
    private int id;
    private int eatingTimer;
    private FanStatus status;
    private static int fanCounter = 1;
    private static final Object fanId = new Object();
    
    public enum FanStatus {
        WAITING, WATCHING, EATING
    }

    public Fan(int eatingTime){
        this.eatingTimer = eatingTime;
        synchronized (fanId) {
            this.id = fanCounter++;
        }
    }

    public int getFanId(){
        return  this.id;
    }
    
    public void run() {
        while (true) {
            status = FanStatus.WAITING;
            System.out.println("[FAN #" + id + "] status: " + status + " (tentando entrar no auditório)");

            try {
                ExibitionScreen.EnterRoom.acquire();
                System.out.println("[FAN #" + id + "] entrou no auditório.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (ExibitionScreen.Mutex) {
                if (ExibitionScreen.EnterRoom.availablePermits() == 0) {
                    System.out.println("[FAN #" + id + "] foi o último a entrar. Liberando o demonstrador.");
                    ExibitionScreen.Display.release();
                }
            }

            // Fã assiste "dormindo" até ser liberado
            status = FanStatus.WATCHING;
            System.out.println("[FAN #" + id + "] sentou na sala e está aguardando o término do filme...");
            try {
                ExibitionScreen.IsWatching.acquire(); // será liberado pelo demonstrador
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("[FAN #" + id + "] o filme acabou, indo lanchar.");

            status = FanStatus.EATING;
            for (int t = eatingTimer; t > 0; t--) {
                System.out.println("[FAN #" + id + "] tempo restante de lanche: " + t + "s");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("[FAN #" + id + "] terminou de lanchar, voltando para a fila.");
        }
    }
}
