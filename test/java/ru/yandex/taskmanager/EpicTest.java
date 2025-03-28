package test.java.ru.yandex.taskmanager;

import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.model.Epic;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    @Test
    void testEpicStatusWhenAllSubtasksAreNew() {
        Epic epic = new Epic(1, "Epic 1", "Description 1", TaskStatus.NEW);
        Subtask subtask1 = new Subtask(2, "Subtask 1", "Description 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask(3, "Subtask 2", "Description 2", TaskStatus.NEW, 1);

        epic.addSubtask(subtask1.getId());
        epic.addSubtask(subtask2.getId());

        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус должен быть NEW, если все подзадачи NEW");
    }


}