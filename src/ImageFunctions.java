import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class ImageFunctions {
	public static void drawImage(GraphicsContext gc, Image img, double x, double y, double width, double height) {
		if (!ImageSettings.stretch().get()) {
			ColorAdjust ca = new ColorAdjust();		
			ca.setBrightness(ImageSettings.brightness());
			gc.setEffect(ca);
			double cratio = width/height;
			double iratio = img.getWidth()/img.getHeight();
			
			if (Math.abs(cratio - iratio) < 0.01) {//almost perfect fit
				gc.drawImage(img, x, y, width, height);
				gc.setEffect(null);
			} else if (iratio > cratio) {//bars on top & bottom. calculate height
				double cheight = width/iratio;
				double yoffset = (height - cheight) / 2;
				gc.drawImage(img, x, y + yoffset, width, cheight);
				gc.setEffect(null);
				
				LinearGradient fadeGradient = new LinearGradient(
				    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				    new Stop(0.0, ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND)),
				    new Stop(1.0, Color.TRANSPARENT)
				);
				gc.setFill(fadeGradient);
				gc.fillRect(x, y+yoffset-1, width, 50);
				
				LinearGradient fadeGradient2 = new LinearGradient(
				    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				    new Stop(0.0, Color.TRANSPARENT),
				    new Stop(1.0, ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND))				    
				);
				gc.setFill(fadeGradient2);
				gc.fillRect(x, y+height-yoffset-49, width, 50);
			} else {//bars on sides. calculate width
				double cwidth = height*iratio;
				double xoffset = (width - cwidth) / 2;
				gc.drawImage(img, x + xoffset, y, cwidth, height);
				gc.setEffect(null);
				
				LinearGradient fadeGradient = new LinearGradient(
				    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
				    new Stop(0.0, ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND)),
				    new Stop(1.0, Color.TRANSPARENT)
				);
				gc.setFill(fadeGradient);
				gc.fillRect(x+xoffset-1, y, 50, height);
				
				LinearGradient fadeGradient2 = new LinearGradient(
				    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
				    new Stop(0.0, Color.TRANSPARENT),
				    new Stop(1.0, ColourSettings.colour(ColourSettings.ColourIndex.CHART_BACKGROUND))				    
				);
				gc.setFill(fadeGradient2);
				gc.fillRect(x+width-xoffset-49, y, 50, height);
			} 
		} else {
			ColorAdjust ca = new ColorAdjust();		
			ca.setBrightness(ImageSettings.brightness());
			gc.setEffect(ca);
			gc.drawImage(img, x, y, width, height);
			gc.setEffect(null);
		}
	}
}
