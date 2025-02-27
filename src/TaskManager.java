import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    // методы  Task
    ArrayList<Task> getAllTasks();
    void deleteAllTasks();
    Task getTaskId(int id);
    void createTask(Task task);
    void updateTask(Task task);
    void deleteTaskId(int id);

    // методы  Subtask
    ArrayList<Subtask> getAllSubTasks();
    void deleteAllSubTask();
    Subtask getSubTaskId(int id);
    void createSubtask(Subtask subtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtaskId(int id);

    // методы Epic
    ArrayList<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpicId(int id);
    void createEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpicId(int id);

    // доп методы
    ArrayList<Subtask> getSubtasksEpic(int epicId);
    //история просмотра
    List<Task> getHistory();
}