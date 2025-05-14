import java.util.concurrent.Semaphore;

public class ThreadFan extends Thread{
    public static Semaphore Aux = new Semaphore(0);

    public ThreadFan(String name){
        super (name);
    }
    
    public void run(){
        while(true){

        }
    }
}
