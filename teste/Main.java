public class Main {
    public static void main(String[] args) {
        Teste.Demonstrator demo = new Teste.Demonstrator(5, 6); // capacidade 5, filme com 6s
        demo.start();

        for (int i = 0; i < 10; i++) {
            Teste.Fan fan = new Teste.Fan(4); // lanche de 4s
            fan.start();

            try {
                Thread.sleep(1000); // fãs vão sendo criados com atraso
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}