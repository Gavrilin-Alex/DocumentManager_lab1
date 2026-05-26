import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private final List<DocumentMemento> history = new ArrayList<>();

    public void saveHistory(DocumentMemento memento) { history.add(memento); }
    public List<DocumentMemento> getHistory() { return history; }

    
    public void truncateHistory(int fromIndex) {
        if (fromIndex >= 0 && fromIndex < history.size()) {
          
            while (history.size() > fromIndex) {
                history.remove(history.size() - 1);
            }
        }
    }

    public void showHistory() {
        System.out.println("\n------- ИСТОРИЯ ИЗМЕНЕНИЙ (ТОЧКИ ОТКАТА) -------");
        if (history.isEmpty()) {
            System.out.println("История изменений пуста.");
            return;
        }
        for (int i = 0; i < history.size(); i++) {
            System.out.println("[" + i + "] Изменение от: " + history.get(i).getTimestamp());
        }
        System.out.println("------------------------------------------------");
    }
}
