public class Demonstrator extends Thread {
    private int capacity;
    private float movieLength;

    public Demonstrator(int capacity, float movieLength){
        this.capacity = capacity;
        this.movieLength = movieLength;
    }

    public void run() {
        while (true) {
            try {
                ExibitionScreen.Display.acquire();
                System.out.println("[DEMONSTRADOR] Iniciando exibição do filme por " + movieLength + " segundos.");
                Thread.sleep((long)(movieLength * 1000));
                System.out.println("[DEMONSTRADOR] Filme finalizado. Acordando fãs para lanche.");

                for (int i = 0; i < capacity; i++) {
                    ExibitionScreen.IsWatching.release(); // serve para deixar os fãs dormindo assim que entram na sala
                }

                ExibitionScreen.EnterRoom.release(capacity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
