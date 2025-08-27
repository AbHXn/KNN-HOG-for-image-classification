import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.util.*;
import java.awt.Color;
import java.awt.RenderingHints;
import java.util.concurrent.*;

class KNN{
    private int PIXEL_SIZE = 192;
    private int cellSize = 8;
    private int binCount = 9;
    private int blockSize = 2;

    public int[][][] KERNEL = {{{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}},
                                {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}}};
    public String train_dir, target_dir, run_dir;
	public int n_count;

	KNN(String train_dir, String target_dir, String run_dir, int n_count){
		this.train_dir = train_dir;
		this.n_count = n_count;
        this.target_dir = target_dir;
        this.run_dir = run_dir;
	}
    public BufferedImage resize(BufferedImage img, int width, int height) {
        try{
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, width, height, null);
            g.dispose();
            return resized;
        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }
    private ArrayList<Image> loadTrainingImages(){
        try{
            File dir = new File(this.train_dir);
            File[] contents = dir.listFiles();
            ArrayList<Image> trainImages = new ArrayList<>();
            for(File trainImage: contents){
                BufferedImage greyTrainImage = KNN.getGreyImage(""+trainImage);
                greyTrainImage = resize(greyTrainImage, PIXEL_SIZE, PIXEL_SIZE);
                Image new_image = new Image(trainImage.getName(), greyTrainImage);
                trainImages.add(new_image);
            }
            return trainImages;
        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }
	static BufferedImage getGreyImage(String source) {
        try {
            BufferedImage img = ImageIO.read(new File(source));
            BufferedImage grey = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = grey.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, null);
            g.dispose();
            return grey;
        } catch (IOException e) {
            System.out.println("Grey image error: " + e);
            return null;
        }
    }
    double[][] sobelZ(BufferedImage img, int k_index){
        int w = img.getWidth();
        int h = img.getHeight();
        int[][] kernel = KERNEL[k_index];
        double[][] gradient = new double[h][w];
        for(int y = 1; y < h-1; y++){
            for(int x = 1; x < w-1; x++){
                double sum = 0;
                for(int a = -1; a <= 1; a++){
                    for(int b = -1; b <= 1; b++){
                        int pix_value = img.getRaster().getSample(x+b, y+a, 0);
                        sum += pix_value * kernel[a+1][b+1];
                    }
                }
                gradient[y][x] = sum;
            }
        }
        return gradient;
    }
    double[][][] computeCellHistograms(double[][] magnitudes, double[][] angles){
        int h = magnitudes.length, w = magnitudes[0].length;
        int cellH = h / cellSize;
        int cellW = w / cellSize;
        double[][][] hist = new double[cellH][cellW][binCount];
        double binSize = 180.0 / binCount;
        for(int x = 0; x < cellH; x++){
            for(int y = 0; y < cellW; y++){
                for(int a = 0; a < cellSize; a++){
                    for(int b = 0; b < cellSize; b++){
                        int ix = x * cellSize + a;
                        int iy = y * cellSize + b;
                        double mag = magnitudes[ix][iy];
                        double ang = angles[ix][iy];
                        int bin = (int) (ang / binSize);
                        if(bin >= binCount) 
                            bin = binCount - 1;
                        hist[x][y][bin] += mag;
                    }
                }
            }
        }
        return hist;
    }
    ArrayList<Double> normalizeBlock(double[][][] hist){
        int h = hist.length;
        int w = hist[0].length;
        ArrayList<Double> features = new ArrayList<>();
        for(int x = 0; x < h - blockSize + 1; x++){
            for(int y = 0; y < w - blockSize + 1; y++){
                double[] block = new double[binCount * blockSize * blockSize];
                int index = 0; 
                for(int a = 0; a < blockSize; a++)
                    for(int b = 0; b < blockSize; b++)
                        for(int k = 0; k < binCount; k++)
                            block[index++] = hist[x+a][y+b][k];
                double norm = 0;
                for(double v: block)
                    norm += v * v;
                norm = Math.sqrt(norm + 1e-6);
                for(double v: block)
                    features.add(v / norm);
            }
        }
        return features;
    }
    ArrayList<Double> getImageVectors(BufferedImage img){
        int H = img.getHeight();
        int W = img.getWidth();
        double[][] sobelX = sobelZ(img, 0);
        double[][] sobelY = sobelZ(img, 1);
        double[][] angles = new double[H][W];
        double[][] magnitudes = new double[H][W];
        for(int x = 0; x < H; x++){
            for(int y = 0; y < W; y++){
                magnitudes[x][y] = sobelX[x][y] * sobelX[x][y] + sobelY[x][y] * sobelY[x][y];
                magnitudes[x][y] = Math.sqrt(magnitudes[x][y]);
                angles[x][y] = Math.toDegrees(Math.atan2(sobelY[x][y], sobelX[x][y]));
                if (angles[x][y] < 0) 
                    angles[x][y] += 180;
            }
        }
        double[][][] histograms = computeCellHistograms(magnitudes, angles);
        ArrayList<Double> features = normalizeBlock(histograms);
        return features;
    }
    public double getDistance(ArrayList<Double> testImage, ArrayList<Double> trainImage){
        double dist = 0;
        for(int i=0;i<testImage.size();i++){
            double diff = testImage.get(i) - trainImage.get(i);
            dist += diff * diff;
        }
        return Math.sqrt(dist);
    }
    public boolean knn_solver(String sourceImage, String imageName){
        try{
            ArrayList<Image> trainImages = loadTrainingImages();
            BufferedImage greyImage = getGreyImage(sourceImage);
            BufferedImage testImage = resize(greyImage, PIXEL_SIZE, PIXEL_SIZE); 
            ArrayList<Double> testVec = getImageVectors(testImage);
            List<Pair> imageDistances = Collections.synchronizedList(new ArrayList<>());
            ExecutorService executor = Executors.newFixedThreadPool(5);
            for(Image trainImage: trainImages){
                executor.submit(() -> {
                    ArrayList<Double> trainVec = getImageVectors(trainImage.img);
                    double distance = getDistance(testVec, trainVec);
                    Pair newPair = new Pair(trainImage.name, distance);
                    imageDistances.add(newPair);
                });
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
            imageDistances.sort((imga, imgb) -> {
                return Double.compare(imga.distance, imgb.distance); 
            });
            ArrayList<String> neighbours = new ArrayList<>();
            Classifier classifier = new Classifier(this.target_dir);
            for(int x = 0; x < n_count; x++)
                neighbours.add(classifier.getFileNameOnly(
                    imageDistances.get(x).name));
            String folder_name = classifier.folder_name(neighbours);
            classifier.move(sourceImage, folder_name, imageName);
            return true;
        }catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
}
