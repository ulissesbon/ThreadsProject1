import java.util.concurrent.Semaphore;

public class Threads {
    public static Semaphore Display = new Semaphore(0);
    public static Semaphore Mutex = new Semaphore(1);
    public static Semaphore IsWatching = new Semaphore(0);
    public static Semaphore EnterRoom = new Semaphore(5, true);  // semaforo para controlar o numero de pessoas dentro da sala para assistir, quando chega em 0, o fã não pode entrar

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

        public void displayMovie(){
            //timer ate acabar o filme
        }

        public void run(){
            while (true) {

                try {
                    Display.acquire();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IsWatching.a
                displayMovie();


            }
        }
    }


    static class Fan extends Thread {
        private int id;
        private int eatingTimer;

        public Fan(int eatingTime){
            this.eatingTimer = eatingTime;
        }


        public void setEatingTimer(int eatingTime) {
            this.eatingTimer = eatingTime;
        }
        public int getEatingTimer() {
            return this.eatingTimer;
        }


        public void watchMovie(){
            //espera o displayMovie()
            double soma = 0;
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 2000; j++) {
                    soma = soma + Math.sin(i) + Math.sin(j);
                }
            }
        }

        public void eat(){
            //sai da sala e vai comer em tempo x | x = eatingTimer
            double soma = 0;
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 2000; j++) {
                    soma = soma + Math.sin(i) + Math.sin(j);
                }
            }
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
               
                down();

                try{
                    EnterRoom.acquire();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
