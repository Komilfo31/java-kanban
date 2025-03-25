package test;

import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TaskTest {
    @Test
    void testTaskEqualityById() {
        Task task1 = new Task(1, "Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task(1, "Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }


}