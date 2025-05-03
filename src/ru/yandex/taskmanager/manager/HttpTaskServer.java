package ru.yandex.taskmanager.manager;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.util.LocalDateTimeAdapter;
import ru.yandex.taskmanager.util.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


public class HttpTaskServer extends BaseHttpHandler {
    private final HttpServer server;
    private final TaskManager taskManager;
    private final int port;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault(), 8080);
    }

    public HttpTaskServer(TaskManager taskManager, int port) throws IOException {
        super(new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new LocalDateTimeAdapter.DurationAdapter())
                .create());
        this.taskManager = taskManager;
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/tasks", this::handleTasks);
        server.createContext("/tasks/", this::handleTaskById);
        server.createContext("/subtasks", this::handleSubtasks);
        server.createContext("/subtasks/", this::handleSubtaskById);
        server.createContext("/epics", this::handleEpics);
        server.createContext("/epics/", this::handleEpicById);
        server.createContext("/epics/subtasks", this::handleEpicSubtasks);
        server.createContext("/history", this::handleHistory);
        server.createContext("/prioritized", this::handlePrioritized);
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + port);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    //Обработчики
    private void handleTasks(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            switch (ex.getRequestMethod()) {
                case "GET":
                    sendJson(exchange, taskManager.getAllTasks(), 200);
                    break;
                case "POST":
                    Task task = parseRequestBody(exchange, Task.class);
                    if (taskManager.getPrioritizedTasks().stream()
                            .anyMatch(t -> !t.equals(task) && isTasksOverlap(t, task))) {
                        throw new TaskOverlapException("Task overlaps with existing tasks");
                    }
                    if (task.getId() == 0) {
                        taskManager.createTask(task);
                        sendText(exchange, "Задача создана", 201);
                    } else {
                        taskManager.updateTask(task);
                        sendText(exchange, "Задача обновлена", 200);
                    }
                    break;
                case "DELETE":
                    taskManager.deleteAllTasks();
                    sendText(exchange, "Все задачи удалены", 200);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        });
    }

    private void handleTaskById(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            int taskId = parseIdFromPath(exchange)
                    .orElseThrow(() -> new NotFoundException("Task not found"));

            switch (exchange.getRequestMethod()) {
                case "GET":
                    Task task = taskManager.getTaskId(taskId);
                    if (task == null) {
                        throw new NotFoundException("Task not found");
                    }
                    sendJson(exchange, task, 200);
                    break;
                case "POST":
                    Task updatedTask = parseRequestBody(exchange, Task.class);
                    if (updatedTask.getId() != 0 && updatedTask.getId() != taskId) {
                        throw new IllegalArgumentException("ID in path and body mismatch");
                    }
                    if (taskManager.getPrioritizedTasks().stream()
                            .anyMatch(t -> !t.equals(updatedTask) && isTasksOverlap(t, updatedTask))) {
                        throw new TaskOverlapException("Task overlaps with existing tasks");
                    }
                    updatedTask.setId(taskId);
                    taskManager.updateTask(updatedTask);
                    sendText(exchange, "Задача обновлена", 200);
                    break;
                case "DELETE":
                    taskManager.deleteTaskId(taskId);
                    sendText(exchange, "Задача удалена", 200);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        });
    }


    private void handleSubtasks(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            switch (ex.getRequestMethod()) {
                case "GET":
                    sendJson(exchange, taskManager.getAllSubTasks(), 200);
                    break;
                case "POST":
                    Subtask subtask = parseRequestBody(exchange, Subtask.class);
                    if (taskManager.getPrioritizedTasks().stream()
                            .anyMatch(t -> !t.equals(subtask) && isTasksOverlap(t, subtask))) {
                        throw new TaskOverlapException("Subtask overlaps with existing tasks");
                    }
                    if (subtask.getId() == 0) {
                        taskManager.createSubtask(subtask);
                        sendText(exchange, "Подзадача создана", 201);
                    } else {
                        taskManager.updateSubtask(subtask);
                        sendText(exchange, "Подзадача обновлена", 200);
                    }
                    break;
                case "DELETE":
                    taskManager.deleteAllSubTask();
                    sendText(exchange, "Все подзадачи удалены", 200);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        });
    }

    private void handleSubtaskById(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            int subtaskId = parseIdFromPath(exchange)
                    .orElseThrow(() -> new NotFoundException("Subtask not found"));

            switch (exchange.getRequestMethod()) {
                case "GET":
                    Subtask subtask = taskManager.getSubTaskId(subtaskId);
                    sendJson(exchange, subtask, 200);
                    break;
                case "POST":
                    Subtask updatedSubtask = parseRequestBody(exchange, Subtask.class);
                    if (updatedSubtask.getId() != 0 && updatedSubtask.getId() != subtaskId) {
                        throw new IllegalArgumentException("ID in path and body mismatch");
                    }
                    if (taskManager.getPrioritizedTasks().stream()
                            .anyMatch(t -> !t.equals(updatedSubtask) && isTasksOverlap(t, updatedSubtask))) {
                        throw new TaskOverlapException("Subtask overlaps with existing tasks");
                    }
                    updatedSubtask.setId(subtaskId);
                    taskManager.updateSubtask(updatedSubtask);
                    sendText(exchange, "Подзадача обновлена", 200);
                    break;
                case "DELETE":
                    taskManager.deleteSubtaskId(subtaskId);
                    sendText(exchange, "Подзадача удалена", 200);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        });
    }


    private void handleEpics(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            switch (ex.getRequestMethod()) {
                case "GET":
                    sendJson(exchange, taskManager.getAllEpics(), 200);
                    break;
                case "POST":
                    Epic epic = parseRequestBody(exchange, Epic.class);
                    if (epic.getId() == 0) {
                        taskManager.createEpic(epic);
                        sendText(exchange, "Эпик создан", 201);
                    } else {
                        taskManager.updateEpic(epic);
                        sendText(exchange, "Эпик обновлен", 200);
                    }
                    break;
                case "DELETE":
                    taskManager.deleteAllEpics();
                    sendText(exchange, "Все эпики удалены", 200);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        });
    }

    private void handleEpicById(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            int epicId = parseIdFromPath(exchange)
                    .orElseThrow(() -> new NotFoundException("Epic not found"));

            switch (exchange.getRequestMethod()) {
                case "GET":
                    Epic epic = taskManager.getEpicId(epicId);
                    sendJson(exchange, epic, 200);
                    break;
                case "POST":
                    Epic updatedEpic = parseRequestBody(exchange, Epic.class);
                    if (updatedEpic.getId() != 0 && updatedEpic.getId() != epicId) {
                        throw new IllegalArgumentException("ID in path and body mismatch");
                    }
                    updatedEpic.setId(epicId);
                    taskManager.updateEpic(updatedEpic);
                    sendText(exchange, "Эпик обновлен", 200);
                    break;
                case "DELETE":
                    taskManager.deleteEpicId(epicId);
                    sendText(exchange, "Эпик удален", 200);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        });
    }

    //доп обработчики
    private void handleEpicSubtasks(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("epicId=")) {
                throw new NotFoundException("Epic ID not specified in query parameters");
            }

            try {
                int epicId = Integer.parseInt(query.substring("epicId=".length()));
                List<Subtask> subtasks = taskManager.getSubtasksEpic(epicId);
                sendJson(exchange, subtasks, 200);
            } catch (NumberFormatException e) {
                throw new NotFoundException("Invalid epic ID format");
            }
        });
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            sendJson(exchange, taskManager.getHistory(), 200);
        });
    }

    private void handlePrioritized(HttpExchange exchange) throws IOException {
        handleRequest(exchange, (ex) -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            sendJson(exchange, taskManager.getPrioritizedTasks(), 200);
        });
    }


    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }
        return task1.getStartTime().isBefore(task2.getEndTime()) &&
                task2.getStartTime().isBefore(task1.getEndTime());
    }

    //для тестов
    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new LocalDateTimeAdapter.DurationAdapter())
                .create();
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nЗавершение работы сервера...");
                server.stop();
            }));

        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера:");

        }
    }
}