package ru.yandex.taskmanager.manager;

import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;

import java.util.List;

public interface TaskManager {
    // методы  Task
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskId(int id);

    void createTask(Task task);

    void updateTask(Task task);

    void deleteTaskId(int id);

    // методы Subtask
    List<Subtask> getAllSubTasks();

    void deleteAllSubTask();

    Subtask getSubTaskId(int id);

    void createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskId(int id);

    // методы Epic
    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpicId(int id);

    void createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpicId(int id);

    // доп методы
    List<Subtask> getSubtasksEpic(int epicId);

    //история просмотра
    List<Task> getHistory();
}