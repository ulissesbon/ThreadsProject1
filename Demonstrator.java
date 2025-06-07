
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Semaphore;

public class Demonstrator extends Thread {
    private int capacity;
    private int movieLength;

    public static Semaphore EnterRoom;
    public static Semaphore Display;

    public Demonstrator(int capacity, int  movieLength){
        this.capacity = capacity;
        this.movieLength = movieLength;
        
        EnterRoom = new Semaphore(capacity, true);
        Display = new Semaphore(0, true);
    }

    public void displayMovie() {
        ExibitionScreen.exibitionScreenInstance.addLog("[DEMONSTRADOR] Iniciando exibição do filme. ");
                
        LocalTime initial = LocalTime.now();
        int lastPrintedSecond = -1;
        while (true) { // função para rodar o filme por X segundos
            LocalTime now = LocalTime.now();
            Duration duration = Duration.between(initial, now);
            float length = duration.toMillis() / 1000f;

            if(length >= (float) movieLength){
                break;
            }
            int currentSecond = (int) length;
            if (currentSecond != lastPrintedSecond) {
                int remainingTime = movieLength - currentSecond;
                ExibitionScreen.exibitionScreenInstance.addLog("[DEMONSTRADOR] Exibindo filme: " + remainingTime + "s restantes");
                lastPrintedSecond = currentSecond;
            }
        }
    }

    private void releasingFans() {
        for (int i = 0; i < capacity; i++) {
            ExibitionScreen.IsWatching.release(); // libera os fãs dormindo quando acaba o filme
            
            LocalTime initial = LocalTime.now();
            while (true) {
                LocalTime now = LocalTime.now();
                Duration duration = Duration.between(initial, now);
                float length = duration.toMillis() / 1000f;
                if(length >= 0.5){
                    break;
                }
            }
        }
    }

    public void run() {
        while (true) {
            try {
                Display.acquire();  // bloqueado até todos entrarem
                ExibitionScreen.exibitionScreenInstance.addLog("[DEMONSTRADOR] Acordado. Começando filme.");
                // ExibitionScreen.Line.acquire(capacity); // move a fila quando acorda
                displayMovie();
                
                ExibitionScreen.exibitionScreenInstance.addLog("[DEMONSTRADOR] Filme finalizado. Liberando fãs para lanche.");

                releasingFans();
                EnterRoom.release(capacity);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
