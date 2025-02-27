import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    @Test
    void testAddAndFindTasks() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);
        Epic epic = new Epic(2, "Epic 1", "Description 1", TaskStatus.NEW);
        Subtask subtask = new Subtask(3, "Subtask 1", "Description 1", TaskStatus.NEW, 2);

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
        Task task2 = new Task("Task 2", "Description 2");

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

        assertEquals(task.getName(), savedTask.getName(), "Имя задачи не должно измениться");
        assertEquals(task.getDescription(), savedTask.getDescription(), "Описание задачи не должно измениться");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статус задачи не должен измениться");
    }

    @Test
    void testTaskDeletion() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);

        taskManager.createTask(task);
        taskManager.deleteTaskId(1);

        assertNull(taskManager.getTaskId(1), "Задача должна быть удалена");
    }
}