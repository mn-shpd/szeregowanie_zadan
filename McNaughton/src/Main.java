import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Main {

    //m - liczba maszyn
    //n - liczba zadan

    public static Double getCmax(int m, int n, List<Double> p) {

        //Wyliczenie sumy czasow i odnalezienie najdluzszego z czasow zadan.
        Double biggest = p.get(0);
        Double sum = 0.0;

        for(int i = 0; i < n; i++) {
            if(p.get(i) > biggest) {
                biggest = p.get(i);
            }
            sum += p.get(i);
        }

        sum /= m;

        System.out.println(biggest + " " + sum);

        if(biggest > sum) {
            return biggest;
        } else {
            return sum;
        }
    }

    public static void mcnaughton(int m) throws IOException {

        List<Double> p = loadDataFromFile("times.txt");

        if(m <= 0 || p.size() <= 0) {
            System.out.println("Nieprawidłowe dane.");
        }

        int n = p.size();
        Double cMax = getCmax(m, n, p);
//        cMax = Math.ceil(cMax);

        //Lista potrzebna do narysowania harmonogramu.
        List<TaskOnSchedule> schedule = new ArrayList<>();

        ////////////ALGORYTM////////////////

        //Czas, ktory minal na jednej maszynie.
        Double timeLasted = 0.0;
        int taskNum = 0;
        int machineNum = 0;

        while(taskNum < n) {

            Double taskTime = p.get(taskNum);

            if(taskTime + timeLasted <= cMax) {
                System.out.print("Z" + taskNum + "(" + taskTime + ")");
                timeLasted += taskTime;
                schedule.add(new TaskOnSchedule(taskNum, taskTime, machineNum));
                taskNum++;
            }
            else {
                if(!(Math.abs(cMax - timeLasted) < 0.01)) {

                    Double partialTime = cMax - timeLasted;
                    p.set(taskNum, taskTime - partialTime);
                    System.out.print("Z" + taskNum + "(" + partialTime + ")");
                    schedule.add(new TaskOnSchedule(taskNum, partialTime, machineNum));
                }
                System.out.println("");
                timeLasted = 0.0;
                machineNum++;
            }
        }

        drawSchedule(schedule, m, cMax);

    }

    public static void drawSchedule(List<TaskOnSchedule> schedule, int amountOfMachines, Double cMax) {

        //Dlugosc jednej jednostki czasu zadania w pixelach.
        final int unitPixels = 50;
        //Rozmiar tekstu osi Y
        final int textSizeY = 15;
        //Rozmiar tekstu na paskach zadan
        final int textSizeTask = 10;
        final int imgWidth = (int) (cMax * unitPixels + 100);
        final int imgHeight = amountOfMachines * textSizeY + 100;

        BufferedImage img = new BufferedImage((int) (cMax * unitPixels + 100), amountOfMachines * textSizeY + 100, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = img.createGraphics();
        g2d.setPaint(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, textSizeY));

        String s = "";
        FontMetrics fm = g2d.getFontMetrics();

        for(int i = 0; i < amountOfMachines; i++) {
            s = "M" + i;
            g2d.drawString(s, 0, (i + 1) * fm.getHeight());
        }

        //Linia pionowa
        g2d.drawLine(fm.stringWidth(s), 0, fm.stringWidth(s), fm.getHeight() * amountOfMachines);
        //Linia pozioma
        g2d.drawLine(fm.stringWidth(s), fm.getHeight() * amountOfMachines, imgWidth, fm.getHeight() * amountOfMachines);

        //Rysowanie tresci harmonogramu
        int x = 0, y = 0, w = 0, h = fm.getHeight();
        int lastMachineNum = 0;
        int barWidthSum = 0;
        Double timeSum = 0.0;
        List<Double> proccessedTimes = new ArrayList<>();

        g2d.setFont(new Font("Arial", Font.BOLD, textSizeTask));

        for(TaskOnSchedule task : schedule) {

            int machineNum = task.getMachineNumber();
            int taskNum = task.getTaskNumber();
            Double taskTime = task.getTaskTime();

            if(machineNum > lastMachineNum) {
                barWidthSum = 0;
                timeSum = 0.0;
            }

            x = fm.stringWidth(s) + barWidthSum;
            y = fm.getHeight() * machineNum;
            w = (int) (taskTime * unitPixels);
            h = fm.getHeight();

            g2d.drawRect(x, y, w, h);
            String taskName = "Z" + taskNum;
            g2d.drawString(taskName, x + (w / 2), y + fm.getHeight());

            timeSum += taskTime;

            if(!proccessedTimes.contains(timeSum)) {
                Double roundedTimeSum = Math.round(timeSum * 10) / 10.0;
                String timeOnX = roundedTimeSum.toString();
                g2d.drawString(timeOnX.toString(), x + w - fm.stringWidth(timeOnX) / 2, fm.getHeight() * (amountOfMachines + 1));
            }

            barWidthSum += w;
            lastMachineNum = machineNum;
        }

        g2d.dispose();

        //Zapis pliku graficznego.
        try {
            ImageIO.write(img, "png", new File("./output_image.png"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Pobiera czasy zadan z pliku.
    public static List<Double> loadDataFromFile(String path) throws IOException {

        List<Double> times = new ArrayList<>();

        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while((line = br.readLine()) != null) {
            times.add(Double.parseDouble(line));
        }

        return times;
    }

    public static void main(String[] args) throws IOException {

        mcnaughton(3);
    }
}
