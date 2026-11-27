// Вариант 12, энергодиспетчерский пункт.
// Подстанция рассылает событие аварии подписчикам и не знает, кто они.
import java.util.ArrayList;
import java.util.List;

public class ObserverAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        Substation substation = new Substation("Гомель-тяговая");
        DispatcherBoardListener boardListener = new DispatcherBoardListener();
        AccidentJournalListener journalListener = new AccidentJournalListener();
        CrewMailingListener mailingListener = new CrewMailingListener();

        substation.addListener(boardListener);
        substation.addListener(journalListener);
        substation.addListener(mailingListener);

        System.out.println("--- Авария КЗ-1: порядок подписки — табло, журнал, рассылка ---");
        substation.accidentOccurred("КЗ-1");

        check("после одной аварии журнал получил ровно одну запись",
                journalListener.recordCount() == 1);
        check("табло, журнал и рассылка получили одно и то же событие",
                sameAccident(boardListener, journalListener, mailingListener, "Гомель-тяговая", "КЗ-1"));

        substation.removeListener(journalListener);
        System.out.println();
        System.out.println("--- Авария КЗ-2: журнал отписан ---");
        substation.accidentOccurred("КЗ-2");
        check("после отписки журнал больше не получает записи",
                journalListener.recordCount() == 1);
        check("остальные подписчики аварию КЗ-2 получили",
                boardListener.lastAccidentCode().equals("КЗ-2")
                        && mailingListener.lastAccidentCode().equals("КЗ-2"));

        AsuListener asuListener = new AsuListener();
        substation.addListener(asuListener);
        System.out.println();
        System.out.println("--- Авария ОЗЗ-3: четвёртый наблюдатель добавлен без правки Substation ---");
        substation.accidentOccurred("ОЗЗ-3");
        check("передача в АСУ получила событие, класс Substation не менялся",
                asuListener.receivedCount() == 1
                        && asuListener.lastAccidentCode().equals("ОЗЗ-3"));

        System.out.println();
        System.out.println("Порядок уведомления равен порядку подписки: табло, журнал, рассылка; АСУ — после отдельной подписки.");

        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }

    static boolean sameAccident(DispatcherBoardListener boardListener,
                                AccidentJournalListener journalListener,
                                CrewMailingListener mailingListener,
                                String substationName,
                                String accidentCode) {
        return boardListener.lastSubstationName().equals(substationName)
                && journalListener.lastSubstationName().equals(substationName)
                && mailingListener.lastSubstationName().equals(substationName)
                && boardListener.lastAccidentCode().equals(accidentCode)
                && journalListener.lastAccidentCode().equals(accidentCode)
                && mailingListener.lastAccidentCode().equals(accidentCode);
    }

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }
}

/** Наблюдатель: получатель сообщения об аварии на подстанции. */
interface AccidentListener {
    void onAccident(String substationName, String accidentCode);
}

/** Субъект: хранит подписчиков и не знает их конкретных классов. */
class Substation {

    private final String substationName;
    private final List<AccidentListener> listeners = new ArrayList<>();

    Substation(String substationName) {
        this.substationName = substationName;
    }

    void addListener(AccidentListener listener) {
        listeners.add(listener);
    }

    void removeListener(AccidentListener listener) {
        listeners.remove(listener);
    }

    void accidentOccurred(String accidentCode) {
        for (AccidentListener listener : listeners) {
            listener.onAccident(substationName, accidentCode);
        }
    }
}

class DispatcherBoardListener implements AccidentListener {
    private String lastSubstationName = "";
    private String lastAccidentCode = "";

    public void onAccident(String substationName, String accidentCode) {
        lastSubstationName = substationName;
        lastAccidentCode = accidentCode;
        System.out.println("[табло]  Авария " + accidentCode + " на подстанции " + substationName);
    }

    String lastSubstationName() {
        return lastSubstationName;
    }

    String lastAccidentCode() {
        return lastAccidentCode;
    }
}

class AccidentJournalListener implements AccidentListener {
    private final List<String> records = new ArrayList<>();
    private String lastSubstationName = "";
    private String lastAccidentCode = "";

    public void onAccident(String substationName, String accidentCode) {
        lastSubstationName = substationName;
        lastAccidentCode = accidentCode;
        records.add(substationName + ": зафиксирована авария " + accidentCode);
        System.out.println("[журнал] " + records.get(records.size() - 1));
    }

    int recordCount() {
        return records.size();
    }

    String lastSubstationName() {
        return lastSubstationName;
    }

    String lastAccidentCode() {
        return lastAccidentCode;
    }
}

class CrewMailingListener implements AccidentListener {
    private String lastSubstationName = "";
    private String lastAccidentCode = "";

    public void onAccident(String substationName, String accidentCode) {
        lastSubstationName = substationName;
        lastAccidentCode = accidentCode;
        System.out.println("[рассылка] Подстанция " + substationName + ", авария " + accidentCode);
    }

    String lastSubstationName() {
        return lastSubstationName;
    }

    String lastAccidentCode() {
        return lastAccidentCode;
    }
}

/** Четвёртый наблюдатель: подключается через addListener, класс Substation не меняется. */
class AsuListener implements AccidentListener {
    private String lastAccidentCode = "";
    private int receivedCount = 0;

    public void onAccident(String substationName, String accidentCode) {
        lastAccidentCode = accidentCode;
        receivedCount++;
        System.out.println("[АСУ]    Передана авария " + accidentCode + " на подстанции " + substationName);
    }

    String lastAccidentCode() {
        return lastAccidentCode;
    }

    int receivedCount() {
        return receivedCount;
    }
}
