import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HistoryManager historyManager = new HistoryManager();
        Document currentDocument = null;
        String currentFileName = "";

        while (true) {
            System.out.println("\nГлавное меню:");
            System.out.println("1. Создать файл");
            System.out.println("2. Выбрать файл");
            System.out.println("3. Завершение работы");
            System.out.print("Выберите действие: ");

            String mainChoice = scanner.nextLine().trim();

            if (mainChoice.equals("1")) {
                System.out.print("Введите имя для нового файла (например, document.txt): ");
                String newFileName = scanner.nextLine().trim();
                if (!newFileName.toLowerCase().endsWith(".txt")) {
                    newFileName += ".txt";
                }

                System.out.println("\nВыберите структуру шаблона:");
                System.out.println("1. Договор купли-продажи");
                System.out.println("2. Финансовый отчет");
                System.out.println("3. Базовый черновик");
                System.out.print("Ваш выбор: ");
                String templateChoice = scanner.nextLine().trim();

                DocumentFactory factory;
                switch (templateChoice) {
                    case "1": factory = new ContractTemplateFactory(newFileName); break;
                    case "2": factory = new ReportTemplateFactory(newFileName); break;
                    default: factory = new DefaultTemplateFactory(newFileName); break;
                }

                currentDocument = factory.createDocument();
                currentDocument.saveToFile();
                currentFileName = newFileName;
                System.out.println("-> Файл '" + currentFileName + "' успешно создан на диске и открыт.");

                fileMenuLoop(scanner, currentDocument, historyManager);

            } else if (mainChoice.equals("2")) {
                File dir = new File(".");
                String[] txtFiles = dir.list((d, name) -> name.toLowerCase().endsWith(".txt"));

                if (txtFiles == null || txtFiles.length == 0) {
                    System.out.println("-> В папке проекта не найдено текстовых файлов.");
                    continue;
                }

                System.out.println("\nСписок файлов в папке проекта:");
                for (int i = 0; i < txtFiles.length; i++) {
                    System.out.println("[" + (i + 1) + "] " + txtFiles[i]);
                }
                System.out.print("Выберите номер файла: ");

                try {
                    int index = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (index >= 0 && index < txtFiles.length) {
                        currentFileName = txtFiles[index];
                        DocumentFactory factory = new ExistingFileFactory(currentFileName);
                        currentDocument = factory.createDocument();
                        System.out.println("-> Файл '" + currentFileName + "' успешно загружен.");

                        fileMenuLoop(scanner, currentDocument, historyManager);
                    } else {
                        System.out.println("-> Ошибка: Неверный номер файла.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("-> Ошибка: Необходимо ввести число.");
                }

            } else if (mainChoice.equals("3")) {
                System.out.println("Работа программы завершена.");
                scanner.close();
                return;
            } else {
                System.out.println("-> Операция не распознана.");
            }
        }
    }

    private static void fileMenuLoop(Scanner scanner, Document document, HistoryManager historyManager) {
        while (true) {
            System.out.println("\nМеню работы с файлом:");
            System.out.println("1. Вывести содержимое файла");
            System.out.println("2. Открыть историю изменений");
            System.out.println("3. Внести изменения в файл");
            System.out.println("0. Назад в главное меню");
            System.out.print("Выберите действие: ");

            String fileChoice = scanner.nextLine().trim();

            if (fileChoice.equals("1")) {
                document.display();
            } else if (fileChoice.equals("2")) {
                historyManager.showHistory();
                if (historyManager.getHistory().isEmpty()) {
                    continue;
                }
                System.out.println("\n1. Откатить состояние файла к изменениям, сделанным ранее");
                System.out.println("0. Продолжить без отката");
                System.out.print("Выберите подпункт: ");
                String subChoice = scanner.nextLine().trim();

                if (subChoice.equals("1")) {
                    System.out.print("Укажите индекс версии для отката: ");
                    try {
                        int index = Integer.parseInt(scanner.nextLine().trim());
                        if (index >= 0 && index < historyManager.getHistory().size()) {
                            DocumentMemento memento = historyManager.getHistory().get(index);

                            
                            document.restore(memento);

                           
                            historyManager.truncateHistory(index);

                            System.out.println("-> Состояние успешно восстановлено. Поздние версии удалены из истории.");
                        } else {
                            System.out.println("-> Ошибка: Неверный индекс.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("-> Ошибка: Неверный формат индекса.");
                    }
                }
            } else if (fileChoice.equals("3")) {
                System.out.print("Введите имя изменяемого атрибута: ");
                String attribute = scanner.nextLine().trim();
                System.out.print("Введите новое значение: ");
                String newValue = scanner.nextLine().trim();

                DocumentMemento currentSnapshot = document.save();

                Command editCommand = new ChangeAttributeCommand(document, attribute, newValue);
                editCommand.execute();

                System.out.println("\n2.3.1. Сохранить изменения или не сохранять?");
                System.out.println("1. Сохранить");
                System.out.println("2. Не сохранять");
                System.out.print("Ваш выбор: ");
                String saveChoice = scanner.nextLine().trim();

                if (saveChoice.equals("1")) {
                    historyManager.saveHistory(currentSnapshot);
                    document.saveToFile();
                    System.out.println("-> Изменения успешно сохранены на диск и добавлены в журнал истории.");
                } else {
                    document.setRoot(currentSnapshot.getSavedState());
                    System.out.println("-> Отмена операции. Изменения сброшены, состояние файла не изменилось.");
                }
            } else if (fileChoice.equals("0")) {
                return;
            } else {
                System.out.println("-> Операция не распознана.");
            }
        }
    }
}
