package graphics;

import javafx.scene.canvas.GraphicsContext;
import utils.MySortedList;
import utils.RectF;
import utils.SizeF;

public class GameScene implements IFrameUpdatable
{
    private MySortedList<Integer, Layer> layers;

    private SizeF size;

    public GameScene(double width, double height)
    {
        this(new SizeF(width, height));
    }

    public GameScene(SizeF size)
    {
        layers = new MySortedList<>(Layer::getOrder);
        this.size = size;
    }

    public void addLayer(Layer layer)
    {
        layers.addValue(layer);
    }

    public void draw(GraphicsContext gc, RectF cameraBounds)
    {
        gc.clearRect(cameraBounds.getX(), cameraBounds.getY(), cameraBounds.getWidth(), cameraBounds.getHeight());
        layers.forEach(l -> l.draw(gc, cameraBounds));
    }

    @Override
    public void update(double deltaT)
    {
        layers.forEach(l -> l.update(deltaT));
    }
}
