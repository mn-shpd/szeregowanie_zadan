import com.mxgraph.layout.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Algorytm {

    private List<Node> nodes;

    public void loadNodes(String path) throws Exception {

        List<Node> newNodes = new ArrayList<>();

        //Utworzenie "niewidzialnego" korzenia.
        Node root = new Node(0, 0.0);
        root.setEarliestStartTime(0.0);
        root.setEarliestFinishTime(0.0);
        root.setLatestStartTime(0.0);
        root.setLatestFinishTime(0.0);
        newNodes.add(root);

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
        Double taskTime;

        while((line = br.readLine()) != null) {
            int firstSpaceIndex = line.indexOf(" ");
            //Sprawdza czy jest w ogole podana jakas zaleznosc dla danego zadania.
            if (firstSpaceIndex != -1) {
                //Pozbycie sie czasu zadania.
                String relationships = line.substring(firstSpaceIndex + 1);
                //Rozdzielenie poszczegolnych relacji w stringu.
                String[] parts = relationships.split(" ");
                //Wyluskanie biezacego wezla.
                Node currentNode = nodes.get(taskNum);
                for (String nextNodeNum : parts) {
                    int nextNodeTaskNum = Integer.parseInt(nextNodeNum);
                    Node nextNode = nodes.get(nextNodeTaskNum);
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

        //W wezlach, w ktorych nie ma poprzednikow dodaje korzen jako poprzednik.
        Node root = nodes.get(0);
        for(int i = 1; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if(node.getPreviousNodes().size() == 0) {
                node.addPreviousNode(root);
                root.addNextNode(node);
            }
        }
    }

    //Sprawdza czy nie podano zaleznosci cyklicznej.
    public boolean checkIfCyclic(Node currentNode, Node nextNode) {

        if(currentNode == null || nextNode == null) {
            throw new IllegalArgumentException("Niepoprawne argumenty.");
        }
        else {
            //Jezeli nastepny wezel jest korzeniem.
            if (nextNode == nodes.get(0)) {
                return true;
            }

            //Kolejka wezlow.
            List<Node> nodesToCheck = new ArrayList<>();
            nodesToCheck.add(currentNode);

            while (nodesToCheck.size() > 0) {
                Node temp = nodesToCheck.get(0);
                if (temp == nextNode) {
                    return true;
                }
                List<Node> previousNodes = temp.getPreviousNodes();
                if (previousNodes != null) {
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

    //Wykonuje czesc algorytmu CPM w przód.
    public void cpmFrontPart() throws Exception {

        if (nodes.size() == 0) {
            throw new Exception("Wezly nie zostaly zaladowane.");
        } else {

            //Kolejka wezlow.
            List<Node> nodesToCount = new ArrayList<>();
            //Na poczatek dodaje wszystkich nastepnikow korzenia.
            List<Node> rootsNextNodes = nodes.get(0).getNextNodes();
            for (Node nextNode : rootsNextNodes) {
                nodesToCount.add(nextNode);
            }
            //Do wyszukiwania maksymalnego czasu u poprzednikow.
            Double maximum = 0.0;
            //Do sprawdzenia czy poprzednik nie ma wyliczonego mozliwego czasu rozpoczecia.
            boolean wasNull;

            while (nodesToCount.size() > 0) {

                Node node = nodesToCount.get(0);
                wasNull = false;
                maximum = 0.0;
                List<Node> previousNodes = node.getPreviousNodes();

                for (Node previousNode : previousNodes) {
                    //Jezeli jeszcze nie wyliczono czasu dla ktoregos z poprzednikow, to przerzucam wezel na koniec kolejki.
                    if (previousNode.getEarliestStartTime() == null) {
                        nodesToCount.remove(0);
                        nodesToCount.add(node);
                        wasNull = true;
                        break;
                    }

                    Double temp = previousNode.getEarliestStartTime() + previousNode.getTime();
                    if (temp > maximum) {
                        maximum = temp;
                    }
                }
                if (!wasNull) {
                    node.setEarliestStartTime(maximum);
                    node.setEarliestFinishTime(maximum + node.getTime());
                    nodesToCount.remove(0);
                }

                //Dodaje do kolejki nastepnikow biezacego wezla.
                List<Node> nextNodes = node.getNextNodes();
                for (Node nextNode : nextNodes) {
                    if(!nodesToCount.contains(nextNode)) {
                        nodesToCount.add(nextNode);
                    }
                }
            }
        }
    }

    //Wykonuje czesc algorytmu CPM w tył.
    public void cpmAbackPart() throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Wezly nie zostaly zaladowane.");
        } else {

            //Kolejka wezlow.
            List<Node> nodesToCount = new ArrayList<>();
            //Na poczatku dodaje wszystkie ostatnie wezly (takie ktore nie maja nastepnikow).
            for(Node node : nodes) {
                if(node.getNextNodes().size() == 0) {
                    node.setLatestFinishTime(node.getEarliestFinishTime());
                    node.setLatestStartTime(node.getLatestFinishTime() - node.getTime());
                    nodesToCount.add(node);
                }
            }

            //Do wyszukiwania minimalnego czasu rozpoczecia u nastepnikow.
            Double minimum;

            while (nodesToCount.size() > 0) {

                Node node = nodesToCount.get(0);
                List<Node> nextNodes = node.getNextNodes();

                if(nextNodes.size() > 0) {
                    //Ustawia najmniejszy czas rowny czasowi rozpoczecia jednego z nastepnikow dla porownan czasow.
                    minimum = nextNodes.get(0).getEarliestStartTime();

                    for (Node nextNode : nextNodes) {
                        if (nextNode.getEarliestStartTime() < minimum) {
                            minimum = nextNode.getEarliestStartTime();
                        }
                    }
                    node.setLatestFinishTime(minimum);
                    node.setLatestStartTime(minimum - node.getTime());
                    nodesToCount.remove(0);
                }
                else {
                    nodesToCount.remove(0);
                }

                //Dodaje do kolejki poprzednikow biezacego wezla.
                List<Node> previousNodes = node.getPreviousNodes();
                for (Node previousNode : previousNodes) {
                    if (!nodesToCount.contains(previousNode)) {
                        nodesToCount.add(previousNode);
                    }
                }
            }
        }
    }

    public List<List<Node>> findCriticalPaths() throws Exception {

        if (nodes.size() == 0) {
            throw new Exception("Wezly nie zostaly zaladowane.");
        } else {

            //Kolejka wezlow.
            List<Node> nodesToCheck = new ArrayList<>();
            //Sciezki krytyczne.
            List<List<Node>> criticalPaths = new ArrayList<>();

            //Na poczatek dodaje wszystkich nastepnikow korzenia, w ktorych zachodzi zaleznosc LS - ES = 0 i LF - EF = 0.
            List<Node> rootsNextNodes = nodes.get(0).getNextNodes();
            for (Node nextNode : rootsNextNodes) {
                if ((nextNode.getLatestStartTime() - nextNode.getEarliestStartTime() == 0) && (nextNode.getLatestFinishTime() - nextNode.getEarliestFinishTime() == 0)) {
                    List<Node> newCriticalPath = new ArrayList<>();
                    newCriticalPath.add(nextNode);
                    criticalPaths.add(newCriticalPath);
                    nodesToCheck.add(nextNode);
                }
            }

            while (nodesToCheck.size() > 0) {

                Node node = nodesToCheck.get(0);
                List<Node> nextNodes = node.getNextNodes();

                boolean isAlready = false;
                List<Node> toCopy = null;
                List<Node> pointer = null;
                for (List<Node> criticalPath : criticalPaths) {
                    if (criticalPath.contains(node)) {
                        toCopy = new ArrayList<>(criticalPath);
                        pointer = criticalPath;
                    }
                }

                for (Node nextNode : nextNodes) {
                    //Sprawdzam zaleznosc LS - ES = 0 i LF - EF = 0.
                    if ((nextNode.getLatestStartTime() - nextNode.getEarliestStartTime() == 0) && (nextNode.getLatestFinishTime() - nextNode.getEarliestFinishTime() == 0)) {
                        if (isAlready) {
                            List<Node> newCriticalPath = new ArrayList<>(toCopy);
                            newCriticalPath.add(nextNode);
                            criticalPaths.add(newCriticalPath);
                            nodesToCheck.add(nextNode);
                        } else {
                            pointer.add(nextNode);
                            isAlready = true;
                            nodesToCheck.add(nextNode);
                        }
                    }
                }
                nodesToCheck.remove(0);
            }

            //Zapisuje do listy wszystkie czasy LF wyszukanych sciezek krytycznych.
            List<Double> lastNodesLatestFinishTime = new ArrayList<>();

            for(List<Node> criticalPath : criticalPaths) {
                lastNodesLatestFinishTime.add(criticalPath.get(criticalPath.size() - 1).getLatestFinishTime());
            }

            //Wyznacza maximum z powyzszej listy.
            Double maximum = 0.0;

            for(Double latestFinishTime : lastNodesLatestFinishTime) {
                if(latestFinishTime > maximum) {
                    maximum = latestFinishTime;
                }
            }

            //Usuwa z listy sciezek krytycznych te ktore nie spelniaja kryterium max czasu LF.
            Double finalMaximum = maximum;
            criticalPaths.removeIf(criticalPath -> criticalPath.get(criticalPath.size() - 1).getLatestFinishTime() < finalMaximum);

            printCriticalPaths(criticalPaths);
            return criticalPaths;
        }
    }

    public void printCriticalPaths(List<List<Node>> criticalPaths) {

        System.out.println("");
        System.out.println("Critical paths: ");
        for(List<Node> criticalPath : criticalPaths) {
            for(Node node : criticalPath) {
                System.out.print(node.getTaskNum() + " ");
            }
            System.out.println("");
        }
    }


    public void createAnNetwork() throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Nie załadowano wezlow.");
        }
        else {

            DefaultDirectedGraph<String, Edge> g = new DefaultDirectedGraph<String, Edge>(Edge.class);

            //Dodanie wezlow do grafu.
            for(int i = 1; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                node.setGraphNode("Z" + node.getTaskNum() + " (" + node.getTime() + ")");
                g.addVertex(node.getGraphNode());
            }

            //Stworzenie zaleznosci pomiedzy wezlami.
            for(int i = 1; i < nodes.size(); i++) {
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

    public void createSchedule() throws Exception {

        if(nodes.size() == 0) {
            throw new Exception("Wezly nie zostaly zaladowane.");
        }

        List<ScheduleMachine> scheduleMachines = new ArrayList<>();

        //Dodaje do listy nowe maszyny z przydzielonymi poczatkowymi zadaniami.
        Node root = nodes.get(0);
        List<Node> rootsNextNodes = root.getNextNodes();
        for(Node nextNode : rootsNextNodes) {
            ScheduleMachine newScheduleMachine = new ScheduleMachine();
            newScheduleMachine.addTask(nextNode);
            newScheduleMachine.setTimeLasted(nextNode.getTime());
            scheduleMachines.add(newScheduleMachine);
        }

        boolean newMachineFlag = false;

        for(int i = rootsNextNodes.size() + 1; i < nodes.size(); i++) {
            newMachineFlag = true;
            Node node = nodes.get(i);
            Double earliestStartTime = node.getEarliestStartTime();
            Double earliestFinishTime = node.getEarliestFinishTime();
            for(ScheduleMachine scheduleMachine : scheduleMachines) {
                Double timeLasted = scheduleMachine.getTimeLasted();
                if(earliestStartTime >= timeLasted) {
                    scheduleMachine.addTask(node);
                    scheduleMachine.setTimeLasted(earliestFinishTime);
                    newMachineFlag = false;
                    break;
                }
            }
            if(newMachineFlag) {
                ScheduleMachine newScheduleMachine = new ScheduleMachine();
                newScheduleMachine.addTask(node);
                newScheduleMachine.setTimeLasted(earliestFinishTime);
                scheduleMachines.add(newScheduleMachine);
            }
        }


        System.out.println("");
        for(int i = scheduleMachines.size() - 1; i >= 0; i--) {
            ScheduleMachine scheduleMachine = scheduleMachines.get(i);
            List<Node> tasks = scheduleMachine.getTasks();
            for(Node task : tasks) {
                System.out.print(task.getTaskNum() + "(" + task.getEarliestStartTime() + ")");
            }
            System.out.println("");
        }

        //Wyznaczenie maksimum czasu na maszynach potrzebnego do rysowaniu harmonogramu.
        Double maximum = 0.0;

        for(ScheduleMachine scheduleMachine : scheduleMachines) {
            if(scheduleMachine.getTimeLasted() > maximum) {
                maximum = scheduleMachine.getTimeLasted();
            }
        }

        drawSchedule(scheduleMachines, maximum);
    }

    public void drawSchedule(List<ScheduleMachine> scheduleMachines, Double maxTimeOnMachine) {

        //Dlugosc jednej jednostki czasu zadania w pixelach.
        final int unitPixels = 25;
        //Rozmiar tekstu osi Y
        final int textSizeY = 15;
        //Rozmiar tekstu na paskach zadan
        final int textSizeTask = 10;
        final int imgWidth = (int) (maxTimeOnMachine * unitPixels + 100);
        final int imgHeight = scheduleMachines.size() * textSizeY + 100;

        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = img.createGraphics();
        g2d.setPaint(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, textSizeY));

        String s = "";
        FontMetrics fm = g2d.getFontMetrics();

        for(int i = 0; i < scheduleMachines.size(); i++) {
            s = "M" + i;
            g2d.drawString(s, 0, (i + 1) * fm.getHeight());
        }

        //Linia pionowa
        g2d.drawLine(fm.stringWidth(s), 0, fm.stringWidth(s), fm.getHeight() * scheduleMachines.size());
        //Linia pozioma
        g2d.drawLine(fm.stringWidth(s), fm.getHeight() * scheduleMachines.size(), imgWidth, fm.getHeight() * scheduleMachines.size());

        //Rysowanie tresci harmonogramu
        int x = 0, y = 0, w = 0, h = fm.getHeight();
        g2d.setFont(new Font("Arial", Font.BOLD, textSizeTask));

        for(int i = 0; i < scheduleMachines.size(); i++) {

            List<Node> tasks = scheduleMachines.get(i).getTasks();

            for(Node task : tasks) {

                int taskNum = task.getTaskNum();
                Double taskTime = task.getTime();
                Double earliestStartTime = task.getEarliestStartTime();

                //Wspolrzedne konkretnego bloku zadania.
                x = fm.stringWidth(s) + (int) (earliestStartTime * unitPixels);
                y = fm.getHeight() * (scheduleMachines.size() - 1 - i);
                w = (int) (taskTime * unitPixels);
                h = fm.getHeight();

                g2d.setPaint(Color.CYAN);
                g2d.drawRect(x, y, w, h);
                g2d.setPaint(Color.WHITE);
                String taskName = "Z" + taskNum;
                g2d.drawString(taskName, x + (w / 2), y + fm.getHeight());
            }
        }

        //Jednostki na osi poziomej.
        Integer counter = 1;
        for(int i = fm.stringWidth(s) + unitPixels; i <= (maxTimeOnMachine * unitPixels) + fm.stringWidth(s); i += unitPixels) {
            String counterStr = Integer.toString(counter);
            g2d.drawString(Integer.toString(counter), i, fm.getHeight() * (scheduleMachines.size() + 1));
            counter++;
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

    public void printCPMTimes() {

        System.out.println("TN\t\t\tT\t\t\tES\t\t\tEF\t\t\tLS\t\t\tLF");

        for(int i = 1; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            System.out.println(node.getTaskNum() + "\t\t\t" + node.getTime() + "\t\t\t" + node.getEarliestStartTime() +
                    "\t\t\t" + node.getEarliestFinishTime() + "\t\t\t" + node.getLatestStartTime() + "\t\t\t" + node.getLatestFinishTime());
        }
    }

    public void printRelationships() {

        for(Node node : nodes) {
            System.out.print(node.getTime() + " ");
            List<Node> nextNodes = node.getNextNodes();
            for(Node nextNode : nextNodes) {
                System.out.print(nextNode.getTaskNum() + " ");
            }
            System.out.println("");
        }
    }

    public void printEst() {
        for(Node node : nodes) {
            System.out.println(node.getTaskNum() + " " + node.getEarliestStartTime());
        }
    }
}
