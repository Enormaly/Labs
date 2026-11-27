// Вариант 12, энергодиспетчерский пункт.
// Тип потребителя создаётся фабричным методом, а не условным оператором.
public class FactoryAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {
        ConsumerCreator tractionCreator = new TractionConsumerCreator();
        ConsumerCreator signalingCreator = new SignalingConsumerCreator();
        ConsumerCreator industrialCreator = new IndustrialConsumerCreator();
        ConsumerCreator householdCreator = new HouseholdConsumerCreator();

        EnergyConsumer tractionConsumer = tractionCreator.createConsumer(2500);
        EnergyConsumer signalingConsumer = signalingCreator.createConsumer(80);
        EnergyConsumer industrialConsumer = industrialCreator.createConsumer(1200);
        EnergyConsumer householdConsumer = householdCreator.createConsumer(150);

        System.out.println(tractionConsumer.description());
        System.out.println(signalingConsumer.description());
        System.out.println(industrialConsumer.description());
        System.out.println(householdConsumer.description());
        System.out.println();

        check("тяговый потребитель: описание не пустое и содержит мощность",
                describedWithPower(tractionConsumer));
        check("потребитель СЦБ: описание не пустое и содержит мощность",
                describedWithPower(signalingConsumer));
        check("промышленный потребитель: описание не пустое и содержит мощность",
                describedWithPower(industrialConsumer));
        check("тяговый потребитель создан своим создателем",
                tractionConsumer instanceof TractionConsumer);
        check("потребитель СЦБ создан своим создателем",
                signalingConsumer instanceof SignalingConsumer);
        check("промышленный потребитель создан своим создателем",
                industrialConsumer instanceof IndustrialConsumer);
        check("бытовой потребитель добавлен новым классом и работает наравне с остальными",
                householdConsumer instanceof HouseholdConsumer && describedWithPower(householdConsumer));

        ensureAllPassed();
    }

    static boolean describedWithPower(EnergyConsumer consumer) {
        return !consumer.description().isEmpty()
                && consumer.description().contains(String.valueOf(consumer.contractedPowerKw()));
    }

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }

    static void ensureAllPassed() {
        if (failedChecks > 0) {
            throw new AssertionError("не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки пройдены.");
    }
}

/** Продукт: потребитель энергии. Конкретный тип вызывающему коду неизвестен. */
interface EnergyConsumer {
    String description();
    double contractedPowerKw();
}

/** Создатель: фабричный метод выбирает класс потребителя. */
interface ConsumerCreator {
    EnergyConsumer createConsumer(double contractedPowerKw);
}

class TractionConsumer implements EnergyConsumer {
    private final double contractedPowerKw;

    TractionConsumer(double contractedPowerKw) {
        this.contractedPowerKw = contractedPowerKw;
    }

    public String description() {
        return "Тяговый потребитель: договорная мощность " + contractedPowerKw
                + " кВт, электроподвижной состав";
    }

    public double contractedPowerKw() {
        return contractedPowerKw;
    }
}

class SignalingConsumer implements EnergyConsumer {
    private final double contractedPowerKw;

    SignalingConsumer(double contractedPowerKw) {
        this.contractedPowerKw = contractedPowerKw;
    }

    public String description() {
        return "Потребитель СЦБ: договорная мощность " + contractedPowerKw
                + " кВт, устройства сигнализации и связи";
    }

    public double contractedPowerKw() {
        return contractedPowerKw;
    }
}

class IndustrialConsumer implements EnergyConsumer {
    private final double contractedPowerKw;

    IndustrialConsumer(double contractedPowerKw) {
        this.contractedPowerKw = contractedPowerKw;
    }

    public String description() {
        return "Промышленный потребитель: договорная мощность " + contractedPowerKw
                + " кВт, производственная нагрузка";
    }

    public double contractedPowerKw() {
        return contractedPowerKw;
    }
}

/** Четвёртый тип: добавлен новым классом, прежние продукты и создатели не менялись. */
class HouseholdConsumer implements EnergyConsumer {
    private final double contractedPowerKw;

    HouseholdConsumer(double contractedPowerKw) {
        this.contractedPowerKw = contractedPowerKw;
    }

    public String description() {
        return "Бытовой потребитель: договорная мощность " + contractedPowerKw
                + " кВт, освещение и бытовая нагрузка";
    }

    public double contractedPowerKw() {
        return contractedPowerKw;
    }
}

class TractionConsumerCreator implements ConsumerCreator {
    public EnergyConsumer createConsumer(double contractedPowerKw) {
        return new TractionConsumer(contractedPowerKw);
    }
}

class SignalingConsumerCreator implements ConsumerCreator {
    public EnergyConsumer createConsumer(double contractedPowerKw) {
        return new SignalingConsumer(contractedPowerKw);
    }
}

class IndustrialConsumerCreator implements ConsumerCreator {
    public EnergyConsumer createConsumer(double contractedPowerKw) {
        return new IndustrialConsumer(contractedPowerKw);
    }
}

class HouseholdConsumerCreator implements ConsumerCreator {
    public EnergyConsumer createConsumer(double contractedPowerKw) {
        return new HouseholdConsumer(contractedPowerKw);
    }
}
