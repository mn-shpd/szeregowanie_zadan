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
        Double taskTime;

        while((line = br.readLine()) != null) {
            //Jesli dla zadania sa nastepnicy.
            if(line.indexOf(" ") != -1) {
                taskTime = Double.parseDouble(line.substring(0, line.indexOf(" ")));
            }
            //Jesli dla zadania nie ma nastepnikow.
            else {
                taskTime = Double.parseDouble(line);
            }
            newNodes.add(new Node(taskNum++, taskTime));
        }

        nodes = newNodes;

        setRelationships(path);
    }

    public void setRelationships(String path) throws Exception {

        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        int taskNum = 1;

        while((line = br.readLine()) != null) {
            int firstSpaceIndex = line.indexOf(" ");
            //Sprawdza czy jest w ogole podana jakas zaleznosc dla danego zadania.
            if (firstSpaceIndex != -1) {
                //Pozbycie sie czasu zadania.
                String relationships = line.substring(firstSpaceIndex + 1);
                //Rozdzielenie poszczegolnych relacji w stringu.
                String[] parts = relationships.split(" ");
                //Wyluskanie biezacego wezla.
                Node currentNode = nodes.get(taskNum - 1);
                for (String nextNodeNum : parts) {
                    int nextNodeTaskNum = Integer.parseInt(nextNodeNum);
                    Node nextNode = nodes.get(nextNodeTaskNum - 1);
                    if(checkIfCyclic(currentNode, nextNode)) {
                        throw new Exception("Podano cykliczna zaleznosc dla zadania nr " + taskNum + ".");
                    }
                    else {
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

        if(currentNode == null || nextNode == null) {
            throw new IllegalArgumentException("Niepoprawne argumenty.");
        }
        else {

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

    //Ustawia poziomy dla kazdego wezla.
    public void setLevels() throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Nie zaladowano wezlow.");
        }

        //Kolejka wezlow do etykietowania.
        List<Node> nodesToCount = new ArrayList<>();

        //Dla korzeni (ostatnich wezlow) ustawia poziom na 1 i dodaje poprzedników do kolejki.
        for(Node node : nodes) {
            if(node.getNextNodes().size() == 0) {
                node.setLevel(1);
                List<Node> previousNodes = node.getPreviousNodes();
                for(Node previousNode : previousNodes) {
                    if(!nodesToCount.contains(previousNode)) {
                        nodesToCount.add(previousNode);
                    }
                }
            }
        }

        //Wylicza poziomy dla pozostalych wezlow.
        while(nodesToCount.size() > 0) {

            Node node = nodesToCount.get(0);
            int maximum = 0;

            //Wyznacza maksymalny poziom wsrod nastepnikow.
            List<Node> nextNodes = node.getNextNodes();
            for(Node nextNode : nextNodes) {
                if(nextNode.getLevel() > maximum) {
                    maximum = nextNode.getLevel();
                }
            }

            node.setLevel(maximum + 1);

            //Dodaje kolejnych poprzednikow do kolejki.
            List<Node> previousNodes = node.getPreviousNodes();

            for(Node previousNode : previousNodes) {
                if(!nodesToCount.contains(previousNode)) {
                    nodesToCount.add(previousNode);
                }
            }

            nodesToCount.remove(0);
        }
    }

    //Wybiera wolne wezly.
    public void chooseFreeNodes(List<Node> freeNodes) {

        //Wybieram wolne wezly.
        Iterator<Node> j = nodes.iterator();
        while(j.hasNext()) {

            Node node = j.next();
            List<Node> previousNodes = node.getPreviousNodes();
            boolean isFree = true;
            for(Node previousNode : previousNodes) {
                if(!previousNode.isScheduled()) {
                    isFree = false;
                }
            }
            if(isFree) {
                freeNodes.add(node);
                j.remove();
            }
        }
    }

    public List<Node> filterNodes(List<Node> freeNodes, int amountOfMachines) throws Exception {

        if(freeNodes.size() == 0) {
            throw new Exception("Podana jako parametr lista wezlow jest pusta.");
        }

        List<Node> toBeScheduled = new ArrayList<>();
        int maxLevel = 0;

        //Wyznacza maksymalny poziom wezlow.
        for(Node freeNode : freeNodes) {
            if(freeNode.getLevel() > maxLevel) {
                maxLevel = freeNode.getLevel();
            }
        }

        for(int i = maxLevel; i > 0; i--) {
            Iterator<Node> k = freeNodes.iterator();
            while(k.hasNext()) {
                Node freeNode = k.next();
                if (freeNode.getLevel() == i) {
                    toBeScheduled.add(freeNode);
                    k.remove();
                    if (toBeScheduled.size() == amountOfMachines) {
                        return toBeScheduled;
                    }
                }
            }
        }

        return toBeScheduled;
    }

    public void HuAndDrawSchedule(int amountOfMachines) throws Exception {

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
        final int imgWidth = (int) (nodes.size() * unitPixels + 100);
        final int imgHeight = amountOfMachines * textSizeY + 100;

        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

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

        //Do rysowania tresci harmonogramu
        int x = 0, y = 0, w = 0, h = fm.getHeight();
        g2d.setFont(new Font("Arial", Font.BOLD, textSizeTask));
        int unitCounter = 0;

        //----------------------ALGORYTM-HU-I-RYSOWANIE-TRESCI-HARMONOGRAMU---------------------

        List<Node> freeNodes = new ArrayList<>();
        //Maksymalny dotyczasowy czas, ktory minal na maszynach.
        Double timeLastedOnMachines = 0.0;
        //Czas jednego zadania na harmonogramie.
        Double time = nodes.get(0).getTime();

        //Wylicza poziom dla kazdego zadania.
        this.setLevels();
        //Okresla ilosc wezlow do umieszczenia na harmonogramie.
        int amountToSchedule = nodes.size();

        while(amountToSchedule > 0) {

            //Aktualizuje liste wolnych wezlow.
            chooseFreeNodes(freeNodes);

            //Zapisuje wezly w nowej liscie, wybierajac te o najwyszych poziomach sposrod wszystkich i usuwa z listy wejsciowej.
            List<Node> toBeScheduled = filterNodes(freeNodes, amountOfMachines);

            int machineNum = 0;
            for(Node tbsNode : toBeScheduled) {

                int taskNum = tbsNode.getTaskNum();

                x = fm.stringWidth(s) + (int) (timeLastedOnMachines * unitPixels);
                y = fm.getHeight() * machineNum;
                w = (int) (time * unitPixels);
                h = fm.getHeight();

                g2d.setPaint(Color.CYAN);
                g2d.drawRect(x, y, w, h);
                g2d.setPaint(Color.WHITE);
                String taskName = "Z" + taskNum;
                g2d.drawString(taskName, x + (w / 2), y + fm.getHeight());

                machineNum++;
                tbsNode.setScheduled(true);
                amountToSchedule--;
            }
            timeLastedOnMachines += time;
            unitCounter++;
        }

        //Rysowanie jednostek osi poziomej.
        for(int i = 1; i < unitCounter + 1; i++) {
            g2d.drawString(Double.toString(timeRound(i * time)), textSizeY + (int) (i * time * unitPixels), fm.getHeight() * (amountOfMachines + 1));
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
