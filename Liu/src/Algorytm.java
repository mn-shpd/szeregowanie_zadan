import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Algorytm {

    private List<Node> nodes;

    public void loadNodes(String path) throws Exception {

        List<Node> newNodes = new ArrayList<>();

        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        int taskNum = 1;
        String[] taskParameters;

        while ((line = br.readLine()) != null) {
            //Jesli dla zadania sa nastepnicy.
            if (line.indexOf("|") != -1) {
                String parametersLine = line.substring(0, line.indexOf("|"));
                taskParameters = parametersLine.split(" ");
            }
            //Jesli dla zadania nie ma nastepnikow.
            else {
                taskParameters = line.split(" ");
            }

            if(taskParameters.length != 3) {
                throw new Exception("Niepoprawna ilosc parametrow jednego z zadan.");
            }
            else {
                newNodes.add(new Node(taskNum++, Double.parseDouble(taskParameters[0]), Double.parseDouble(taskParameters[1]), Double.parseDouble(taskParameters[2])));
            }
        }

        nodes = newNodes;

        setRelationships(path);
    }

    public void setRelationships(String path) throws Exception {

        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        int taskNum = 1;

        while ((line = br.readLine()) != null) {
            int firstPipeIndex = line.indexOf("|");
            //Sprawdza czy jest w ogole podana jakas zaleznosc dla danego zadania.
            if (firstPipeIndex != -1) {
                //Pozbycie sie czasu zadania.
                String relationships = line.substring(firstPipeIndex + 2);
                //Rozdzielenie poszczegolnych relacji w stringu.
                String[] parts = relationships.split(" ");
                //Wyluskanie biezacego wezla.
                Node currentNode = nodes.get(taskNum - 1);
                for (String nextNodeNum : parts) {
                    int nextNodeTaskNum = Integer.parseInt(nextNodeNum);
                    Node nextNode = nodes.get(nextNodeTaskNum - 1);
                    if (checkIfCyclic(currentNode, nextNode)) {
                        throw new Exception("Podano cykliczna zaleznosc dla zadania nr " + taskNum + ".");
                    } else {
                        //Dodaje nastepnika do biezacego wezla.
                        currentNode.addNextNode(nextNode);
                        //Dodaje poprzednika (biezacy wezel iteracji) do nastepnika.
                        nextNode.addPreviousNode(currentNode);
                    }
                }
            }
            taskNum++;
        }
    }

    //Sprawdza czy nie podano zaleznosci cyklicznej.
    public boolean checkIfCyclic(Node currentNode, Node nextNode) {

        if (currentNode == null || nextNode == null) {
            throw new IllegalArgumentException("Niepoprawne argumenty.");
        } else {

            //Kolejka wezlow.
            List<Node> nodesToCheck = new ArrayList<>();
            nodesToCheck.add(currentNode);

            while (nodesToCheck.size() > 0) {
                Node temp = nodesToCheck.get(0);
                if (temp == nextNode) {
                    return true;
                }
                List<Node> previousNodes = temp.getPreviousNodes();
                if (previousNodes.size() != 0) {
                    for (Node previousNode : previousNodes) {
                        if (!nodesToCheck.contains(previousNode)) {
                            nodesToCheck.add(previousNode);
                        }
                    }
                }
                nodesToCheck.remove(0);
            }

            return false;
        }
    }

    //Wylicza zmodyfikowane terminy zakonczenia dla wszystkich zadań.
    public void setModifiedDeadlines() throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Nie zaladowano wezlow.");
        }

        for(int i = 0; i < nodes.size(); i++) {

            //Kolejka wezlow do sprawdzenia.
            List<Node> nodesToCheck = new ArrayList<>();
            Double minimum = nodes.get(i).getDeadline();

            nodesToCheck.add(nodes.get(i));

            //Sprawdza terminy zakonczenia dla nastepników.
            while(nodesToCheck.size() > 0) {

                Node node = nodesToCheck.get(0);

                if (node.getDeadline() < minimum) {
                    minimum = node.getDeadline();
                }

                //Dodaje nastepnikow wezla do kolejki.
                List<Node> nextNodes = node.getNextNodes();

                for (Node nextNode : nextNodes) {
                    if (!nodesToCheck.contains(nextNode)) {
                        nodesToCheck.add(nextNode);
                    }
                }

                nodesToCheck.remove(0);
            }

            nodes.get(i).setModifiedDeadline(minimum);
        }
    }

    //Aktualizuje liste zadan o zadania, ktore sa w systemie w danym momencie.
    public void updateReleasedNodes(List<Node> nodes, List<Node> releasedNodes, Double timeLastedOnMachine) {

        //Wybieram dostepne wezly.
        Iterator<Node> j = nodes.iterator();
        while(j.hasNext()) {

            Node node = j.next();

            if(node.getReleaseTime() <= timeLastedOnMachine) {
                releasedNodes.add(node);
                j.remove();
            }
        }
    }

    public Node chooseNode(List<Node> releasedNodes) {

        if(releasedNodes.size() == 0) {
            return null;
        } else {

            Node chosenNode = null;
            Double minimumMD = Double.POSITIVE_INFINITY;

            //Wyszukuje zadanie z najnizszym zmodyfikowanym terminem zakonczenia z listy dostepnych zadan.
            for (Node releasedNode : releasedNodes) {
                if (releasedNode.getModifiedDeadline() < minimumMD) {
                    boolean isFree = true;
                    List<Node> previousNodes = releasedNode.getPreviousNodes();
                    for(Node previousNode : previousNodes) {
                        if(previousNode.isScheduled() == false) {
                            isFree = false;
                        }
                    }
                    if(isFree) {
                        chosenNode = releasedNode;
                        minimumMD = releasedNode.getModifiedDeadline();
                    }
                }
            }

            return chosenNode;
        }
    }

    public void liu() throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Nie zaladowano wezlow.");
        }

        //Kopia wezlow.
        List<Node> nodes_copy = new ArrayList<>(nodes);
        //Lista zadan na harmonogramie.
        List<LiuScheduleEl> schedule = new ArrayList<>();
        //Czas, ktory minal na maszynie.
        Double timeLastedOnMachine = 0.0;
        //Zadania aktualnie wystepujace w systemie.
        List<Node> releasedNodes = new ArrayList<>();
        int amountToSchedule = nodes.size();

        //Wyznacza zadaniom zmodyfikowane terminy zakonczenia.
        setModifiedDeadlines();

        while(amountToSchedule > 0) {

            //Aktualizuje liste zadan obecnych w systemie.
            updateReleasedNodes(nodes_copy, releasedNodes, timeLastedOnMachine);
            //Wybiera wolne zadanie o najnizszym zmodyfikowanym terminie zakonczenia z listy obecnych w systemie zadan.
            Node toBeScheduled = chooseNode(releasedNodes);

            if(toBeScheduled == null) {
                LiuScheduleEl last = schedule.get(schedule.size() - 1);
                if(last.getNode() == null) {
                    last.setTime(last.getTime() + 1.0);
                }
                else {
                    schedule.add(new LiuScheduleEl(null, 1.0));
                }
            } else {

                if (schedule.size() == 0 || schedule.get(schedule.size() - 1).getNode() != toBeScheduled) {
                    schedule.add(new LiuScheduleEl(toBeScheduled, 1.0));
                } else {
                    LiuScheduleEl last = schedule.get(schedule.size() - 1);
                    last.setTime(last.getTime() + 1.0);
                }

                toBeScheduled.setTime(toBeScheduled.getTime() - 1.0);
                if (toBeScheduled.getTime() == 0.0) {
                    toBeScheduled.setScheduled(true);
                    toBeScheduled.setLatency(timeLastedOnMachine + 1.0 - toBeScheduled.getDeadline());
                    releasedNodes.remove(toBeScheduled);
                    amountToSchedule--;
                }
            }

            timeLastedOnMachine += 1.0;
        }

        System.out.println("-----------SCHEDULE-----------");
        for(LiuScheduleEl el : schedule) {
            if(el.getNode() != null) {
                System.out.print(el.getNode().getTaskNum() + "(" + el.getTime() + ")");
            }
            else {
                System.out.print("-(" + el.getTime() + ")");
            }
            System.out.print(" . ");
        }

        System.out.println("\n-------------LATENCY-------------");
        for(Node node : nodes) {
            System.out.print(node.getTaskNum() + "(" + node.getLatency() + ") . ");
        }

        System.out.println("\nLMax: " + countLMax());

        drawSchedule(schedule, timeLastedOnMachine);
    }

    public Double countLMax() throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Nie zaladowano wezlow.");
        }

        Double LMax = 0.0;

        for(Node node : nodes) {
            if(node.getLatency() > LMax) {
                LMax = node.getLatency();
            }
        }

        return LMax;
    }

    //Rysowanie harmonogramu
    public void drawSchedule(List<LiuScheduleEl> schedule, Double scheduleEndTime) throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Nie zaladowano wezlow.");
        }

        //--------------PARAMETRY-RYSOWANIA-------------------

        //Dlugosc jednej jednostki czasu zadania w pixelach.
        final int unitPixels = 40;
        //Rozmiar tekstu osi Y
        final int textSizeY = 15;
        //Rozmiar tekstu na paskach zadan
        final int textSizeTask = 10;
        final int imgWidth = (int) (scheduleEndTime * unitPixels + 100);
        final int imgHeight =  textSizeY + 100;

        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = img.createGraphics();
        g2d.setPaint(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, textSizeY));

        String s = "";
        FontMetrics fm = g2d.getFontMetrics();

        s = "M0";
        g2d.drawString(s, 0, fm.getHeight());

        //Linia pionowa
        g2d.drawLine(fm.stringWidth(s), 0, fm.stringWidth(s), fm.getHeight());
        //Linia pozioma
        g2d.drawLine(fm.stringWidth(s), fm.getHeight(), imgWidth, fm.getHeight());

        //Do rysowania tresci harmonogramu
        int x = 0, y = 0, w = 0, h = fm.getHeight();
        g2d.setFont(new Font("Arial", Font.BOLD, textSizeTask));
        Double timeLastedOnMachine = 0.0;

        //---------------------RYSOWANIE-TRESCI-HARMONOGRAMU---------------------

        for(LiuScheduleEl el : schedule) {

            int taskNum = 0;
            if(el.getNode() != null) {
                taskNum = el.getNode().getTaskNum();
            }

            x = fm.stringWidth(s) + (int) (timeLastedOnMachine * unitPixels);
            y = 0;
            w = (int) (el.getTime() * unitPixels);
            h = fm.getHeight();

            g2d.setPaint(Color.CYAN);
            g2d.drawRect(x, y, w, h);
            g2d.setPaint(Color.WHITE);
            String taskName;
            if(taskNum == 0) {
                taskName = "-";
            }
            else {
                taskName = "Z" + taskNum;
            }
            g2d.drawString(taskName, x + (w / 2), y + fm.getHeight());
            timeLastedOnMachine += el.getTime();
        }

        //Rysowanie jednostek osi poziomej.
        for(int i = 1; i <= scheduleEndTime; i++) {
            g2d.drawString(Double.toString(i), textSizeY + (int) (i * unitPixels), fm.getHeight() * 2);
        }

        g2d.dispose();

        //Zapis pliku graficznego.
        try {
            ImageIO.write(img, "png", new File("./schedule.png"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void createNetwork() throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Nie załadowano wezlow.");
        }
        else {

            DefaultDirectedGraph<String, Edge> g = new DefaultDirectedGraph<String, Edge>(Edge.class);

            //Dodanie wezlow do grafu.
            for(int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                node.setGraphNode("Z" + node.getTaskNum() + " (" + node.getTime() + ")");
                g.addVertex(node.getGraphNode());
            }

            //Stworzenie zaleznosci pomiedzy wezlami.
            for(int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                List<Node> nextNodes = node.getNextNodes();
                for(Node nextNode : nextNodes) {
                    g.addEdge(node.getGraphNode(), nextNode.getGraphNode());
                }
            }

            JGraphXAdapter<String, Edge> graphAdapter = new JGraphXAdapter<String, Edge>(g);
            mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
            layout.execute(graphAdapter.getDefaultParent());

            BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
            File imgFile = new File("network.png");
            ImageIO.write(image, "PNG", imgFile);
        }
    }

    public Double timeRound(Double time) {
        time *= 10;
        Long roundedTime = Math.round(time);
        return roundedTime.doubleValue() / 10;
    }
}