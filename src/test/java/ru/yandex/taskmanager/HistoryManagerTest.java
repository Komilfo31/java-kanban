package test.java.ru.yandex.taskmanager;

import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.manager.HistoryManager;
import ru.yandex.taskmanager.model.Task;
import ru.yandex.taskmanager.model.TaskStatus;
import ru.yandex.taskmanager.util.Managers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryManagerTest {
    @Test
    void testHistoryManagerLimit() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        for (int i = 1; i <= 15; i++) {
            Task task = new Task(i, "Task " + i, "Description " + i, TaskStatus.NEW);
            historyManager.add(task);
        }

        assertEquals(10, historyManager.getHistory().size(), "История не должна превышать 10 задач");
    }
}