package ru.yandex.taskmanager.manager;

public class TaskOverlapException extends Exception {
    public TaskOverlapException(String message) {
        super(message);
    }
}
