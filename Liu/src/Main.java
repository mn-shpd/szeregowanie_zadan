public class Main {

    public static void main(String[] args) throws Exception {
        Algorytm alg = new Algorytm();
        alg.loadNodes("input");
        alg.createNetwork();
        alg.liu();
    }
}
