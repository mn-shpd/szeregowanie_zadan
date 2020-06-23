import java.util.ArrayList;
import java.util.List;

public class ScheduleMachine {

    private List<Node> tasks;
    private Double timeLasted;

    public ScheduleMachine() {
        this.tasks = new ArrayList<>();
        this.timeLasted = 0.0;
    }

    public void addTask(Node node) {
        tasks.add(node);
    }

    public List<Node> getTasks() {
        return tasks;
    }

    public void setTimeLasted(Double time) {
        timeLasted = time;
    }

    public Double getTimeLasted() {
        return timeLasted;
    }
}
