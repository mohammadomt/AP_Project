package models.soldiers;

import models.Attack;
import utils.Point;

import java.util.ArrayList;
import java.util.Arrays;

public class HealerAttackHelper extends AttackHelper
{
    private ArrayList<Soldier> targets;
    private final Point point1 = new Point(5, 5);
    private final Point point2 = new Point(15, 5);
    private final Point point3 = new Point(25, 5);
    private final Point point4 = new Point(5, 15);
    private final Point point5 = new Point(15, 15);
    private final Point point6 = new Point(25, 15);
    private final Point point7 = new Point(5, 25);
    private final Point point8 = new Point(15, 25);
    private final Point point9 = new Point(25, 25);
    private final ArrayList<Point> points = new ArrayList<>(Arrays.asList(point1, point2, point3, point4, point5, point6, point7, point8, point9));
    private Point destination;


    public HealerAttackHelper(Attack attack, Healer healer)
    {
        super(attack, healer);
    }

    @Override
    public void move()
    {
        changeDestinationIfNeeded();
        //TODO‌ in the path finding algorithm we should use the destination attribute in the roll of destination
    }

    @Override
    public void fire()
    {
        if (targets != null && targets.size() != 0)
        {
            for (Soldier target : targets)
            {
                target.increaseHealth(getDamage());
            }
        }
    }

    @Override
    public void setTarget()
    {
        targets = getSoldiersInRange();
    }

    private Point getSoldiersConcentrationPoint()
    {
        Point output = new Point(0, 0);
        int outputCountOfSoldiers = 0;
        for (int i = 0; i < 9; i++)
        {
            if (i == 0)
            {
                output = points.get(i);
            }
            int soldierNumbers = countNumberOfSoldiersAround(points.get(i));
            if (i == 0)
            {
                outputCountOfSoldiers = soldierNumbers;
            }
            if (soldierNumbers > outputCountOfSoldiers)
            {
                output = points.get(i);
                outputCountOfSoldiers = soldierNumbers;
            }
        }
        return output;
    }

    private void changeDestinationIfNeeded()
    {
        Point shouldISwitchTo = getSoldiersConcentrationPoint();
        int shouldISwitchToNumber = countNumberOfSoldiersAround(shouldISwitchTo);
        if (Math.abs(countNumberOfSoldiersAround(destination) - shouldISwitchToNumber) < (0.3 * shouldISwitchToNumber))
        {
            destination = shouldISwitchTo;
        }
    }

    private int countNumberOfSoldiersAround(Point point)
    {
        int numberOfSoldiersInRange = 0;
        for (Soldier soldier : attack.soldiersOnMap)
        {
            if (euclidianDistance(soldier.getLocation(), point) <= 10)
            {
                numberOfSoldiersInRange++;
            }
        }
        return numberOfSoldiersInRange;
    }



    private ArrayList<Soldier> getSoldiersInRange()
    {
        ArrayList<Soldier> soldiersInRange = new ArrayList<>();
        for (Soldier soldier : attack.soldiersOnMap)
        {
            if (euclidianDistance(soldier.getLocation(), getSoldierLocation()) <= getRange())
            {
                soldiersInRange.add(soldier);
            }
        }
        return soldiersInRange;
    }
}
