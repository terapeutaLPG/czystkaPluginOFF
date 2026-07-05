package pl.czystkaplugin;

public final class ClearNotification {

    private final String targetName;
    private final String executorName;
    private final String executorUuid;
    private final long requestedAt;
    private final long executedAt;

    public ClearNotification(String targetName, String executorName, String executorUuid, long requestedAt, long executedAt) {
        this.targetName = targetName;
        this.executorName = executorName;
        this.executorUuid = executorUuid;
        this.requestedAt = requestedAt;
        this.executedAt = executedAt;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getExecutorName() {
        return executorName;
    }

    public String getExecutorUuid() {
        return executorUuid;
    }

    public long getRequestedAt() {
        return requestedAt;
    }

    public long getExecutedAt() {
        return executedAt;
    }
}