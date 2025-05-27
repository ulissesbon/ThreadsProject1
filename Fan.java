public class Fan extends Thread {
    private int id;
    private int eatingTimer;
    private SimulationScreen.FanStatus status;

    public Fan(int id, int eatingTime) {
        this.id = id;
        this.eatingTimer = eatingTime;
    }

    public void run() {
        while (true) {
            status = SimulationScreen.FanStatus.WAITING;
            System.out.println("[FAN #" + id + "] status: " + status + " (tentando entrar no auditório)");

            try {
                SimulationScreen.EnterRoom.acquire();
                System.out.println("[FAN #" + id + "] entrou no auditório.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (SimulationScreen.Mutex) {
                if (SimulationScreen.EnterRoom.availablePermits() == 0) {
                    System.out.println("[FAN #" + id + "] foi o último a entrar. Liberando o demonstrador.");
                    SimulationScreen.Display.release();
                }
            }

            // Fã assiste "dormindo" até ser liberado
            status = SimulationScreen.FanStatus.WATCHING;
            System.out.println("[FAN #" + id + "] sentou na sala e está aguardando o término do filme...");
            try {
                SimulationScreen.IsWatching.acquire(); // será liberado pelo demonstrador
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("[FAN #" + id + "] o filme acabou, indo lanchar.");

            status = SimulationScreen.FanStatus.EATING;
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
