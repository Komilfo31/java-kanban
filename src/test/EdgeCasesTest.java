package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.manager.HistoryManager;
import ru.yandex.taskmanager.manager.InMemoryHistoryManager;
import ru.yandex.taskmanager.manager.InMemoryTaskManager;
import ru.yandex.taskmanager.manager.TaskManager;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class EdgeCasesTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
    }

    @Test
    void getNonExistentTaskShouldReturnNull() {
        assertNull(taskManager.getTaskId(999), "Должен возвращать null для несуществующей задачи");
        assertNull(taskManager.getEpicId(999), "Должен возвращать null для несуществующего эпика");
        assertNull(taskManager.getSubTaskId(999), "Должен возвращать null для несуществующей подзадачи");
    }

    @Test
    void updateNonExistentTaskShouldNotFail() {
        Task task = new Task("Task", "Description");
        Epic epic = new Epic("Epic", "Description");
        Subtask subtask = new Subtask("Subtask", "Description");

        // Не должно бросать исключений
        taskManager.updateTask(task);
        taskManager.updateEpic(epic);
        taskManager.updateSubtask(subtask);

        assertNull(taskManager.getTaskId(task.getId()), "Задача не должна добавляться через update");
    }

    @Test
    void deleteTwiceShouldNotFail() {
        Task task = new Task("Task", "Description");
        taskManager.createTask(task);
        int taskId = task.getId();

        taskManager.deleteTaskId(taskId);
        taskManager.deleteTaskId(taskId); // повторное удаление не должно ошибок

        assertNull(taskManager.getTaskId(taskId), "Задача должна остаться удаленной");
    }

    @Test
    void epicShouldUpdateStatusWhenSubtaskRemoved() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Subtask", "Description");
        subtask.setEpicId(epicId);
        subtask.setStatus(TaskStatus.DONE);
        taskManager.createSubtask(subtask);

        taskManager.deleteSubtaskId(subtask.getId());
        assertEquals(TaskStatus.NEW, taskManager.getEpicId(epicId).getStatus(),
                "Статус эпика должен сброситься после удаления подзадачи");
    }

    @Test
    void subtaskShouldBeRemovedAfterEpicDeletion() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask subtask = new Subtask("Subtask", "Description");
        subtask.setEpicId(epicId);
        taskManager.createSubtask(subtask);
        int subtaskId = subtask.getId();

        taskManager.deleteEpicId(epicId);
        assertNull(taskManager.getSubTaskId(subtaskId),
                "Подзадача должна быть удалена при удалении эпика");
    }

    @Test
    void taskFieldsModificationShouldAffectManager() {
        Task task = new Task("Original", "Original");
        taskManager.createTask(task);
        int taskId = task.getId();

        task.setName("Modified");
        task.setDescription("Modified");
        taskManager.updateTask(task);

        Task storedTask = taskManager.getTaskId(taskId);
        assertEquals("Modified", storedTask.getName(), "Имя должно обновиться");
        assertEquals("Modified", storedTask.getDescription(), "Описание должно обновиться");
    }

    @Test
    void emptyEpicShouldHaveNewStatus() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus(),
                "Пустой эпик должен иметь статус NEW");
    }

    @Test
    void deletingAllTasksShouldClearHistory() {
        Task task = new Task("Task", "Description");
        taskManager.createTask(task);
        taskManager.getTaskId(task.getId());

        taskManager.deleteAllTasks();
        Assertions.assertTrue(taskManager.getHistory().isEmpty(),
                "История должна очиститься после удаления всех задач");
    }
}