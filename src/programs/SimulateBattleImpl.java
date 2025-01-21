package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;

/**
 * Реализация симуляции сражения, которая выполняет пошаговые действия между армией игрока
 * и армией противника, до тех пор, пока одна из сторон не проиграет.
 *
 * Класс работает с двумя армиями, определяет порядок атакующих бойцов,
 * выполняет атаки, и ведет логирование боя через предоставленный {@code PrintBattleLog}.
 *
 * Основные этапы работы:
 * 1. Фильтрация живых юнитов из каждой армии.
 * 2. Создание приоритетной очереди для юнитов каждой армии, сортируя их по базовой атаке.
 * 3. Организация боя, где каждый юнит атакует противника.
 * 4. Обновление списка живых юнитов после каждого раунда.
 *
 * Инкапсулирует логику управления и атаки бойцов, а также определяет правила обработки очередей.
 */
public class SimulateBattleImpl implements SimulateBattle {

    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        List<Unit> playerUnits = filterAliveUnits(playerArmy);
        List<Unit> computerUnits = filterAliveUnits(computerArmy);

        while (!playerUnits.isEmpty() && !computerUnits.isEmpty()) {
            PriorityQueue<Unit> playerQueue = createSortedUnitQueue(playerUnits);
            PriorityQueue<Unit> computerQueue = createSortedUnitQueue(computerUnits);

            while (!playerQueue.isEmpty() || !computerQueue.isEmpty()) {
                moveAndAttack(playerQueue);
                moveAndAttack(computerQueue);
            }

            playerUnits = filterAliveUnits(playerArmy);
            computerUnits = filterAliveUnits(computerArmy);
        }
    }

    private List<Unit> filterAliveUnits(Army army) {
        return army.getUnits().stream()
                .filter(Unit::isAlive)
                .toList();
    }

    private PriorityQueue<Unit> createSortedUnitQueue(List<Unit> units) {
        PriorityQueue<Unit> queue = new PriorityQueue<>(Comparator.comparingInt(Unit::getBaseAttack).reversed());
        queue.addAll(units);
        return queue;
    }

    private void moveAndAttack(PriorityQueue<Unit> queue) throws InterruptedException {
        while (!queue.isEmpty()) {
            Unit unit = queue.poll();
            if (unit.isAlive()) {
                performAttack(unit);
                return;
            }
        }
    }

    private void performAttack(Unit attacker) throws InterruptedException {
        Unit target = attacker.getProgram().attack();
        printBattleLog.printBattleLog(attacker, target);
    }
}