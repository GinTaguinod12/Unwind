public class Task {
    private int taskId;
    private String task;
    private int status;
    long userId;

    public Task(String task, int status, long userId) {
        this.task = task;
        this.status = status;
        this.userId = userId;
    }

    public Task(int taskId, String task, int status, long userId) {
        this.taskId = taskId;
        this.task = task;
        this.status = status;
        this.userId = userId;
    }

    public String getTask() {
        return task;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getStatus() {
        return status;
    }

    public long getUserId() {
        return userId;
    }
}
