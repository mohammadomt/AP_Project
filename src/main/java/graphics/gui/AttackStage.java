package graphics.gui;

import exceptions.ConsoleException;
import graphics.Fonts;
import graphics.GraphicsValues;
import graphics.drawers.Drawer;
import graphics.drawers.drawables.ButtonDrawable;
import graphics.drawers.drawables.ImageDrawable;
import graphics.drawers.drawables.RoundRectDrawable;
import graphics.drawers.drawables.TextDrawable;
import graphics.helpers.*;
import graphics.layers.Layer;
import graphics.positioning.NormalPositioningSystem;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import menus.Menu;
import menus.ParentMenu;
import menus.SoldierMenuItem;
import models.World;
import models.attack.Attack;
import models.attack.AttackReport;
import models.buildings.Building;
import models.soldiers.Healer;
import models.soldiers.MoveType;
import models.soldiers.Soldier;
import models.soldiers.SoldierValues;
import utils.GraphicsUtilities;
import utils.Point;
import utils.RectF;
import utils.TimeSpan;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.Consumer;

public class AttackStage extends AttackMapStage
{
    private Layer lFliers;

    private TimerGraphicHelper timer;
    private TextDrawable txtTime;
    private TextDrawable txtDefenderName;

    private Consumer<AttackReport> attackFinishedListener;

    public AttackStage(Attack attack, double width, double height)
    {
        super(attack, width, height, false);
        this.theAttack = attack;

        lFliers = new Layer(3, getObjectsLayer().getBounds(), getObjectsLayer().getPosSys());
    }

    private void callAttackFinished(AttackReport report)
    {
        if (attackFinishedListener != null)
            attackFinishedListener.accept(report);
    }

    public void setAttackFinishedListener(Consumer<AttackReport> attackFinishedListener)
    {
        this.attackFinishedListener = attackFinishedListener;
    }

    @Override
    protected void preShow(Group group)
    {
        super.preShow(group);

        initStyle(StageStyle.UNDECORATED);

        ParentMenu parentMenu = new ParentMenu(100, "");
        ArrayList<SoldierMenuItem> soldierMenuItems = new ArrayList<>();
        for (int i = 1; i <= SoldierValues.SOLDIER_TYPES_COUNT; i++)
        {
            SoldierMenuItem submenu = new SoldierMenuItem(100 + i, SoldierValues.getSoldierInfo(i).getName(), theAttack.getAllUnits(i).size(), i);
            submenu.setFocusable(theAttack.isReal);
            soldierMenuItems.add(submenu);
        }
        soldierMenuItems.forEach(parentMenu::insertItem);
        getMenuLayer().setCurrentMenu(parentMenu);

        txtTime = new TextDrawable("03:00", Color.WHITE, Fonts.getMedium());
        {
            txtTime.setHasShadow(true);
            txtTime.setPivot(0.5, 0.5);
            Drawer dTime = new Drawer(txtTime);
            dTime.setPosition(0.5, 0.5);
            dTime.setLayer(getStuffsLayer());
            theAttack.setAttackTime(new TimeSpan(3, 0));
            theAttack.setTimeChangeListener(this::updateTimerText);
            if (theAttack.isReal)
            {
                timer = new TimerGraphicHelper(theAttack.getAttackTime(), true);
                timer.setOnTimeFinished(() -> this.quitAttack(Attack.QuitReason.TURN));
                getGuiHandler().addUpdatable(timer);
            }
        }
        if (theAttack.getDefenderName() != null)
        {
            txtDefenderName = new TextDrawable(theAttack.getDefenderName(), Color.WHITE, Fonts.getLarge());
            {
                txtDefenderName.setHasShadow(true);
                txtDefenderName.setPivot(0.5, 0.5);
                Drawer dDefenderName = new Drawer(txtDefenderName);
                dDefenderName.setPosition(0.5, 0.05);
                dDefenderName.setLayer(getInfoLayer());
            }
        }
        if (theAttack.isReal)
        {
            ButtonDrawable btnEndAttack = new ButtonDrawable("End Attack", GraphicsValues.IconPaths.Stop, CELL_SIZE, CELL_SIZE);
            btnEndAttack.setPivot(0, 0);
            btnEndAttack.setFill(ButtonDrawable.DARK);
            Drawer dBtnEndAttack = new Drawer(btnEndAttack);
            dBtnEndAttack.setPosition(0, getStuffsLayer().getHeight() / ((NormalPositioningSystem)getStuffsLayer().getPosSys()).getScale() - 2);
            dBtnEndAttack.setLayer(getStuffsLayer());
            dBtnEndAttack.setClickListener((sender, event) -> quitAttack(Attack.QuitReason.USER));
        }

        getGameScene().addLayer(lFliers);
    }

    public void setup()
    {
        theAttack.setSoldierPutListener(this::addSoldier);
        super.setup();
    }

    private void addSoldier(Soldier soldier)
    {
        soldier.getAttackHelper().setIsReal();
        SoldierGraphicHelper helper;
        Layer layer = soldier.getMoveType() == MoveType.AIR ? lFliers : getObjectsLayer();
        if (soldier.getType() == Healer.SOLDIER_TYPE)
            helper = new HealerGraphicHelper(soldier, layer);
        else
            helper = new GeneralSoldierGraphicHelper(soldier, layer);

        soldier.getAttackHelper().setGraphicHelper(helper);
        soldier.getAttackHelper().addSoldierDieListener(this::checkForAllSoldiersDead);
        helper.setUpListeners();
        gHandler.addUpdatable(helper);
        int count = (int)theAttack.getAliveUnits(soldier.getType()).filter(s -> !s.getAttackHelper().isSoldierDeployed()).count();
        System.out.println(count);
        ((SoldierMenuItem)getMenuLayer().getCurrentMenu().getMenuItems().stream().filter(menu -> menu.getId() == soldier.getType() + 100).findFirst().get())
                .setCount(count);
        getMenuLayer().updateMenu();
    }

    @Override
    public BuildingGraphicHelper addBuilding(Building building)
    {
        building.getAttackHelper().addDestroyListener(this::checkForAllBuildingsDestroyed);
        return super.addBuilding(building);
    }

    @Override
    protected void onMarginalCellClick(Drawer sender, MouseEvent e)
    {
        if (!theAttack.isReal)
            return;
        SoldierMenuItem menu = (SoldierMenuItem)getMenuLayer().getCurrentMenu().getMenuItems().stream().filter(Menu::isFocused).findFirst().orElse(null);
        if (menu == null)
            return;
        try
        {
            theAttack.putUnits(menu.getId() - 100, 1, new Point((int)sender.getPosition().getX(), (int)sender.getPosition().getY()), false);
        }
        catch (ConsoleException ex) { ex.printStackTrace(); }
    }

    private void updateTimerText(TimeSpan time)
    {
        txtTime.setText(String.format("%02d:%02d", time.getTotalMinutes(), time.getSeconds()));
    }


    @Override
    protected void onClose(WindowEvent event)
    {
        super.onClose(event);
        theAttack = null;
        timer = null;
    }

    private void checkForAllSoldiersDead()
    {
        if (theAttack.areSoldiersDead())
            quitAttack(Attack.QuitReason.SOLDIERS_DIE);
    }

    private void checkForAllBuildingsDestroyed()
    {
        if (theAttack.areBuildingsDestroyed())
            quitAttack(Attack.QuitReason.MAP_DESTROYED);
    }

    private void quitAttack(Attack.QuitReason reason)
    {
        getGuiHandler().removeUpdatable(timer);

        AttackReport report = theAttack.quitAttack(reason);

        getLooper().stop();

        createAttackFinishLayer(reason, report);

        if (World.sCurrentClient != null)
        {
            report.setDefenderId(World.sCurrentClient.getActiveAttackTarget());
            report.setAttackerId(World.sCurrentClient.getClientId());
        }

        callAttackFinished(report);
    }

    private void createAttackFinishLayer(Attack.QuitReason reason, AttackReport report)
    {
        Layer layer = new Layer(12, new RectF((width - 400) / 2, (height - 400) / 2, 400, 400), new NormalPositioningSystem(1));

        RoundRectDrawable bg = new RoundRectDrawable(layer.getWidth(), layer.getHeight(), GraphicsValues.PADDING, ButtonDrawable.DARK);
        Drawer dBackground = new Drawer(bg);
        dBackground.setPosition(0, 0);
        dBackground.setLayer(layer);

        TextDrawable txtTitle = new TextDrawable(reason.getTitle(), reason == Attack.QuitReason.MAP_DESTROYED ? Color.GREEN : Color.RED, Fonts.getLarge());
        {
            txtTitle.setHasShadow(true);
            txtTitle.setPivot(0.5, 0.5);
            Drawer drawer = new Drawer(txtTitle);
            drawer.setPosition(layer.getWidth() / 2, 50);
            drawer.setLayer(layer);
        }


        addTextAndIcon(layer, Integer.toString(report.getClaimedResource().getGold()), GraphicsValues.IconPaths.GoldCoin, 100);
        addTextAndIcon(layer, Integer.toString(report.getClaimedResource().getElixir()), GraphicsValues.IconPaths.ElixirDrop, 150);
        addTextAndIcon(layer, Integer.toString(report.getClaimedScore()), GraphicsValues.IconPaths.Trophie, 200);

        ButtonDrawable btnEnd = new ButtonDrawable("Return to Village", GraphicsValues.IconPaths.Map, CELL_SIZE * 2, CELL_SIZE);
        btnEnd.setPivot(0.5, 1);
        btnEnd.setFill(Color.web("#41c300", 0.6));
        Drawer dBtnEnd = new Drawer(btnEnd);
        dBtnEnd.setPosition(layer.getWidth() / 2, layer.getHeight() - GraphicsValues.PADDING);
        dBtnEnd.setLayer(layer);
        dBtnEnd.setClickListener((sender, event) -> close());

        getGuiScene().addLayer(layer);
    }

    private void addTextAndIcon(Layer layer, String text, String iconPath, double y)
    {
        TextDrawable txt = new TextDrawable(text, Color.WHITE, Fonts.getLarge());
        txt.setHasShadow(true);
        txt.setMaxWidth(200);
        txt.setPivot(0.5, 0.5);

        Drawer drawer = new Drawer(txt);
        drawer.setPosition(layer.getWidth() / 2, y);
        drawer.setLayer(layer);

        try
        {
            ImageDrawable imgIcon = GraphicsUtilities.createImageDrawable(iconPath, 30, 30, true);
            imgIcon.setPivot(0.5, 0.5);
            Drawer dIcon = new Drawer(imgIcon);
            dIcon.setPosition(layer.getWidth() / 2 + txt.getMaxWidth() / 2, drawer.getPosition().getY());
            dIcon.setLayer(layer);
        }
        catch (URISyntaxException e) { }
    }
}
