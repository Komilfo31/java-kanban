import ru.yandex.taskmanager.manager.NotFoundException;
import ru.yandex.taskmanager.manager.TaskManager;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;
import ru.yandex.taskmanager.util.Managers;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws NotFoundException {
        TaskManager taskManager = Managers.getDefault();

        // задачи с временными параметрами
        Task task1 = new Task(1, "Помыть посуду", "Сделать до вечера", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 4, 1, 10, 0));

        Task task2 = new Task(2, "Купить продукты", "Молоко, хлеб, яйца", TaskStatus.NEW,
                Duration.ofHours(1), LocalDateTime.of(2025, 4, 1, 11, 0));


        Epic epic1 = new Epic(3, "Переезд", "Организовать переезд в новую квартиру", TaskStatus.NEW);
        taskManager.createEpic(epic1);

        //подзадачи с временными параметрами
        Subtask subtask1 = new Subtask(
                4,
                "Упаковать вещи",
                "Коробки, скотч, маркер",
                TaskStatus.NEW,
                epic1.getId(),
                Duration.ofHours(3),
                LocalDateTime.of(2025, 4, 2, 9, 0)
        );

        Subtask subtask2 = new Subtask(
                5,
                "Нанять грузчиков",
                "Найти через приложение",
                TaskStatus.NEW,
                epic1.getId(),
                Duration.ofHours(1),
                LocalDateTime.of(2025, 4, 2, 13, 0)
        );


        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);


        System.out.println("Все задачи:");
        printAllTasks(taskManager);

        //Проверка истории просмотров
        System.out.println("\nПросматриваем задачи для формирования истории:");
        taskManager.getTaskId(task1.getId());
        taskManager.getEpicId(epic1.getId());
        taskManager.getSubTaskId(subtask1.getId());
        taskManager.getTaskId(task2.getId());
        taskManager.getSubTaskId(subtask2.getId());

        System.out.println("\nИстория просмотров:");
        taskManager.getHistory().forEach(task ->
                System.out.println(task.getName() + " (ID: " + task.getId() + ")")
        );

        //приоритеты
        System.out.println("\nЗадачи в порядке приоритета:");
        taskManager.getPrioritizedTasks().forEach(task ->
                System.out.println(task.getStartTime() + " - " + task.getName())
        );

        // статус эпика
        System.out.println("\nСтатус эпика 'Переезд': " + epic1.getStatus());
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nОбычные задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("\nЭпики с подзадачами:");
        manager.getAllEpics().forEach(epic -> {
            System.out.println(epic);
            manager.getSubtasksEpic(epic.getId()).forEach(subtask ->
                    System.out.println("  ↳ " + subtask)
            );
        });

        System.out.println("\nВсе подзадачи:");
        manager.getAllSubTasks().forEach(System.out::println);
    }
}