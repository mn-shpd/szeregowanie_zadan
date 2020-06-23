public class Main {

    public static void main(String[] args) throws Exception {

        Algorytm algorytm = new Algorytm();
        algorytm.loadNodes("input");
        algorytm.cpmFrontPart();
        algorytm.cpmAbackPart();
        algorytm.printCPMTimes();
        algorytm.findCriticalPaths();
        algorytm.createAnNetwork();
        algorytm.createSchedule();
    }
}
