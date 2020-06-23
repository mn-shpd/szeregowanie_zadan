import java.util.ArrayList;
import java.util.List;

public class Node {

    private int taskNum;
    private Double time;
    //Do algorytmu Hu
    private int level;
    private boolean isScheduled;
    private List<Node> previousNodes;
    private List<Node> nextNodes;
    //Do rysowania grafu
    private String graphNode;

    public Node(int taskNum, Double time) {
        this.taskNum = taskNum;
        this.time = time;
        this.isScheduled = false;
        this.previousNodes = new ArrayList<>();
        this.nextNodes = new ArrayList<>();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isScheduled() {
        return isScheduled;
    }

    public void setScheduled(boolean scheduled) {
        isScheduled = scheduled;
    }

    public void addNextNode(Node node) {
        nextNodes.add(node);
    }

    public void addPreviousNode(Node node) {
        previousNodes.add(node);
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public String getGraphNode() {
        return graphNode;
    }

    public void setGraphNode(String graphNode) {
        this.graphNode = graphNode;
    }

    public List<Node> getPreviousNodes() {
        return previousNodes;
    }

    public List<Node> getNextNodes() {
        return nextNodes;
    }
}
