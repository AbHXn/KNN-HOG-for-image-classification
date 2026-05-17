package JavaKnn;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.*;
import java.util.HashMap;

public class KNN{
    // trainDir -> folder path for training images
    // targetDir -> folder path for classified images
    // runDir -> folder path of images to be classified
    // dRate -> neighbhour selection ratio (0-1)
    public String trainDir, targetDir, runDir;
	public double dRate;
	public KNN(   String trainDir, 
           String targetDir, 
           String runDir, 
           double dRate)
    {	
        this.trainDir = trainDir;
		this.dRate = dRate;
        this.targetDir = targetDir;
        this.runDir = runDir;
	}
        
    // EUCLIDEAN DISTANCE
    private double get_euclidean_distance(double[] testImage, double[] trainImage){
        double distance = 0;
        for(int i = 0; i < testImage.length; i++){
            double diff = testImage[i] - trainImage[i];
            distance += diff * diff;
        }
        return Math.sqrt(distance);
    }

    public boolean knn_solver(String sourceImage, String imageName){
        try{
            // convert the source to same as training image           
            BufferedImage testImage = TrainData.get_resized_gray_scale_image(sourceImage);
            // get the feature vector for test image
            double[] testVec = HOG_Container.get_image_vectors(testImage);
            // concurrent running
            ExecutorService executor = Executors.newFixedThreadPool(5);
            for(Image trainImage: TrainData.trainImages){
				final Image tImage = trainImage;
                executor.submit(() -> {
                    double distance = get_euclidean_distance(testVec, tImage.features);
                    // set the distance attribute
                    tImage.set_distance(distance);
                });
            }
            executor.shutdown();
            if(!executor.awaitTermination(10, TimeUnit.MINUTES)){
				System.out.println("Some thread not finished work");
			};
            // sort images based on distance
            TrainData.trainImages.sort((imga, imgb) -> {
                return Double.compare(imga.distance, imgb.distance); 
            });
            Move classifier = new Move(this.targetDir);

            int nCount = (int) (TrainData.trainImages.size() * dRate);
            nCount = Math.max(1, Math.min(nCount, TrainData.trainImages.size()));

            HashMap<String, Double> classWeights = new HashMap<>();
            double totalWeight = 0.0;

            for(int x = 0; x < nCount; x++){
                Image neighbor = TrainData.trainImages.get(x);
                String className = classifier.getFileNameOnly(neighbor.name);

                double weight = 1.0 / (neighbor.distance + 1e-6);
                classWeights.put(className, classWeights.getOrDefault(className, 0.0) + weight);
                totalWeight += weight;
            }

            // Find class with highest total weight
            String predictedClass = "";
            double maxWeight = -1.0;

            for(String cls : classWeights.keySet()){
                double w = classWeights.get(cls);
                double percentage = (w / totalWeight) * 100.0;
                System.out.println(cls + ": " + percentage + "%");
                if(w > maxWeight){
                    maxWeight = w;
                    predictedClass = cls;
                }
            }
            System.out.println("Predicted Class: " + predictedClass);

            // extract the folder name from neighbours images name
            //String folder_name = classifier.folder_name(neighbours);
            // move to the folder where neighbours live...
            classifier.move(sourceImage, predictedClass, imageName);
            return true;
        }catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
}
