import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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