package test.java.ru.yandex.taskmanager;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.manager.FileBackedTaskManager;
import ru.yandex.taskmanager.manager.HistoryManager;
import ru.yandex.taskmanager.manager.InMemoryHistoryManager;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FileBackedTaskManagerTest {
    private static Path tempFile;
    private FileBackedTaskManager manager;
    private HistoryManager historyManager;


    @BeforeAll
    static void setUpAll() throws IOException {
        // Создаем временный файл перед всеми тестами
        tempFile = Files.createTempFile("tasks", ".csv");
    }

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        manager = new FileBackedTaskManager(historyManager, tempFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Очищаем файл после каждого теста
        Files.write(tempFile, new byte[0]);
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        //Удаляем временный файл после  тестов
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testSaveEmptyManager() {
        // Убедимся, что файл изначально не существует
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить файл перед тестом", e);
        }

        manager.save();

        assertTrue(Files.exists(tempFile), "Файл должен существовать после сохранения");

        try {
            List<String> lines = Files.readAllLines(tempFile);
            assertEquals(1, lines.size(), "Файл должен содержать только заголовок");
            assertEquals("id,type,name,status,description,epic", lines.get(0));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла", e);
        }
    }

    @Test
    void testSaveAndLoadTasks() {
        try {
            Files.write(tempFile, new byte[0]);

            Epic epic = new Epic("Тестовый эпик", "Описание эпика");
            manager.createEpic(epic);

            Subtask subtask = new Subtask(epic.getId(), "Тестовая подзадача", "Описание", TaskStatus.NEW);
            manager.createSubtask(subtask);

            Task task = new Task("Обычная задача", "Описание");
            manager.createTask(task);

            manager.save();

            List<String> lines = Files.readAllLines(tempFile);
            assertTrue(lines.size() > 1, "Файл должен содержать данные");

            FileBackedTaskManager loadedManager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);
            loadedManager.loadFromFile();

            // Проверяем данные
            List<Epic> loadedEpics = loadedManager.getAllEpics();
            List<Subtask> loadedSubtasks = loadedManager.getAllSubTasks();

            // Проверка связей
            Epic loadedEpic = loadedEpics.get(0);
            Subtask loadedSubtask = loadedSubtasks.get(0);

            // Проверяем содержимое задач
            assertEquals(epic.getName(), loadedEpic.getName(), "Название эпика не совпадает");
            assertEquals(subtask.getName(), loadedSubtask.getName(), "Название подзадачи не совпадает");

        } catch (IOException e) {
            throw new RuntimeException("Ошибка ввода-вывода при выполнении теста: " + e.getMessage());
        }
    }

    @Test
    void testLoadFromEmptyFile() {
        try {
            Files.write(tempFile, new byte[0]);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать пустой файл", e);
        }


        try {
            manager.loadFromFile();
        } catch (Exception e) {
            throw new RuntimeException("Загрузка из пустого файла не должна вызывать исключений", e);
        }


        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }

    @Test
    void testSaveAfterEachOperation() {

        Task task = new Task("Задача", "Описание");
        manager.createTask(task);
        assertTrue(Files.exists(tempFile), "Файл должен быть создан после добавления задачи");

        manager.updateTask(task);
        manager.deleteTaskId(task.getId());
    }

    @Test
    void testHistoryAfterLoading() {

        Task task = new Task("Задача", "Описание");
        manager.createTask(task);


        manager.getTaskId(task.getId());


        manager.save();
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(historyManager, tempFile);
        loadedManager.loadFromFile();


        assertEquals(1, loadedManager.getHistory().size());
        assertEquals(task.getId(), loadedManager.getHistory().get(0).getId());
    }
}
