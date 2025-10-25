package JavaKnn;

import java.io.File;

public class Main{
	public static void main(String[] args){
        KNN test = new KNN("./train_data", "./library", "./runner", 7);
        File dir = new File("./runner");
        // load training data
        boolean isLoaded = TrainData.load_training_data("./train_data");
        if(isLoaded){
        	File[] contents = dir.listFiles();
			for (File content : contents) 
				test.knn_solver("./runner/"+content.getName(), content.getName());
		}else{
			System.out.println("Failed to load train images");
		}
	}
}
