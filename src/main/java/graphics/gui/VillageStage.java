package graphics.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import exceptions.ConsoleException;
import exceptions.MyIOException;
import exceptions.MyJsonException;
import graphics.Fonts;
import graphics.GraphicsValues;
import graphics.drawers.BuildingDrawer;
import graphics.drawers.Drawer;
import graphics.drawers.drawables.ButtonDrawable;
import graphics.drawers.drawables.RoundRectDrawable;
import graphics.drawers.drawables.TextDrawable;
import graphics.gui.dialogs.MapInputDialog;
import graphics.gui.dialogs.SettingsDialog;
import graphics.gui.dialogs.SingleChoiceDialog;
import graphics.gui.dialogs.TextInputDialog;
import graphics.helpers.BuildingGraphicHelper;
import graphics.helpers.VillageBuildingGraphicHelper;
import graphics.layers.Layer;
import graphics.layers.ResourceLayer;
import graphics.positioning.NormalPositioningSystem;
import graphics.positioning.PercentPositioningSystem;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import menus.AttackMapItem;
import menus.Menu;
import menus.ParentMenu;
import models.Map;
import models.Village;
import models.World;
import models.attack.Attack;
import models.attack.AttackMap;
import models.attack.attackHelpers.NetworkDecoder;
import models.buildings.Building;
import models.buildings.ElixirStorage;
import models.buildings.GoldStorage;
import models.soldiers.Soldier;
import network.AttackUDPReceiver;
import serialization.AttackMapGlobalAdapter;
import serialization.BuildingGlobalAdapter;
import serialization.StorageGlobalAdapter;
import utils.RectF;
import views.VillageView;
import views.dialogs.DialogResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class VillageStage extends GUIMapStage
{
    private static VillageStage instance;

    public static VillageStage getInstance()
    {
        if (instance == null)
        {
            instance = new VillageStage(World.getVillage(), 1200, 900);
            instance.setup();
        }
        return instance;
    }

    public static void showInstance()
    {
        getInstance().show();
    }

    private ResourceLayer lResource;

    private Layer lCaution;
    private TextDrawable txtCaution;
    private Drawer dBtnCaution;


    private VillageView villageView;

    private VillageStage(Village village, double width, double height)
    {
        super(village.getMap(), width, height);
        lResource = new ResourceLayer(8,
                new RectF(width - 200 - GraphicsValues.PADDING * 2, 20, 200 + GraphicsValues.PADDING * 2, 70),
                village::getResources, village::getTotalResourceCapacity);
        lCaution = new Layer(100, new RectF(0, 0, width, height));
        lCaution.setPosSys(new PercentPositioningSystem(lCaution));
        lCaution.setVisible(false);
    }

    public void setVillageView(VillageView villageView)
    {
        this.villageView = villageView;
    }

    @Override
    protected void preShow(Group group)
    {
        super.preShow(group);

        getMenuLayer().setClickListener(item ->
        {
            villageView.setCurrentMenu(getMenuLayer().getCurrentMenu(), true);
            villageView.onItemClicked(item);
        });

        ButtonDrawable btnAttack = new ButtonDrawable("Attack", GraphicsValues.IconPaths.Axes, CELL_SIZE, CELL_SIZE);
        btnAttack.setPivot(0, 0);
        btnAttack.setFill(ButtonDrawable.LIGHT);
        Drawer dBtnAttack = new Drawer(btnAttack);
        dBtnAttack.setPosition(0, getStuffsLayer().getHeight() / ((NormalPositioningSystem)getStuffsLayer().getPosSys()).getScale() - 2);
        dBtnAttack.setLayer(getStuffsLayer());
        dBtnAttack.setClickListener(this::onBtnAttackClick);

        ButtonDrawable btnSave = new ButtonDrawable("", GraphicsValues.IconPaths.Save, CELL_SIZE / 2, CELL_SIZE / 2);
        btnSave.setPivot(0, 0);
        btnSave.setFill(Color.rgb(255, 255, 255, 0.6));
        Drawer dBtnSave = new Drawer(btnSave);
        dBtnSave.setPosition(0, 0);
        dBtnSave.setLayer(getStuffsLayer());
        dBtnSave.setClickListener(this::onBtnSaveClick);

        ButtonDrawable btnSettings = new ButtonDrawable("", GraphicsValues.IconPaths.Settings, CELL_SIZE / 2, CELL_SIZE / 2);
        btnSettings.setPivot(0, 0);
        btnSettings.setFill(ButtonDrawable.LIGHT);
        Drawer dBtnSettings = new Drawer(btnSettings);
        dBtnSettings.setPosition(0, 0.5);
        dBtnSettings.setLayer(getStuffsLayer());
        dBtnSettings.setClickListener(this::onBtnSettingsClick);

        ButtonDrawable btnChat = new ButtonDrawable("", GraphicsValues.IconPaths.Chat, CELL_SIZE / 2, CELL_SIZE / 2);
        btnChat.setPivot(0, 0);
        btnChat.setFill(ButtonDrawable.LIGHT);
        Drawer dBtnChat = new Drawer(btnChat);
        dBtnChat.setPosition(1, 0.5);
        dBtnChat.setLayer(getStuffsLayer());
        dBtnChat.setClickListener(this::onBtnChatClick);

        ButtonDrawable btnNetwork = new ButtonDrawable("", GraphicsValues.IconPaths.Network, CELL_SIZE / 2, CELL_SIZE / 2);
        btnNetwork.setFill(ButtonDrawable.LIGHT);
        Drawer dBtnNetwork = new Drawer(btnNetwork);
        dBtnNetwork.setPosition(1, 0);
        dBtnNetwork.setLayer(getStuffsLayer());
        dBtnNetwork.setClickListener(this::onBtnNetworkClick);


        //Caution Layer
        {
            RoundRectDrawable bg = new RoundRectDrawable(lCaution.getWidth(), lCaution.getHeight(), 0, GraphicsValues.BLACK_60);
            Drawer dBackground = new Drawer(bg);
            dBackground.setLayer(lCaution);
            dBackground.setClickListener((sender, e) ->
            {
                System.out.println("fuck");
            });

            txtCaution = new TextDrawable("", Fonts.getLarge());
            txtCaution.setPivot(0.5, 0.5);
            txtCaution.setFill(Color.WHITE);
            Drawer dTxtCaution = new Drawer(txtCaution);
            dTxtCaution.setPosition(0.5, 0.5);
            dTxtCaution.setLayer(lCaution);

            ButtonDrawable btnCaution = new ButtonDrawable("", "", CELL_SIZE * 2, CELL_SIZE);
            btnCaution.setPivot(0.5, 0.5);
            dBtnCaution = new Drawer(btnCaution);
            dBtnCaution.setPosition(0.5, 0.75);
            dBtnCaution.setClickListener((sender, e) ->
            {
            });
            dBtnCaution.setLayer(lCaution);

            getGuiScene().addLayer(lCaution);
        }

        getGuiScene().addLayer(lResource);

    }


    @Override
    public BuildingGraphicHelper addBuilding(Building building)
    {
        VillageBuildingGraphicHelper graphicHelper = new VillageBuildingGraphicHelper(building, getObjectsLayer(), getMap());

        building.createAndSetVillageHelper();

        setUpBuildingDrawer(graphicHelper.getBuildingDrawer());

        building.getVillageHelper().setGraphicHelper(graphicHelper);

        gHandler.addUpdatable(graphicHelper);

        return graphicHelper;
    }

    @Override
    protected void setUpBuildingDrawer(BuildingDrawer drawer)
    {
        super.setUpBuildingDrawer(drawer);
        drawer.setClickListener((sender, event) ->
        {
            showBottomMenu(drawer.getBuilding());
        });
    }

    private void showBottomMenu(Building building)
    {
        getMenuLayer().setCurrentMenu(building.getMenu(new ParentMenu(Menu.Id.VILLAGE_MAIN_MENU, "")));
    }


    public DialogResult showSingleChoiceDialog(String message, ButtonType yes, ButtonType no)
    {
        SingleChoiceDialog dialog = new SingleChoiceDialog(getInfoLayer(), width, height, message, yes, no);
        return dialog.showDialog();
    }

    public DialogResult showMapInputDialog(String message, Map map)
    {
        MapInputDialog dialog = new MapInputDialog(getStuffsLayer(), width, height, message, map);
        return dialog.showDialog();
    }

    public DialogResult showOpenMapDialog(String message)
    {
        TextInputDialog dialog = new TextInputDialog(getStuffsLayer(), width, height, message);
        return dialog.showDialog();
    }

    @Override
    protected void onClickAnywhereElse(MouseEvent event)
    {
        super.onClickAnywhereElse(event);
        getMenuLayer().setCurrentMenu(null);
    }

    public void onBtnAttackClick(Drawer sender, MouseEvent event)
    {
        if (World.sCurrentClient == null)
        {
            List<Path> paths = World.sSettings.getAttackMapPaths().stream().map(Paths::get).collect(Collectors.toList());
            ParentMenu mainMenu = new ParentMenu(Menu.Id.ATTACK_MAIN_MENU, "");
            mainMenu.insertItem(new Menu(Menu.Id.ATTACK_LOAD_MAP, "Load Map", GraphicsValues.IconPaths.NewMap));
            paths.forEach(p -> mainMenu.insertItem(new AttackMapItem(mainMenu, p)));
            getMenuLayer().setCurrentMenu(mainMenu);
        }
        else
        {
            NetworkStage.showInstance();
            NetworkStage.getInstance().getController().selectTab(0);
        }
    }

    private void onBtnSaveClick(Drawer sender, MouseEvent event)
    {
        try
        {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data", "save.json"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
            {
                World.saveGame(writer);
                World.saveSettings();
            }
            catch (JsonIOException ex)
            {
                throw new MyJsonException(ex);
            }
            catch (NoSuchFileException ex)
            {
                throw new MyIOException("File not found.", ex);
            }
            catch (IOException ex)
            {
                throw new MyIOException(ex);
            }
        }
        catch (ConsoleException ex)
        {
            villageView.showError(ex);
        }
    }

    private void onBtnSettingsClick(Drawer sender, MouseEvent event)
    {
        SettingsDialog dialog = new SettingsDialog();
        dialog.showMenu();

        if (dialog.isPaused())
        {
            World.sSettings.setGameSpeed(0);
        }
        if (!dialog.hasSounds()) ;
        //disable game sounds
        if (dialog.isBackToMenu())
        {
            close();
            World.showMenu();
        }
        else
        {
            World.sSettings.setGameSpeed(dialog.getGameSpeed());
        }


    }

    private void onBtnChatClick(Drawer sender, MouseEvent event)
    {
        NetworkStage.showInstance();
        NetworkStage.getInstance().getController().selectTab(1);
    }

    private void onBtnNetworkClick(Drawer sender, MouseEvent event)
    {
        NetworkStage.showInstance();
        NetworkStage.getInstance().toFront();
    }

    private AttackStage liveCachedStage;

    public void lockStageForAttack(String attackerName, List<Soldier> soldiers)
    {
        txtCaution.setText("Your village is under attack by: " + attackerName);
        ((ButtonDrawable)dBtnCaution.getDrawable()).setText("WATCH");
        AttackUDPReceiver receiver = new AttackUDPReceiver(5500);
        receiver.start();
        Gson serializer = new GsonBuilder()
                .registerTypeAdapter(AttackMap.class, new AttackMapGlobalAdapter())
                .registerTypeAdapter(Map.class, new AttackMapGlobalAdapter())
                .registerTypeAdapter(Building.class, new BuildingGlobalAdapter())
                .registerTypeAdapter(GoldStorage.class, new StorageGlobalAdapter<>(GoldStorage.class))
                .registerTypeAdapter(ElixirStorage.class, new StorageGlobalAdapter<>(ElixirStorage.class))
                .setPrettyPrinting()
                .create();
        Attack attack = new Attack(serializer.fromJson(serializer.toJson(World.getVillage().getMap(), Map.class), AttackMap.class), false);
        attack.addUnits(soldiers);
        NetworkDecoder decoder = new NetworkDecoder(attack, receiver);
        World.sCurrentClient.sendUDPStarted("localhost", receiver.getSocket().getLocalPort());
        Platform.runLater(() ->
        {
            AttackStage stage = new AttackStage(attack, width, height);
            stage.setup();
            dBtnCaution.setClickListener(((sender, event) -> stage.show()));
            liveCachedStage = stage;
        });
        lCaution.setVisible(true);
        getLooper().stop();
    }

    public void unlockStageAfterAttack()
    {
        lCaution.setVisible(false);
        getLooper().start();
        Platform.runLater(() -> liveCachedStage.close());
    }
}
