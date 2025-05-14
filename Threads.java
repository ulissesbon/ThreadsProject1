import java.util.concurrent.Semaphore;

public class Threads {
    public static Semaphore Display = new Semaphore(4);
    public static Semaphore Watch = new Semaphore(0);

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
                int crowd = 0;

                if(crowd == this.getCapacity())
                displayMovie();

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

                watchMovie();

                eat();

            }
        }
    }

}
