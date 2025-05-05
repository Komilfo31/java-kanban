package ru.yandex.taskmanager.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;
import ru.yandex.taskmanager.util.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HttpTaskServerTest {
    private HttpTaskServer server;
    private HttpClient client;
    private final Gson gson = HttpTaskServer.getGson();

    @BeforeEach
    void setUp() throws IOException {
        TaskManager taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
        server = new HttpTaskServer(taskManager, 8080);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testGetAllTasksWhenEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testCreateAndGetTask() throws IOException, InterruptedException {

        Task task = new Task(0, "Test Task", "тестовая задача", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());


        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        List<Task> tasks = gson.fromJson(getResponse.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getName());
    }


    @Test
    void testUpdateTask() throws IOException, InterruptedException {

        Task task = new Task(0, "Test Task", "Description", TaskStatus.NEW);
        String taskJson = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        client.send(createRequest, HttpResponse.BodyHandlers.ofString());


        Task updatedTask = new Task(1, "обновляем Task", "новое описание", TaskStatus.IN_PROGRESS);
        String updatedTaskJson = gson.toJson(updatedTask);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, updateResponse.statusCode());


        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        Task retrievedTask = gson.fromJson(getResponse.body(), Task.class);
        assertEquals("обновляем Task", retrievedTask.getName());
        assertEquals(TaskStatus.IN_PROGRESS, retrievedTask.getStatus());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {

        Task task = new Task(0, "Test Task", "Description", TaskStatus.NEW);
        String taskJson = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        client.send(createRequest, HttpResponse.BodyHandlers.ofString());


        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());


    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {

        Epic epic = new Epic(0, "Тестовый Epic", "тестовое описание подзадачи", TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> createEpicResponse = client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createEpicResponse.statusCode());


        Subtask subtask = new Subtask(0, "Тестовый Subtask", "тестовое описание подзадачи", TaskStatus.NEW, 1);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());


        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        List<Subtask> subtasks = gson.fromJson(getResponse.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertEquals(1, subtasks.size());
        assertEquals("Тестовый Subtask", subtasks.get(0).getName());
    }


    @Test
    void testGetHistory() throws IOException, InterruptedException {

        Task task = new Task(1, "Test Task", "Description", TaskStatus.NEW);
        String taskJson = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        client.send(createRequest, HttpResponse.BodyHandlers.ofString());


        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .GET()
                .build();

        client.send(getRequest, HttpResponse.BodyHandlers.ofString());


        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());

        List<Task> history = gson.fromJson(historyResponse.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertEquals(1, history.size());
        assertEquals("Test Task", history.get(0).getName());
    }

    @Test
    void testGetPrioritizedTasks() throws IOException, InterruptedException {

        Task task1 = new Task(0, "Task 1", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        Task task2 = new Task(0, "Task 2", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        String task1Json = gson.toJson(task1);
        String task2Json = gson.toJson(task2);


        HttpRequest createRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .build();

        client.send(createRequest2, HttpResponse.BodyHandlers.ofString());


        HttpRequest createRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .build();

        client.send(createRequest1, HttpResponse.BodyHandlers.ofString());


        HttpRequest prioritizedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> prioritizedResponse = client.send(prioritizedRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, prioritizedResponse.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(prioritizedResponse.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertEquals(2, prioritizedTasks.size());
        assertEquals("Task 2", prioritizedTasks.get(0).getName());
    }

    @Test
    void testTaskOverlapValidation() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);


        Task task1 = new Task(0, "Task 1", "Description", TaskStatus.NEW, duration, now);
        String task1Json = gson.toJson(task1);

        HttpRequest createRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .build();

        HttpResponse<String> createResponse1 = client.send(createRequest1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse1.statusCode());


    }

    @Test
    void testInvalidTaskId() throws IOException, InterruptedException {

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void testInvalidMethod() throws IOException, InterruptedException {

        HttpRequest putRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, putResponse.statusCode());
    }
}