package JavaKnn;

import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.*;

public class TrainData{
	public static List<Image> trainImages = new CopyOnWriteArrayList<>();
	static public int PIXEL_SIZE = 192;
    static private final int cores = Runtime.getRuntime().availableProcessors();

	static public boolean load_training_data(String trainData){
		try{
            File dir = new File(trainData);
            File[] contents = dir.listFiles();
            // check if its null or contents is empty
            if (contents == null || contents.length == 0) {
                System.out.println("No training images found in: " + trainData);
                return false;
            }
            ExecutorService executor = Executors.newFixedThreadPool(cores);
            for (File trainImage : contents) {
                final File currentFile = trainImage;
                executor.submit(() -> {
                    String fullFileName = trainData.concat("/").concat(currentFile.getName());
                    BufferedImage greyTrainImage = get_resized_gray_scale_image(fullFileName);
                    Image new_image = new Image(fullFileName, greyTrainImage);
                    double[] features = HOG_Container.get_image_vectors(greyTrainImage);
                    new_image.set_features(features);
                    trainImages.add(new_image);
                });
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
           	return true;
        }catch(Exception e){
            System.out.println("Error occured in loading training data: " + e.getMessage());
            return false;
        }
	}
	// for getting resized gray scale image
	static public BufferedImage get_resized_gray_scale_image(String source){
        try{
            BufferedImage img = ImageIO.read(new File(source));
            BufferedImage gray = new BufferedImage(PIXEL_SIZE, PIXEL_SIZE, 
                                            BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = gray.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, PIXEL_SIZE, PIXEL_SIZE, null);
            g.dispose();
            return gray;
        }catch (Exception e){
            System.out.println("Error occured in resizing and gray scale convertion: " + e.getMessage());
            return null;
        }
    }
}
