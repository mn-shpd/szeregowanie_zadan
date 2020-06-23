import java.util.ArrayList;
import java.util.List;

public class Node {

    private int taskNum;
    private Double time;
    private Double releaseTime;
    private Double deadline;
    private Double latency;
    //Do algorytmu Liu
    private Double modifiedDeadline;
    //////
    private boolean isScheduled;
    private List<Node> previousNodes;
    private List<Node> nextNodes;
    //Do rysowania grafu
    private String graphNode;

    public Node(int taskNum, Double time, Double releaseTime, Double deadline) {
        this.taskNum = taskNum;
        this.time = time;
        this.releaseTime = releaseTime;
        this.deadline = deadline;
        this.latency = 0.0;
        this.modifiedDeadline = 0.0;
        this.isScheduled = false;
        this.previousNodes = new ArrayList<>();
        this.nextNodes = new ArrayList<>();
    }

    public Double getLatency() {
        return latency;
    }

    public void setLatency(Double latency) {
        this.latency = latency;
    }

    public Double getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Double releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Double getDeadline() {
        return deadline;
    }

    public void setDeadline(Double deadline) {
        this.deadline = deadline;
    }

    public Double getModifiedDeadline() {
        return modifiedDeadline;
    }

    public void setModifiedDeadline(Double modifiedDeadline) {
        this.modifiedDeadline = modifiedDeadline;
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
