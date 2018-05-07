package models.soldiers;

import models.Attack;
import models.Resource;
import models.buildings.Building;
import models.buildings.BuildingValues;
import models.buildings.Storage;
import utils.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            if (target != null)
                if (isTargetInRange())
                {
                    if (target.getStrength() > 0 && !target.isDestroyed())
                    {
                        if (target.getType() == 3 || target.getType() == 4)
                        {
                            Storage storage = (Storage)target;
                            int initialStrength = storage.getStrength();
                            storage.decreaseStrength(getDamage());
                            int finalStrength = Math.max(storage.getStrength(), 0);
                            int damageRael = initialStrength - finalStrength;
                            switch (target.getType())
                            {
                                case 3:
                                {
                                    Resource resourceClaimed = new Resource((int)Math.floor(1.0 * damageRael / (BuildingValues.getBuildingInfo(target.getType()).getInitialStrength() + target.getLevel() * 10) * storage.getCurrentAmount()), 0);// 10 may vary in the up and coming configs
                                    attack.addToClaimedResource(resourceClaimed);
                                    //System.out.println("gold claimed amount:" + resourceClaimed.getGold());
                                    attack.addToGainedResourceOfStorageDesroying(storage, resourceClaimed);
                                    break;
                                }
                                case 4:
                                {
                                    Resource resourceClaimed = new Resource(0, (int)Math.floor(1.0 * damageRael / (BuildingValues.getBuildingInfo(target.getType()).getInitialStrength() + target.getLevel() * 10) * storage.getCurrentAmount()));// 10 may vary in the up and coming configs
                                    attack.addToClaimedResource(resourceClaimed); // 10 may vary in the up and coming configs
                                    attack.addToGainedResourceOfStorageDesroying(storage, resourceClaimed);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            target.decreaseStrength(getDamage());
                        }
                    }
                    if (target.getStrength() <= 0)
                    {
                        target.setDestroyed(true);
                        attack.addScore(BuildingValues.getBuildingInfo(target.getType()).getDestroyScore());
                        attack.addToClaimedResource(target.getBuildingInfo().getDestroyResource());
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
        return true;
        //TODO‌ bad smell of redundant code of escaping compile error.in fact the code shouldn't reach here because we set the target first then we come up to move or fight
    }

    @Override
    public void setTarget()
    {
        if (soldier != null && !soldier.getAttackHelper().isDead())
            if (target == null || target.getStrength() <= 0 || target.isDestroyed())
            {
                target = getNearestBuilding();
                if (target != null)
                    System.err.println("new target: " + target.getName() + " at: " + target.getLocation().toString());
                else
                    System.err.println("no target found.");
            }
    }

    private Building getNearestBuilding()
    {

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
        return attack.getMap().getBuildings().stream().filter(building -> !building.isDestroyed());
    }

    private boolean isTargetReachable(Building favouriteTarget)
    {
        return !(attack.getSoldierPath(soldier.getLocation(), favouriteTarget.getLocation(), soldier.getMoveType() == MoveType.AIR) == null);
    }

    @Override
    public void move()
    {
        if (soldier != null && !soldier.getAttackHelper().isDead())
            if (!isTargetInRange())
            {
                Point pointToGo = getPointToGo(target.getLocation());
                attack.moveOnLocation(soldier, getSoldierLocation(), pointToGo);
                soldier.setLocation(pointToGo);
            }
    }
}
