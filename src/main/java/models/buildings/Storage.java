package models.buildings;


import menus.BuildingInfoSubmenu;
import menus.Menu;
import utils.Point;

public abstract class Storage extends VillageBuilding
{
    private final String INITIAL_CAPACITY_KEY = "initcap";

    protected int capacity;

    public Storage(Point location, int buildingNum)
    {
        super(location, buildingNum);
    }

    public int getCapacity()
    {
        return capacity;
    }

    @Override
    public void upgrade()
    {
        super.upgrade();
        double newCapacity = capacity * 1.6;
        capacity = (int)newCapacity;
    }

    @Override
    public void ensureLevel()
    {
        super.ensureLevel();
        capacity = (int)(Math.pow(1.6, level) * (double)getBuildingInfo().getMetadata(INITIAL_CAPACITY_KEY));
    }

    @Override
    public BuildingInfoSubmenu getInfoSubmenu()
    {
        return (BuildingInfoSubmenu)new BuildingInfoSubmenu(null)
                .insertItem(Menu.Id.STORAGE_SRC_INFO, "Sources info");
    }
}
