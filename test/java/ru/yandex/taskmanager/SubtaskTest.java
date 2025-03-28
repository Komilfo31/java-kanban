package ru.yandex.taskmanager;

import org.junit.jupiter.api.Test;
import ru.yandex.taskmanager.model.Subtask;
import ru.yandex.taskmanager.model.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {
    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description 1", TaskStatus.NEW, 10);
        Subtask subtask2 = new Subtask(1, "Subtask 2", "Description 2", TaskStatus.DONE, 10);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }
}