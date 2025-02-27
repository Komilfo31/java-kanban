public class Main {

 public static void main(String[] args) {

  TaskManager taskManager = Managers.getDefault();


  Task task1 = new Task("Помыть посуду", "Сделать до вечера");
  Task task2 = new Task("Купить продукты", "Молоко, хлеб, яйца");


  Epic epic1 = new Epic("Переезд", "Организовать переезд в новую квартиру");
  Subtask subtask1 = new Subtask("Упаковать вещи", "Коробки, скотч, маркер", epic1.getId());
  Subtask subtask2 = new Subtask("Нанять грузчиков", "Найти через приложение", epic1.getId());


  taskManager.createTask(task1);
  taskManager.createTask(task2);
  taskManager.createEpic(epic1);
  taskManager.createSubtask(subtask1);
  taskManager.createSubtask(subtask2);


  //printAllTasks(taskManager);


  System.out.println("Просматриваем задачу №1:");
  taskManager.getTaskId(task1.getId());
  printAllTasks(taskManager);

  System.out.println("Просматриваем эпик №1:");
  taskManager.getEpicId(epic1.getId());
  printAllTasks(taskManager);

  System.out.println("Просматриваем подзадачу №1:");
  taskManager.getSubTaskId(subtask1.getId());
  printAllTasks(taskManager);

  System.out.println("Просматриваем задачу №2:");
  taskManager.getTaskId(task2.getId());
  printAllTasks(taskManager);

  System.out.println("Просматриваем подзадачу №2:");
  taskManager.getSubTaskId(subtask2.getId());
  printAllTasks(taskManager);
 }

 private static void printAllTasks(TaskManager manager) {
  System.out.println("Задачи:");
  for (Task task : manager.getAllTasks()) {
   System.out.println(task);
  }
  System.out.println("Эпики:");
  for (Epic epic : manager.getAllEpics()) {
   System.out.println(epic);

   for (Subtask subtask : manager.getSubtasksEpic(epic.getId())) {
    System.out.println("--> " + subtask);
   }
  }
  System.out.println("Подзадачи:");
  for (Subtask subtask : manager.getAllSubTasks()) {
   System.out.println(subtask);
  }

  System.out.println("История:");
  for (Task task : manager.getHistory()) {
   System.out.println(task);
  }
 }
}