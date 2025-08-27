import java.io.File;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Pair{
	public String name;
	public double distance = 0;
	Pair(String name, double distance){
		this.name = name;
		this.distance = distance;
	}
}
class Image{
	public String name;
	public BufferedImage img;
	Image(String name, BufferedImage img){
		this.name = name;
		this.img = img;
	}
}
public class ImageClassifier{
	public static void main(String[] args){
        KNN test = new KNN("./train_data", "./library", "./runner", 31);
        File dir = new File("./runner");
        File[] contents = dir.listFiles();
		System.out.println("Program Running....");
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (File content : contents) {
		    executor.submit(() -> {
		        System.out.println("Running " + content.getName() + " in " + Thread.currentThread().getName());
		        test.knn_solver("./runner/"+content.getName(), content.getName());
		    });
		}
		executor.shutdown();
	}
}
