package test.java.ru.yandex.taskmanager;


import org.junit.jupiter.api.*;
import ru.yandex.taskmanager.manager.FileBackedTaskManager;
import ru.yandex.taskmanager.manager.HistoryManager;
import ru.yandex.taskmanager.manager.InMemoryHistoryManager;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FileBackedTaskManagerTest {
    private static Path tempFile;
    private FileBackedTaskManager manager;
    private HistoryManager historyManager;

    @BeforeAll
    static void setUpAll() throws IOException {
        tempFile = Files.createTempFile("tasks", ".csv");
    }

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        manager = new FileBackedTaskManager(historyManager, tempFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.write(tempFile, new byte[0]);
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testSaveEmptyManager() throws IOException {
        Files.deleteIfExists(tempFile);
        manager.save();

        assertTrue(Files.exists(tempFile), "Файл должен существовать после сохранения");

        List<String> lines = Files.readAllLines(tempFile);
        assertEquals(1, lines.size(), "Файл должен содержать только заголовок");
        assertEquals("id,type,name,status,description,epic,duration,startTime,endTime", lines.get(0));
    }

    @Test
    void testSaveAndLoadTasks() throws IOException {
        Files.write(tempFile, new byte[0]);

        Epic epic = new Epic(1, "Тестовый эпик", "Описание эпика", TaskStatus.NEW,
                Duration.ZERO, LocalDateTime.now(), null);
        manager.createEpic(epic);

        Task task = new Task(
                3, "Обычная задача", "Описание", TaskStatus.NEW,
                Duration.ofMinutes(30),
                LocalDateTime.now().plusHours(1)
        );
        manager.createTask(task);

        manager.save();

        List<String> lines = Files.readAllLines(tempFile);
        assertTrue(lines.size() > 1, "Файл должен содержать данные");

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);
        loadedManager.loadFromFile();

        List<Epic> loadedEpics = loadedManager.getAllEpics();


        Epic loadedEpic = loadedEpics.get(0);


        assertEquals(epic.getName(), loadedEpic.getName(), "Название эпика не совпадает");

    }

    @Test
    void testLoadFromEmptyFile() throws IOException {
        Files.write(tempFile, new byte[0]);
        manager.loadFromFile();

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }

    @Test
    void testSaveAfterEachOperation() throws IOException {
        Task task = new Task(1, "Задача", "Описание", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.now());
        manager.createTask(task);
        assertTrue(Files.exists(tempFile), "Файл должен быть создан после добавления задачи");

        manager.updateTask(task);
        manager.deleteTaskId(task.getId());
    }

    @Test
    void testHistoryAfterLoading() throws IOException {
        Task task = new Task(1, "Задача", "Описание", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.now());
        manager.createTask(task);

        manager.getTaskId(task.getId());
        manager.save();

        FileBackedTaskManager loadedManager = new FileBackedTaskManager(historyManager, tempFile);
        loadedManager.loadFromFile();

        assertEquals(1, loadedManager.getHistory().size());
        assertEquals(task.getId(), loadedManager.getHistory().get(0).getId());
    }
}
