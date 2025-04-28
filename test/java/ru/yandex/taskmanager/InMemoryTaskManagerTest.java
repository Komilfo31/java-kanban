package ru.yandex.taskmanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.manager.HistoryManager;
import ru.yandex.taskmanager.manager.InMemoryHistoryManager;
import ru.yandex.taskmanager.manager.TaskManager;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;
import ru.yandex.taskmanager.util.Managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class InMemoryTaskManagerTest {
    private HistoryManager manager;
    private Task task1;
    private Task task2;

    @Test
    void testAddAndFindTasks() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);
        Epic epic = new Epic(2, "Epic 1", "Description 1", TaskStatus.NEW);
        Subtask subtask = new Subtask(3, "model.Subtask 1", "Description 1", TaskStatus.NEW, 2);

        taskManager.createTask(task);
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask);

        assertEquals(task, taskManager.getTaskId(1), "Задача должна быть найдена по id");
        assertEquals(epic, taskManager.getEpicId(2), "Эпик должен быть найден по id");
        assertEquals(subtask, taskManager.getSubTaskId(3), "Подзадача должна быть найдена по id");

    }

    @Test
    void testTaskIdsDoNotConflict() {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description 2", TaskStatus.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "ID задач не должны конфликтовать");
    }

    @Test
    void testTaskImmutabilityWhenAdded() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);

        taskManager.createTask(task);
        Task savedTask = taskManager.getTaskId(1);

        assertEquals(task.getName(), savedTask.getName(), "Имя задачи не меняется");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи не меняется");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задачи не меняется");
    }

    @Test
    void testTaskDeletion() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);

        taskManager.createTask(task);
        taskManager.deleteTaskId(1);

        Assertions.assertNull(taskManager.getTaskId(1), "Задача должна быть удалена");
    }


    @BeforeEach
    void setUp() {
        manager = new InMemoryHistoryManager();
        task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);
    }

    @Test
    void getHistoryShouldReturnEmptyListWhenNoTasks() {
        Assertions.assertTrue(manager.getHistory().isEmpty(), "История не пустая");
    }


    @Test
    void removeShouldWorkWithEmptyHistory() {
        manager.remove(1);
        Assertions.assertTrue(manager.getHistory().isEmpty());
    }


    @Test
    void historyShouldNotContainDuplicatesAfterMultipleAdds() {
        manager.add(task1);
        manager.add(task1);
        manager.add(task1);

        assertEquals(1, manager.getHistory().size(), "Дубликаты не удаляются");
    }
}