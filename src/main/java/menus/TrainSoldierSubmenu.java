package menus;

import graphics.GraphicsValues;
import models.World;
import models.buildings.Barracks;
import models.buildings.Building;
import models.soldiers.SoldierInfo;
import models.soldiers.SoldierValues;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class TrainSoldierSubmenu extends Submenu implements IBuildingMenu
{
    private Barracks barracks;

    public TrainSoldierSubmenu(ParentMenu parent, Barracks barracks)
    {
        super(Id.BARRACKS_TRAIN_SOLDIER, "Train", parent);
        setIconPath(GraphicsValues.UI_ASSETS_PATH + "/sword.png");
        this.barracks = barracks;
    }

    private void setItems()
    {
        int availableElixir = World.getVillage().getResources().getElixir();
        items = SoldierValues.getInfos().stream()
                .sorted(Comparator.comparingInt(SoldierInfo::getType))
                .map(info -> new TrainSoldierItem(info.getType(), info.getMinBarracksLevel() > barracks.getLevel() ? -1 : availableElixir / info.getBrewCost().getElixir()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<String> getItems()
    {
        setItems();
        return super.getItems();
    }

    @Override
    public ArrayList<Menu> getMenuItems()
    {
        setItems();
        return super.getMenuItems();
    }

    @Override
    public Building getBuilding()
    {
        return ((BuildingSubmenu)parent).getBuilding();
    }
}
