package model.buildings;

import model.Soldiers.Soldier;

import java.util.ArrayList;

public class Camp extends VillageBuilding
{
    int capacity;
    ArrayList<Soldier> soldiers;

    public int getCapacity()
    {
        return capacity;
    }

    @Override
    public int getType()
    {
        return 7;
    }

    @Override
    public void upgrade()
    {
        // TODO: 4/13/18 method should throw an exception  
    }

    public void heal()
    {

    }
}