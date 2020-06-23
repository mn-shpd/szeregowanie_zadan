public class TaskOnSchedule {

    private int taskNumber;
    private Double taskTime;
    private int machineNumber;

    public TaskOnSchedule(int taskNumber, Double taskTime, int machineNumber) {
        this.taskNumber = taskNumber;
        this.taskTime = taskTime;
        this.machineNumber = machineNumber;
    }

    public int getTaskNumber() {
        return taskNumber;
    }

    public Double getTaskTime() {
        return taskTime;
    }

    public int getMachineNumber() {
        return machineNumber;
    }
}
