package pl.czystkaplugin;

public final class PendingClear {

    private final String targetName;
    private final String targetUuid;
    private final String executorName;
    private final String executorUuid;
    private final long requestedAt;

    public PendingClear(String targetName, String targetUuid, String executorName, String executorUuid, long requestedAt) {
        this.targetName = targetName;
        this.targetUuid = targetUuid;
        this.executorName = executorName;
        this.executorUuid = executorUuid;
        this.requestedAt = requestedAt;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetUuid() {
        return targetUuid;
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
}