package ru.yandex.taskmanager.manager;

import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;


public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>(); // задачи
    private final Map<Integer, Subtask> subtasks = new HashMap<>(); // подзадачи
    private final Map<Integer, Epic> epics = new HashMap<>(); // эпики
    private int nextId = 1; // id-шники

    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return nextId++;
    }

    //проверка пересечения двух задач
    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    //пересечения задачи с другими
    private boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null) {
            return false;
        }

        return getPrioritizedTasks().stream()
                .filter(task -> !task.equals(newTask))
                .anyMatch(existingTask -> isTasksOverlap(newTask, existingTask));
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        //компаратор для сортировки по startTime
        Comparator<Task> taskComparator = Comparator.comparing(
                Task::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        //все задачи в один список
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(subtasks.values());

        // Сорт и фильтр задачи с null startTime
        return allTasks.stream()
                .filter(task -> task.getStartTime() != null)
                .sorted(taskComparator)
                .collect(Collectors.toList());
    }

    // Методы для Task
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskId(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void deleteTaskId(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    // Методы для Subtask
    @Override
    public List<Subtask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubTask() {
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        });
    }

    @Override
    public Subtask getSubTaskId(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void createSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void deleteSubtaskId(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    // Методы для Epic
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicId(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void deleteEpicId(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            epic.getSubtaskIds().forEach(subtasks::remove);
        }
        historyManager.remove(id);
    }

    // Дополнительные методы
    @Override
    public List<Subtask> getSubtasksEpic(int epicId) {
        return epics.getOrDefault(epicId, new Epic(0, "", "", TaskStatus.NEW))
                .getSubtaskIds().stream()
                .map(subtasks::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> epicSubtasks = getSubtasksEpic(epicId);

        // Если нет подзадач - статус NEW
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : epicSubtasks) {
            TaskStatus status = subtask.getStatus();

            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }


            if (status == TaskStatus.IN_PROGRESS) {
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}