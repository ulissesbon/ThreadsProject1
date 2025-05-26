import java.util.concurrent.Semaphore;

public class Teste {

    public static Semaphore Display = new Semaphore(0);
    public static Semaphore Mutex = new Semaphore(1);
    public static Semaphore IsWatching = new Semaphore(0);
    public static Semaphore EnterRoom = new Semaphore(5, true);  // capacidade do auditório

    private static int fanCounter = 0;
    private static final Object fanId = new Object();

    enum FanStatus { WAITING, WATCHING, EATING }

    static class Demonstrator extends Thread {

        private int capacity;
        private float movieLength;

        public Demonstrator(int capacity, float movieLength){
            this.capacity = capacity;
            this.movieLength = movieLength;
        }

        public void displayMovie() {
            System.out.println("[DEMONSTRADOR] Iniciando exibição do filme por " + movieLength + " segundos.");

            try {
                Thread.sleep((long)(movieLength * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("[DEMONSTRADOR] Filme finalizado. Acordando fãs para lanchar.");

            for (int i = 0; i < capacity; i++) {
                IsWatching.release();  // acorda todos os fãs que estão bloqueados
            }

            EnterRoom.release(capacity);  // libera a sala para o próximo grupo
        }

        public void run(){
            while (true) {
                try {
                    Display.acquire();
                    displayMovie();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    static class Fan extends Thread {
        private int id;
        private int eatingTimer;
        private FanStatus status;

        public Fan(int eatingTime){
            this.eatingTimer = eatingTime;
            synchronized (fanId) {
                this.id = fanCounter++;
            }
        }

        public void down(){
            try {
                Mutex.acquire();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void up(){
            Mutex.release();
        }

        public void watchMovie() {
            status = FanStatus.WATCHING;
            System.out.println("[FAN #" + id + "] sentou na sala e está aguardando o filme...");

            try {
                IsWatching.acquire();  // fã dorme aqui esperando o filme acabar
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("[FAN #" + id + "] o filme acabou, indo lanchar.");
        }

        public void eat(){
            status = FanStatus.EATING;
            System.out.println("[FAN #" + id + "] status: " + status + " (indo lanchar por " + eatingTimer + "s)");

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

        public void run(){
            while (true) {
                status = FanStatus.WAITING;
                System.out.println("[FAN #" + id + "] status: " + status + " (tentando entrar no auditório)");

                down();
                try {
                    EnterRoom.acquire();
                    System.out.println("[FAN #" + id + "] entrou no auditório.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                up();

                down();
                if (EnterRoom.availablePermits() == 0) {
                    Display.release();  // ultimo a entrar libera o demonstrador
                }
                up();

                try {
                    IsWatching.acquire();  // aguarda o demonstrador terminar o filme
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                watchMovie();
                eat();
            }
        }
    }

}
