import java.util.concurrent.Semaphore;

public class Threads {
    public static Semaphore Display = new Semaphore(1);
    public static Semaphore EnterRoom = new Semaphore(5);  // semaforo para controlar o numero de pessoas dentro da sala para assistir, quando chega em 0, o fã não pode entrar

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

        }

        public void run(){
            while (true) {

                if(EnterRoom.availablePermits() == 0){

                    try {
                        Display.acquire();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    displayMovie();
                    Display.release();

                }

            }
        }
    }


    static class Fan extends Thread {

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
            //entra na sala em fila e espera o displayMovie()

        }

        public void eat(){
            //sai da sala e vai comer em tempo x | x = eatingTimer

        }

        public void run(){

            while (true) {

                try{ 
                    EnterRoom.acquire();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                watchMovie();
                EnterRoom.release();
                
                eat();

            }
        }
    }

}
