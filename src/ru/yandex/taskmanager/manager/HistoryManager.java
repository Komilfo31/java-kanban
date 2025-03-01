package ru.yandex.taskmanager.manager;

import ru.yandex.taskmanager.model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
}
