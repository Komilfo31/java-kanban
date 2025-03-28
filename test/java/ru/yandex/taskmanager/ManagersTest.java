package ru.yandex.taskmanager;

import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.manager.HistoryManager;
import ru.yandex.taskmanager.manager.TaskManager;
import ru.yandex.taskmanager.util.Managers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {
    @Test
    void testManagersReturnInitializedInstances() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(taskManager, "TaskManager должен быть проинициализирован");
        assertNotNull(historyManager, "HistoryManager должен быть проинициализирован");
    }
}