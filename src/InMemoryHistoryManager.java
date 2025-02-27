import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

        private final ArrayList<Task> history = new ArrayList<>();

        @Override
        public void add(Task task) {
            if (history.size() == 10) {
                history.remove(0);
            }
            history.add(task);
        }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
