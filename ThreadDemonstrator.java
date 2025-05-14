import java.util.concurrent.Semaphore;

public class ThreadDemonstrator extends Thread {
    public static Semaphore Aux = new Semaphore(0);
    
    public ThreadDemonstrator (String name){
        super (name);
    }

    public void run(){
        while(true){

        }
    }
}
