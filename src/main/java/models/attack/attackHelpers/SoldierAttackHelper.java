package models.attack.attackHelpers;


import graphics.helpers.IOnMoveFinishedListener;
import graphics.helpers.IOnReloadListener;
import graphics.helpers.SoldierGraphicHelper;
import models.attack.Attack;
import models.soldiers.MoveType;
import models.soldiers.Soldier;
import models.soldiers.SoldierValues;
import utils.Point;

import java.util.List;

public abstract class SoldierAttackHelper implements IOnReloadListener, IOnMoveFinishedListener
{
    private int health;
    protected Attack attack;
    protected Soldier soldier;
    private boolean isSoldierDeployed = false;

    protected boolean isDead = false;

    public SoldierAttackHelper(Attack attack, Soldier soldier)
    {
        this.attack = attack;
        this.soldier = soldier;
        this.health = getInitialHealth();
    }

    public int getHealth()
    {
        return health;
    }

    public void setHealth(int health)
    {
        this.health = health;
    }

    public boolean isSoldierDeployed()
    {
        return isSoldierDeployed;
    }

    public void setSoldierIsDeployed(boolean isSoldierDeployed)
    {
        this.isSoldierDeployed = isSoldierDeployed;
    }

    public Point getSoldierLocation()
    {
        return soldier.getLocation();
    }

    public int getDamage()
    {
        return soldier.getDamage();
    }

    public int getRange()
    {
        return SoldierValues.getSoldierInfo(soldier.getType()).getRange();
    }

    public Attack getAttack()
    {
        return attack;
    }

    public void passTurn()
    {
        removeSoldierIfDead();
        if (soldier != null && !isDead && isSoldierDeployed)
        {
            setTarget();
            move();
            fire();
        }
    }

    protected void removeSoldierIfDead()
    {
        if (health <= 0)
        {
            setDead(true);
            soldier = null;
        }
    }

    public Point getPointToGo(Point destination)
    {
        List<Point> soldierPath = attack.getSoldierPath(getSoldierLocation(), destination, soldier.getMoveType() == MoveType.AIR);
        Point pointToGo = soldierPath.get(soldierPath.size() - 1);

        int i;
        for (i = soldierPath.size() - 1; i >= 0; i--)
        {
            if (i != soldierPath.size() - 1)
                pointToGo = soldierPath.get(i + 1);
            if (Point.euclideanDistance(soldierPath.get(i), getSoldierLocation()) > soldier.getSpeed())
                break;
        }
        return pointToGo;
    }

    public Point getNextPathPoint(Point start, Point destination)
    {
        List<Point> soldierPath = attack.getSoldierPath(start, destination, soldier.getMoveType() == MoveType.AIR);
        if (soldierPath.size() >= 2)
        {
            return soldierPath.get(soldierPath.size() - 2);
        }
        else
        {
            return null;
        }
    }

    public void increaseHealth(int amount)
    {
        health = Math.min(this.getHealth() + amount, getInitialHealth());
    }

    public void decreaseHealth(int amount)
    {
        health = Math.max(health - amount, 0);
        if (health <= 0)
            setDead(true);
    }

    private int getInitialHealth()
    {
        return SoldierValues.getSoldierInfo(soldier.getType()).getInitialHealth() + (soldier.getLevel()) * 5;
    }

    public abstract void move();

    public abstract void fire();

    public abstract void setTarget();

    public boolean isDead()
    {
        return isDead;
    }

    public void setDead(boolean dead)
    {
        isDead = dead;
    }

    //graphic
    private SoldierGraphicHelper graphicHelper;

    public SoldierGraphicHelper getGraphicHelper()
    {
        return graphicHelper;
    }

    public void setGraphicHelper(SoldierGraphicHelper graphicHelper)
    {
        this.graphicHelper = graphicHelper;
    }
}
