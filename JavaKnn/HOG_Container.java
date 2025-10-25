package JavaKnn;

import java.awt.image.BufferedImage;

class HOG_Container{
    static private int cellSize = 8;
    static private int binSize = 9;
    static private int blockSize = 2;
    static public int[][][] KERNEL = {
                {{-1, 0, 1}, 
                 {-2, 0, 2}, 
                 {-1, 0, 1}},

                {{-1, -2, -1}, 
                 {0, 0, 0}, 
                 {1, 2, 1}}
            };
    
    static private double[][][] sobelZ(BufferedImage img){
            int w = img.getWidth();
            int h = img.getHeight();
            int[][] kernel1 = KERNEL[0];
            int[][] kernel2 = KERNEL[1];

            double[][][] gradient = new double[2][h][w];
            // stride the sobel filter across the image and store the changes in gradients
            for(int y = 1; y < h-1; y++){
                for(int x = 1; x < w-1; x++){
                    double sum1 = 0, sum2 = 0;
                    for(int a = -1; a <= 1; a++){
                        for(int b = -1; b <= 1; b++){
                            // get the pixel at given pornts
                            int pix_value = img.getRaster().getSample(x+b, y+a, 0);
                            sum1 += pix_value * kernel1[a + 1][b + 1];
                            sum2 += pix_value * kernel2[a + 1][b + 1];
                        }
                    }
                    gradient[0][y][x] = sum1;
                    gradient[1][y][x] = sum2;
                }
            }
            return gradient;
        }

    static private double[][][] compute_cell_histogram(double[][] magnitudes, double[][] angles){
        // stride the cell size across the image 
        // each cell size has its own bins
        int h = magnitudes.length;
        int w = magnitudes[0].length;
        int H = h / cellSize;
        int W = w / cellSize;

        double[][][] hist = new double[H][W][binSize];
        double t_bins = 180.0 / binSize;   // angles range (0 - 180)

        for(int x = 0; x < H; x++){
            for(int y = 0; y < W; y++){
                for(int a = 0; a < cellSize; a++){
                    for(int b = 0; b < cellSize; b++){
                        double mag = magnitudes[x * cellSize + a][y * cellSize + b];
                        double ang = angles[x * cellSize + a][y * cellSize + b];
                        int bin = (int) (ang / t_bins);
                        if(bin >= binSize) 
                            bin = binSize - 1;
                        hist[x][y][bin] += mag;
                    }
                }
            }
        }
        return hist;
    }
    // method for nomalizing the block Pair
    static private double[] normalize_block(double[][][] hist){
        int h = hist.length;
        int w = hist[0].length;
        int cur_ptr = 0;
        int total_size = (h - blockSize + 1) * (w - blockSize + 1);
        total_size *= binSize * blockSize * blockSize;
        double []features = new double[total_size];

        for(int x = 0; x < h - blockSize + 1; x++){
            for(int y = 0; y < w - blockSize + 1; y++){
                double norm = 0;
                int copy_index = cur_ptr;
                // making 1d vectors..
                for(int a = 0; a < blockSize; a++){
                    for(int b = 0; b < blockSize; b++){
                        for(int k = 0; k < binSize; k++){
                            double hist_value = hist[x + a][y + b][k];
                            features[cur_ptr++] = hist_value;
                            norm += hist_value * hist_value;
                        }
                    }
                }
                // to avoid division by zero error
                norm = Math.sqrt(norm + 1e-6);
                for(int i = copy_index; i < cur_ptr; i++)
                    features[i] /= norm;
            }
        }
        return features;
    }
    static public double[] get_image_vectors(BufferedImage img){
        int H = img.getHeight();
        int W = img.getWidth();
        double [][][] sobel = sobelZ(img);
        double[][] sobelX = sobel[0];
        double[][] sobelY = sobel[1];
        double[][] angles = new double[H][W];
        double[][] magnitudes = new double[H][W];

        for(int x = 0; x < H; x++){
            for(int y = 0; y < W; y++){
                magnitudes[x][y] = sobelX[x][y] * sobelX[x][y] + sobelY[x][y] * sobelY[x][y];
                magnitudes[x][y] = Math.sqrt(magnitudes[x][y]);
                // tan(0) = dy/dy => 0 = tan-1(dy/dy)
                angles[x][y] = Math.toDegrees(Math.atan2(sobelY[x][y], sobelX[x][y]));
                // ensure the angle is between 0 - 180
                if (angles[x][y] < 0)  angles[x][y] += 180;
                if (angles[x][y] > 180) angles[x][y] -= 180;
            }
        }
        // compute historgram, nomalize and return the vector
        double[][][] histograms = compute_cell_histogram(magnitudes, angles);
        double[] features = normalize_block(histograms);
        return features;
    }
}