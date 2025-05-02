package ru.yandex.taskmanager.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status,
                   int epicId, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }


    public int getEpicId() {

        return epicId;
    }

    public void setEpicId(int epicId) {

        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}
