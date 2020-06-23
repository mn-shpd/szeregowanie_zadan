public class Main {

    public static void main(String[] args) throws Exception {
        Algorytm algorytm = new Algorytm();
        algorytm.loadNodes("input");
        algorytm.createNetwork();
        algorytm.HuAndDrawSchedule(3);
    }
}
