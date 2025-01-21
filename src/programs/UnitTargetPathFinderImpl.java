package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.EdgeDistance;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

/**
 * Реализация интерфейса UnitTargetPathFinder для нахождения пути к цели в координатной сетке
 * с учетом препятствий и ограничений.
 *
 * Класс использует алгоритм поиска кратчайшего пути (например, на основе похожего на алгоритм Дейкстры)
 * для определения последовательности координат, ведущих от начальной позиции атакующего юнита к цели.
 *
 * Поля:
 * - WIDTH: ширина игрового поля.
 * - HEIGHT: высота игрового поля.
 * - DIRECTIONS: массив возможных направлений движения (4 соседние клетки).
 *
 * Методы:
 * - getTargetPath: основной метод для вычисления пути.
 * - collectAliveUnitPositions: определяет позиции всех живых юнитов, которые являются препятствиями.
 * - initializeDistances: инициализирует таблицу расстояний значениями по умолчанию (максимальные значения).
 * - getAscendingEdgeDistanceComparator: возвращает компаратор для очереди с приоритетом.
 * - isValid: проверяет, можно ли пройти в конкретную клетку (с учетом ограничений и целей).
 * - isWithinBounds: проверяет, находятся ли координаты внутри допустимой области.
 * - isTargetNode: проверяет, является ли заданная клетка целевой позицией.
 * - createNodeKey: создаёт уникальный ключ для позиции в формате строки.
 * - constructPath: восстанавливает путь от целевой позиции к начальной.
 *
 * Этот класс проектировался для использования на игровом поле фиксированных размеров.
 * Обеспечивает поиск пути в условиях, где могут существовать препятствия, а также принимает во внимание,
 * что целевая позиция также может быть временно занята.
 */
public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {

    private static final int WIDTH = 27;
    private static final int HEIGHT = 21;
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    @Override
    public List<Edge> getTargetPath(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        int[] startCoordinates = {attackUnit.getxCoordinate(), attackUnit.getyCoordinate()};
        int[] targetCoordinates = {targetUnit.getxCoordinate(), targetUnit.getyCoordinate()};

        Set<String> unitPositions = collectAliveUnitPositions(attackUnit, targetUnit, existingUnitList);
        int[][] distances = new int[WIDTH][HEIGHT];
        initializeDistances(distances);
        distances[startCoordinates[0]][startCoordinates[1]] = 0;

        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        Edge[][] predecessors = new Edge[WIDTH][HEIGHT];

        PriorityQueue<EdgeDistance> priorityQueue = new PriorityQueue<EdgeDistance>(getAscendingEdgeDistanceComparator());
        priorityQueue.add(new EdgeDistance(startCoordinates[0], startCoordinates[1], 0));

        while (!priorityQueue.isEmpty()) {
            EdgeDistance current = priorityQueue.poll();
            int currentX = current.getX(), currentY = current.getY();

            if (visited[currentX][currentY]) continue;
            visited[currentX][currentY] = true;

            if (currentX == targetCoordinates[0] && currentY == targetCoordinates[1]) break;

            for (int[] direction : DIRECTIONS) {
                int newX = currentX + direction[0], newY = currentY + direction[1];
                if (isValid(newX, newY, unitPositions, targetUnit)) {
                    int newDistance = distances[currentX][currentY] + 1;
                    if (newDistance < distances[newX][newY]) {
                        distances[newX][newY] = newDistance;
                        predecessors[newX][newY] = new Edge(currentX, currentY);
                        priorityQueue.add(new EdgeDistance(newX, newY, newDistance));
                    }
                }
            }
        }

        return constructPath(predecessors, startCoordinates, targetCoordinates);
    }

    private static Set<String> collectAliveUnitPositions(Unit attackUnit, Unit targetUnit, List<Unit> existingUnitList) {
        Set<String> unitPositions = new HashSet<>();
        for (Unit unit : existingUnitList) {
            if (unit != attackUnit && unit != targetUnit && unit.isAlive()) {
                unitPositions.add(createNodeKey(unit.getxCoordinate(), unit.getyCoordinate()));
            }
        }
        return unitPositions;
    }

    private static void initializeDistances(int[][] distances) {
        for (int[] row : distances) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
    }

    private static Comparator<EdgeDistance> getAscendingEdgeDistanceComparator() {
        return Comparator.comparingInt(EdgeDistance::getDistance);
    }

    private static boolean isValid(int x, int y, Set<String> unitPositions, Unit targetUnit) {
        return isWithinBounds(x, y) && (!unitPositions.contains(createNodeKey(x, y)) || isTargetNode(x, y, targetUnit));
    }

    private static boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    private static boolean isTargetNode(int x, int y, Unit targetUnit) {
        return x == targetUnit.getxCoordinate() && y == targetUnit.getyCoordinate();
    }

    private static String createNodeKey(int x, int y) {
        return x + "," + y;
    }

    private List<Edge> constructPath(Edge[][] predecessors, int[] start, int[] target) {
        List<Edge> path = new ArrayList<>();
        int targetX = target[0], targetY = target[1];

        if (predecessors[targetX][targetY] == null) return path;

        while (targetX != start[0] || targetY != start[1]) {
            path.add(new Edge(targetX, targetY));
            Edge predecessor = predecessors[targetX][targetY];
            targetX = predecessor.getX();
            targetY = predecessor.getY();
        }

        path.add(new Edge(start[0], start[1]));
        Collections.reverse(path);
        return path;
    }
}