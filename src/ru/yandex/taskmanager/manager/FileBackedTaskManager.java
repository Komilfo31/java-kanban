package ru.yandex.taskmanager.manager;


import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private Path file = Paths.get("tasks.csv");

    public FileBackedTaskManager(HistoryManager historyManager, Path file) {
        super(historyManager);
        this.file = file;
    }

    public class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file.toFile())) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Subtask subtask : getAllSubTasks()) {
                writer.write(toString(subtask) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл", e);
        }
    }

    private String toString(Task task) {

        if (task == null || task.getStatus() == null) {
            return "";
        }


        String type = getTaskType(task);


        String result = task.getId() + "," +
                type + "," +
                task.getName() + "," +
                task.getStatus().name() + "," +
                task.getDescription();


        if (task instanceof Subtask) {
            //Subtask subtask = (Subtask) task;
            result += "," + ((Subtask) task).getEpicId(); //запись epicid в файл
        } else {
            result += ",";
        }

        return result;
    }

    private String getTaskType(Task task) {
        if (task instanceof Subtask) return "SUBTASK";
        if (task instanceof Epic) return "EPIC";
        return "TASK";
    }

    private static Task fromString(String value) {

        String[] parts = value.split(",");
        try {
            TypeTask type = TypeTask.valueOf(parts[1]);
            int id = Integer.parseInt(parts[0]);
            String name = parts[2];
            TaskStatus status = TaskStatus.valueOf(parts[3]);
            String description = parts[4];

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status);
                case SUBTASK:
                    int epicId = Integer.parseInt(parts[5].trim());
                    Subtask subtask = new Subtask(id, name, description, status);
                    subtask.setEpicId(epicId); // устанавливаю epicId после создания
                    return subtask;
                case EPIC:
                    return new Epic(id, name, description, status);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ошибка разбора строки: " + value, e);
        }
    }

    public void loadFromFile() {
        if (!Files.exists(file)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return;
            }

            // загружаем все эпики
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                try {
                    Task task = fromString(line);
                    if (task instanceof Epic) {
                        createEpic((Epic) task);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка в строке " + (i + 1) + ": " + e.getMessage());
                }
            }

            // загружаем подзадачи и обычные задачи
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                try {
                    Task task = fromString(line);
                    if (task == null) continue;

                    if (task instanceof Subtask) {
                        createSubtask((Subtask) task);
                    } else if (!(task instanceof Epic)) {  // Обычные задачи
                        createTask(task);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Ошибка в строке " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки файла: " + e.getMessage());
        }
    }


    @Override
    public List<Task> getAllTasks() {
        List<Task> task = super.getAllTasks();
        return task;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Task getTaskId(int id) {
        return super.getTaskId(id);
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskId(int id) {
        super.deleteTaskId(id);
        save();
    }

    @Override
    public List<Subtask> getAllSubTasks() {
        return super.getAllSubTasks();
    }

    @Override
    public void deleteAllSubTask() {
        super.deleteAllSubTask();
        save();
    }

    @Override
    public Subtask getSubTaskId(int id) {
        return super.getSubTaskId(id);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskId(int id) {
        super.deleteSubtaskId(id);
        save();
    }

    @Override
    public List<Epic> getAllEpics() {
        return super.getAllEpics();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Epic getEpicId(int id) {
        return super.getEpicId(id);
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicId(int id) {
        super.deleteEpicId(id);
        save();
    }

    @Override
    public List<Subtask> getSubtasksEpic(int epicId) {
        return super.getSubtasksEpic(epicId);
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }


}
