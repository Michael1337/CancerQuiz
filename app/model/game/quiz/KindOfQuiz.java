package model.game.quiz;

import java.util.HashMap;
import java.util.Map;

public enum KindOfQuiz {
    General(1), Diseases(2), Treatment(3);

    private static final Map<Integer, KindOfQuiz> EnumMap = new HashMap<Integer, KindOfQuiz>();

    static {
        for (KindOfQuiz myEnum : values()) {
            EnumMap.put(myEnum.getId(), myEnum);
        }
    }

    int id;
    String name;

    KindOfQuiz(int id) {
        this.id = id;
        switch (id) {
            case 1:
                this.name = "Krebs allgemein";
                break;
            case 2:
                this.name = "Die verschiedenen Krebsarten";
                break;
            case 3:
                this.name = "Krebsbehandlung und -forschung";
                break;
            default:
                this.name = "Krebs allgemein";
                break;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static KindOfQuiz getByValue(int value) {
        return EnumMap.get(value);
    }
}
