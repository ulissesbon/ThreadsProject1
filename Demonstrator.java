
import java.time.Duration;
import java.time.LocalTime;

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
                
                LocalTime initial = LocalTime.now();
                int lastPrintedSecond = -1;
                while (true) { 
                    LocalTime now = LocalTime.now();
                    Duration duration = Duration.between(initial, now);
                    float length = duration.toMillis() / 1000f;

                    if(length >= movieLength){
                        break;
                    }
                    int currentSecond = (int) length;
                    if (currentSecond != lastPrintedSecond) {
                        int remainingTime = (int) movieLength - currentSecond;
                        System.out.println("[DEMONSTRADOR] Exibindo filme: " + remainingTime + "s restantes");
                        lastPrintedSecond = currentSecond;
                    }
                }
                
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
