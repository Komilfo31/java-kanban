package test.java.ru.yandex.taskmanager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.manager.FileBackedTaskManager;
import ru.yandex.taskmanager.manager.InMemoryHistoryManager;
import ru.yandex.taskmanager.manager.InMemoryTaskManager;
import ru.yandex.taskmanager.manager.TaskManager;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Epic epic;
    protected Subtask subtask1, subtask2, subtask3;
    protected Task task1, task2;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createTaskManager();

        epic = new Epic(1, "Epic", "Epic description", TaskStatus.NEW);
        taskManager.createEpic(epic);

        subtask1 = new Subtask(2, "Subtask 1", "Desc 1", TaskStatus.NEW, 1,
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 10, 0));
        subtask2 = new Subtask(3, "Subtask 2", "Desc 2", TaskStatus.NEW, 1,
                Duration.ofHours(2), LocalDateTime.of(2023, 1, 1, 12, 0));
        subtask3 = new Subtask(4, "Subtask 3", "Desc 3", TaskStatus.DONE, 1,
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 15, 0));

        task1 = new Task(5, "Task 1", "Desc", TaskStatus.NEW,
                Duration.ofHours(2), LocalDateTime.of(2023, 1, 2, 10, 0));
        task2 = new Task(6, "Task 2", "Desc", TaskStatus.IN_PROGRESS,
                Duration.ofHours(1), LocalDateTime.of(2023, 1, 2, 12, 0));
    }

    /* Общие тесты для TaskManager */
    @Test
    void testCreateAndGetTask() {
        taskManager.createTask(task1);
        Task savedTask = taskManager.getTaskId(task1.getId());
        assertEquals(task1, savedTask, "Задачи не совпадают");
    }

    @Test
    void testCreateAndGetEpic() {
        Epic newEpic = new Epic(7, "New Epic", "Desc", TaskStatus.NEW);
        taskManager.createEpic(newEpic);
        Epic savedEpic = taskManager.getEpicId(newEpic.getId());
        assertEquals(newEpic, savedEpic, "Эпики не совпадают");
    }


    /* Тесты временных интервалов */
    @Nested
    class TimeIntervalTests {
        @Test
        void testTasksTimeOverlap() {
            Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW,
                    Duration.ofHours(2), LocalDateTime.of(2023, 1, 1, 10, 0));
            taskManager.createTask(task1);


            Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW,
                    Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 11, 0));


            boolean hasOverlap = isTasksOverlap(task1, task2);
            assertTrue(hasOverlap, "Задачи должны пересекаться по времени");
        }

        @Test
        void testTasksTimeNoOverlap() {

            Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW,
                    Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 10, 0));
            taskManager.createTask(task1);


            Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW,
                    Duration.ofHours(1), LocalDateTime.of(2023, 1, 1, 12, 0));


            boolean hasOverlap = isTasksOverlap(task1, task2);
            assertFalse(hasOverlap, "Задачи НЕ должны пересекаться по времени");
        }


        private boolean isTasksOverlap(Task task1, Task task2) {
            LocalDateTime start1 = task1.getStartTime();
            LocalDateTime end1 = task1.getEndTime();
            LocalDateTime start2 = task2.getStartTime();
            LocalDateTime end2 = task2.getEndTime();


            return start1.isBefore(end2) && start2.isBefore(end1);
        }
    }

    /* Тесты для HistoryManager */
    @Nested
    class HistoryTests {
        @Test
        void testEmptyHistory() {
            assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пустой");
        }

        @Test
        void testHistoryWithDuplicates() {
            taskManager.createTask(task1);
            taskManager.getTaskId(task1.getId());
            taskManager.getTaskId(task1.getId());

            assertEquals(1, taskManager.getHistory().size(),
                    "История не должна содержать дубликатов");
        }

        @Test
        void testHistoryOrder() {
            taskManager.createTask(task1);
            taskManager.createEpic(epic);

            taskManager.getTaskId(task1.getId());
            taskManager.getEpicId(epic.getId());

            List<Task> history = taskManager.getHistory();
            assertEquals(2, history.size(), "Неверное количество задач в истории");
            assertEquals(task1, history.get(0), "Порядок задач в истории нарушен");
            assertEquals(epic, history.get(1), "Порядок задач в истории нарушен");
        }

        @Test
        void testRemoveFromHistory() {
            taskManager.createTask(task1);
            taskManager.createTask(task2);
            taskManager.createEpic(epic);

            taskManager.getTaskId(task1.getId());
            taskManager.getTaskId(task2.getId());
            taskManager.getEpicId(epic.getId());

            // Удаление из начала
            taskManager.deleteTaskId(task1.getId());
            assertEquals(3, taskManager.getHistory().size(), "Не удалилась задача из начала");

        }
    }
}

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }
}


class FileBackedTaskManagerTests extends TaskManagerTest<FileBackedTaskManager> {
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("tasks", ".csv");
        super.setUp();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);
    }
}