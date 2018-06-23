package graphics.gui.dialogs;

import graphics.Layer;
import graphics.drawers.Drawer;
import graphics.drawers.drawables.ImageDrawable;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import models.Map;
import utils.ConsoleUtilities;
import utils.GraphicsUtilities;
import views.dialogs.DialogResult;
import views.dialogs.DialogResultCode;

import java.net.URISyntaxException;
import java.util.regex.Matcher;

public class MapInputDialog extends GraphicDialog
{
    private Map map;

    public MapInputDialog(Layer layer, double width, double height, String message, Map map)
    {
        super(layer, width, height, message);
        this.map = map;
    }

    @Override
    public DialogResult showDialog()
    {
        Dialog dialog = new TextInputDialog();
        dialog.setContentText(message);
        dialog.showAndWait();
        Object result = dialog.getResult();
        Matcher command = ConsoleUtilities.getMatchedCommand("\\((?<x>\\d+),(?<y>\\d+)\\)", (String)result);
        return new DialogResult(DialogResultCode.YES).addData("text", command.group(0)).addData("matcher", command);
    }

    private void showMap()
    {
        try
        {
            ImageDrawable bg1 = GraphicsUtilities.createImageDrawable("assets/floor/gd.png", getWidth() / (2 * map.getSize().getWidth()), getWidth() / (2 * map.getSize().getWidth()), false);
            ImageDrawable bg2 = GraphicsUtilities.createImageDrawable("assets/floor/gl.png", getWidth() / (2 * map.getSize().getWidth()), getWidth() / (2 * map.getSize().getWidth()), false);
            ImageDrawable building = GraphicsUtilities.createImageDrawable("assets/floor/building.png", getWidth() / (2 * map.getSize().getWidth()), getWidth() / (2 * map.getSize().getWidth()), false);
            bg1.setPivot(0.5, 0.5);
            bg2.setPivot(0.5, 0.5);
            building.setPivot(0.5, 0.5);
            for (int i = 0; i < map.getSize().getHeight(); i++)
            {
                for (int j = 0; j < map.getSize().getWidth(); j++)
                {
                    DialogResult dialogResult;
                    if (map.isEmptyForBuilding(j, i))
                    {
                        Drawer bg = new Drawer((i + j) % 2 == 0 ? bg1 : bg2);
                        bg.setPosition(getWidth() / 2 + (j - map.getSize().getWidth() / 2) * bg1.getHeight(), getHeight() / 2 + (i - map.getSize().getWidth() / 2) * bg1.getWidth());
                        bg.setLayer(getLayer());
                        bg.setClickListener((sender, event) ->
                        {
                            double J = (bg.getPosition().getX() - getWidth() / 2) / bg1.getHeight() + map.getWidth() / 2;
                            double I = (bg.getPosition().getY() - getHeight() / 2) / bg1.getHeight() + map.getWidth() / 2;
//                            dialogResult = new DialogResult(DialogResultCode.YES).
//                                    addData("text", String.format("%d,%d", J, I)).addData("matcher", String.format("%d,%d", J, I));
                            getLayer().removeAllObjects();
                        });
                    }
                    else if (!map.isEmptyForBuilding(j, i))
                    {
                        Drawer buildingDrawer = new Drawer(building);
                        buildingDrawer.setPosition(getWidth() / 2 + (j - map.getSize().getWidth() / 2) * bg1.getHeight(), getHeight() / 2 + (i - map.getSize().getWidth() / 2) * bg1.getWidth());
                        buildingDrawer.setLayer(getLayer());
                    }
                }
            }
        }
        catch (URISyntaxException ignored) {}
    }
}