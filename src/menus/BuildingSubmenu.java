package menus;

import models.buildings.Building;

public class BuildingSubmenu extends Submenu
{
    private Building building;

    public BuildingSubmenu(String text, ParentMenu parent, Building building)
    {
        super(Id.BUILDING_MENU, building.getName() + " " + building.getBuildingNum(), parent);
        this.building = building;
        insertItem(new BuildingInfoSubmenu(this));
    }

    public Building getBuilding()
    {
        return building;
    }
}
