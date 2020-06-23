import java.util.ArrayList;
import java.util.List;

public class Node {

    private int taskNum;
    private Double time;
    private Double earliestStartTime;
    private Double earliestFinishTime;
    private Double latestStartTime;
    private Double latestFinishTime;
    private List<Node> previousNodes;
    private List<Node> nextNodes;
    //Do rysowania
    private String graphNode;

    public Node(int taskNum, Double time) {
        this.taskNum = taskNum;
        this.time = time;
        this.previousNodes = new ArrayList<>();
        this.nextNodes = new ArrayList<>();
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

    public Double getEarliestStartTime() {
        return earliestStartTime;
    }

    public void setEarliestStartTime(Double earliestStartTime) {
        this.earliestStartTime = earliestStartTime;
    }

    public Double getEarliestFinishTime() {
        return earliestFinishTime;
    }

    public void setEarliestFinishTime(Double earliestFinishTime) {
        this.earliestFinishTime = earliestFinishTime;
    }

    public Double getLatestStartTime() {
        return latestStartTime;
    }

    public void setLatestStartTime(Double latestStartTime) {
        this.latestStartTime = latestStartTime;
    }

    public Double getLatestFinishTime() {
        return latestFinishTime;
    }

    public void setLatestFinishTime(Double latestFinishTime) {
        this.latestFinishTime = latestFinishTime;
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

    public void setPreviousNodes(List<Node> previousNodes) {
        this.previousNodes = previousNodes;
    }

    public List<Node> getNextNodes() {
        return nextNodes;
    }
}
