package JavaKnn;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.*;

class KNN{
    // trainDir -> folder path for training images
    // targetDir -> folder path for classified images
    // runDir -> folder path of images to be classified
    // nCount -> neighbor count...
    public String trainDir, targetDir, runDir;
	public int nCount;
	KNN(   String trainDir, 
           String targetDir, 
           String runDir, 
           int nCount)
    {	
        this.trainDir = trainDir;
		this.nCount = nCount;
        this.targetDir = targetDir;
        this.runDir = runDir;
	}
        
    // EUCLIDEAN DISTANCE
    private double get_euclidean_distance(double[] testImage, double[] trainImage){
        double distance = 0;
        for(int i = 0; i < TrainData.PIXEL_SIZE; i++){
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
                executor.submit(() -> {
                    double distance = get_euclidean_distance(testVec, trainImage.features);
                    // set the distance attribute
                    trainImage.set_distance(distance);
                });
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
            // sort images based on distance
            TrainData.trainImages.sort((imga, imgb) -> {
                return Double.compare(imga.distance, imgb.distance); 
            });
            // get the neighbours
            ArrayList<String> neighbours = new ArrayList<>();
            Classifier classifier = new Classifier(this.targetDir);
            for(int x = 0; x < nCount; x++)
                neighbours.add(classifier.getFileNameOnly(
                    TrainData.trainImages.get(x).name));
            // extract the folder name from neighbours images name
            String folder_name = classifier.folder_name(neighbours);
            // move to the folder where neighbours live...
            classifier.move(sourceImage, folder_name, imageName);
            return true;
        }catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
}
