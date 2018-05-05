package models.soldiers;

import models.Attack;
import models.buildings.Building;
import utils.Point;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.min;

public class GeneralAttackHelper extends AttackHelper

{
    private Building target;

    public GeneralAttackHelper(Attack attack, Soldier soldier)
    {
        super(attack, soldier);
    }

    @Override
    public void fire()
    {
        if (soldier != null && !soldier.getAttackHelper().isDead())
        {
            if (target != null)
            {
                if (isTargetInRange())
                {
                    if (target.getStrength() > 0 && !target.isDestroyed())
                    {
                        target.decreaseStrength(getDamage());
                    }
                    if (target.getStrength() <= 0)
                    {
                        target.setDestroyed(true);
                        attack.addToClaimedResource(target.getBuildingInfo().getDestroyResource());
                    }
                }
            }
        }
    }

    private boolean isTargetInRange()
    {
        if (target != null)
        {
            double distance2nd = Point.euclideanDistance2nd(target.getLocation(), getSoldierLocation());
            return distance2nd == 2 || Math.sqrt(distance2nd) <= getRange();
        }
        return true;//TODO‌ bad smell of redandent code of escaping compile error.in fact the code shouldn't reach here because we set the target first then we come up to move or fight
    }

    @Override
    public void setTarget()
    {
        if (soldier != null && !soldier.getAttackHelper().isDead())
        {
            if (target == null || target.getStrength() <= 0 || target.isDestroyed())
            {
//                if (getBestFavouriteTarget() != null)
//                {
//                    target = getBestFavouriteTarget();
//                }
//                else
//                {
//                    target = getNearestBuilding();
//                }
                target = getNearestBuilding();
                if (target != null)
                    System.err.println("new target: " + target.getName() + " at: " + target.getLocation().toString());
                else
                    System.err.println("no target found.");
            }
        }
    }

    private Building getNearestBuilding()
    {
//        ArrayList<Building> buildings = getAliveBuildings();
//        ArrayList<Double> distances = new ArrayList<>();
//        for (Building building : buildings)
//        {
//            distances.add(Point.euclideanDistance(building.getLocation(), getSoldierLocation()));
//        }
//        double minimumDistance = min(distances);
//        for (Building building : buildings)
//        {
//            if (Math.abs(Point.euclideanDistance(building.getLocation(), getSoldierLocation()) - minimumDistance) < 0.01)
//            {
//                return building;
//            }
//        }

        ArrayList<Building> aliveBuildings = getAliveBuildings()
                .sorted(Comparator.comparingDouble(building -> Point.euclideanDistance2nd(building.getLocation(), getSoldierLocation())))
                .collect(Collectors.toCollection(ArrayList::new));
        try
        {
            if (soldier.getSoldierInfo().getFavouriteTargets().length == 0)
                throw new Exception();

            return aliveBuildings.stream()
                    .filter(building -> Arrays.stream(soldier.getSoldierInfo().getFavouriteTargets()).anyMatch(t -> t.isInstance(building)))
                    .filter(this::isTargetReachable)
                    .findFirst().orElseThrow(Exception::new);

        }
        catch (Exception ex)
        {
            return aliveBuildings.stream()
                    .filter(this::isTargetReachable)
                    .findFirst()
                    .orElse(null);
        }
    }


    public Stream<Building> getAliveBuildings()
    {
//        ArrayList<Building> buildings = super.attack.getMap().getBuildings();
//        ArrayList<Building> aliveBuildings = new ArrayList<>();
//        for (Building building : buildings)
//        {
//            if (!building.isDestroyed() && building.getStrength() > 0)
//            {
//                aliveBuildings.add(building);
//            }
//        }
//        return aliveBuildings;
        return attack.getMap().getBuildings().stream().filter(building -> !building.isDestroyed());
    }

    public Building getBestFavouriteTarget()
    {
//        ArrayList<Building> favoutriteTargets = new ArrayList<>();
//        ArrayList<Building> buildings = getAliveBuildings();
//        for (Building building : buildings)
//        {
//            if (Arrays.stream(soldier.getSoldierInfo().getFavouriteTargets()).anyMatch(c -> c.isInstance(building)))
//            {
//                favoutriteTargets.add(building);
//            }
//        }
//        ArrayList<Double> euclidianDistances = new ArrayList<>();
//        if (favoutriteTargets.size() != 0)
//        {
//            for (Building favoutriteTarget : favoutriteTargets)
//            {
//                euclidianDistances.add(euclidianDistance(favoutriteTarget.getLocation(), super.getSoldierLocation()));
//            }
//            Collections.sort(euclidianDistances);
//            for (int i = 0; i < euclidianDistances.size(); i++)
//            {
//                for (Building favoutriteTarget : favoutriteTargets)
//                {
//                    if (Math.abs(euclidianDistance(favoutriteTarget.getLocation(), super.getSoldierLocation()) - euclidianDistances.get(i)) < 0.01)
//                    {
//                        if (isTargetReachable(favoutriteTarget))
//                        {
//                            return favoutriteTarget;
//                        }
//                    }
//                }
//            }
//        }

        return null;
    }

    private boolean isTargetReachable(Building favouriteTarget)
    {
        //attack.getArrayMap().isReachable();
        return true;
    }

    @Override
    public void move()
    {
        if (soldier != null && !soldier.getAttackHelper().isDead())
        {
            if (!isTargetInRange())
            {
                List<Point> soldierPath = attack.getSoldierPath(getSoldierLocation(), target.getLocation());
                Point pointToGo = soldierPath.get(soldierPath.size() - 1);

                int i;
                for (i = soldierPath.size() - 1; i >= 0; i--)
                {
                    if (i != soldierPath.size() - 1)
                    {
                        pointToGo = soldierPath.get(i + 1);
                    }
                    if (Point.euclideanDistance(soldierPath.get(i), getSoldierLocation()) > soldier.getSpeed())
                    {
                        break;
                    }
                }
                attack.displayMove(soldier, getSoldierLocation(), pointToGo);
                soldier.setLocation(pointToGo);
            }
        }
    }
}
