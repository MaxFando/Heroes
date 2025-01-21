package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

/**
 * Реализация интерфейса GeneratePreset, которая создает армию, распределяя юниты на координатной сетке.
 *
 * Класс отвечает за генерацию армии из списка доступных юнитов в соответствии с заданным лимитом очков.
 * Юниты сортируются по убыванию эффективности, и их размещение на сетке происходит случайным образом.
 *
 * Основные задачи класса:
 * 1. Сортировка юнитов по коэффициенту эффективности: (атака + здоровье) / стоимость.
 * 2. Создание клонов юнитов для добавления в армию с уникальными координатами.
 * 3. Распределение юнитов на сетке в случайных координатах по правилам ограничения максимального количества юнитов одного типа.
 *
 * Константы класса:
 * - MAX_UNITS_PER_TYPE: Максимальное количество юнитов одного типа (одинакового имени) в армии.
 * - GRID_WIDTH: Ширина сетки, на которой размещаются юниты.
 * - GRID_HEIGHT: Высота сетки, на которой размещаются юниты.
 *
 * Основной метод:
 * {@link #generate(List<Unit>, int)} - генерирует армию, размещает юнитов на координатной сетке
 * и возвращает объект класса Army.
 */
public class GeneratePresetImpl implements GeneratePreset {

    private static final int MAX_UNITS_PER_TYPE = 11;
    private static final int GRID_WIDTH = 3;
    private static final int GRID_HEIGHT = 21;

    @Override
    public Army generate(List<Unit> units, int maxPoints) {
        units.sort(createDescendingEfficiencyComparator());

        List<Unit> armyUnits = new ArrayList<>();
        int currentPoints = 0;
        Iterator<Edge> randomEdges = createRandomizedEdges();

        for (Unit unit : units) {
            int unitsToAdd = Math.min(MAX_UNITS_PER_TYPE, (maxPoints - currentPoints) / unit.getCost());
            for (int i = 0; i < unitsToAdd; i++) {
                armyUnits.add(createClonedUnit(unit, i, randomEdges));
                currentPoints += unit.getCost();
            }
        }

        Army army = new Army(armyUnits);
        army.setPoints(currentPoints);
        return army;
    }

    private static Comparator<Unit> createDescendingEfficiencyComparator() {
        return (a, b) -> Double.compare(
                (double) (b.getBaseAttack() + b.getHealth()) / b.getCost(),
                (double) (a.getBaseAttack() + a.getHealth()) / a.getCost()
        );
    }

    private static Unit createClonedUnit(Unit unit, int index, Iterator<Edge> randomEdges) {
        Edge edge = randomEdges.next();
        return new Unit(
                unit.getName() + " " + (index + 1),
                unit.getUnitType(),
                unit.getHealth(),
                unit.getBaseAttack(),
                unit.getCost(),
                unit.getAttackType(),
                unit.getAttackBonuses(),
                unit.getDefenceBonuses(),
                edge.getX(),
                edge.getY()
        );
    }

    private static Iterator<Edge> createRandomizedEdges() {
        List<Edge> edges = new ArrayList<>();

        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                edges.add(new Edge(x, y));
            }
        }

        Collections.shuffle(edges);
        return edges.iterator();
    }
}