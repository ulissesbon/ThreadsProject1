import java.util.concurrent.Semaphore;

public class Threads {
    public static Semaphore Display = new Semaphore(0);
    public static Semaphore Mutex = new Semaphore(1);
    public static Semaphore IsWatching = new Semaphore(0);
    public static Semaphore EnterRoom = new Semaphore(5, true);  // semaforo para controlar o numero de pessoas dentro da sala para assistir

    private static int fanCounter = 0;
    private static final Object fanIdLock = new Object();

    static class Demonstrator extends Thread {

        private int capacity;
        public float movieLength;

        public Demonstrator(int capacity, float movieLength){
            this.capacity = capacity;
            this.movieLength = movieLength;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
        public int getCapacity() {
            return this.capacity;
        }

        public void setMovieLength(float movieLength) {
            this.movieLength = movieLength;
        }
        public float getMovieLength() {
            return this.movieLength;
        }

        public void displayMovie() {
            System.out.println("[DEMONSTRADOR] Exibindo o filme por " + movieLength + " segundos.");
            
            for (int i = 1; i <= movieLength; i++) {
                System.out.println("[DEMONSTRADOR] ... filme em exibição: " + i + "s");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("[DEMONSTRADOR] Filme finalizado.");

            // Libera todos os fãs que estão assistindo
            for (int i = 0; i < capacity; i++) {
                IsWatching.release();
            }
        }

        public void run(){
            while (true) {

                try {
                    Display.acquire();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                displayMovie();


            }
        }
    }


    static class Fan extends Thread {
        private int id;
        private int eatingTimer;

        public Fan(int eatingTime){
            this.eatingTimer = eatingTime;
            
            synchronized (fanIdLock) {
                this.id = fanCounter++;
            }
        }


        public void setEatingTimer(int eatingTime) {
            this.eatingTimer = eatingTime;
        }
        public int getEatingTimer() {
            return this.eatingTimer;
        }


        public void watchMovie() {
            System.out.println("[FAN #" + id + "] iniciou a sessão do filme.");
            // Simulação leve de CPU para fins didáticos:
            try {
                Thread.sleep(1000); // cada fã "assiste" por 1s antes de continuar
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void eat() {
 
            System.out.println("[FAN #" + id + "] foi lanchar por " + eatingTimer + " segundos.");
    
            for (int t = eatingTimer; t > 0; t--) {
                System.out.println("[FAN #" + id + "] lanchando... tempo restante: " + t + "s");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("[FAN #" + id + "] terminou de lanchar e está voltando para assistir o filme novamente.");
        }

        public void down(){
            try {
                    Mutex.acquire();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
        }
        public void up(){
            Mutex.release();
        }

        public void run(){

            while (true) {
                // -----------------------------------------------
                System.out.println("[FAN #" + id + "] tentando entrar no auditório...");

                down();

                try{
                    EnterRoom.acquire();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("[FAN #" + id + "] entrou no auditório. Aguardando início da sessão.");
                up();

                // -----------------------------------------------
                
                down();

                if(EnterRoom.availablePermits() == 0){
                    Display.release();
                }

                up();
                
                try{ // usar esse mutex que começa em 0 e deixa todos dormindo ate que o demonstrador acordar e exibir o filme
                    IsWatching.acquire();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                watchMovie(); // fazer algo que todos vejam o mesmo filme ao mesmo tempo quando Display.release()
                
                // -----------------------------------------------

                eat();

                // -----------------------------------------------

            }
        }
    }

}
