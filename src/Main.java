public class Main {

    public static void main(String[] args) {
       TaskManager manager = new TaskManager();

        Task task1 = new Task(0, "Задача 1: Найти новую квартиру", "Поиск квартиры через агентство", TaskStatus.NEW);
        Task task2 = new Task(0, "Задача 2: Упаковать вещи", "Упаковать вещи в коробки", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic(0, "Эпик 1: Организация переезда", "Подготовка к переезду в новую квартиру", TaskStatus.NEW);
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Подзадача 1: Заказать грузовик", "Найти и заказать грузовик для переезда", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(0, "Подзадача 2: Собрать документы", "Подготовить документы для переезда", TaskStatus.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic epic2 = new Epic(0, "Эпик 2: Уборка старой квартиры", "Подготовка старой квартиры к сдаче", TaskStatus.NEW);
        manager.createEpic(epic2);

        Subtask subtask3 = new Subtask(0, "Подзадача 3: Вывезти мусор", "Утилизировать ненужные вещи", TaskStatus.NEW, epic2.getId());
        manager.createSubtask(subtask3);


        System.out.println("Все задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Все подзадачи:");
        for (Subtask subtask : manager.getAllSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("Все эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }

        //меняю статусы
        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask3);


        //обновленые статусы
        System.out.println("Обновленные задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Обновленные подзадачи:");
        for (Subtask subtask : manager.getAllSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("Обновленные эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }


        manager.deleteTaskId(task1.getId());
        manager.deleteEpicId(epic1.getId());

    }
}