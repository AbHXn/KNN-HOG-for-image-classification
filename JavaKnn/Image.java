package JavaKnn;

import java.awt.image.BufferedImage;
import java.util.ArrayList;


class Image{
	public String name;
	public BufferedImage img;
	public double distance;
	public double[] features;

	Image(
		String name, 
		BufferedImage img)
	{
		this.name = name;
		this.img = img;
	}

	public void set_distance(double distance){
		this.distance = distance;
	}
	public void set_features(double[] features){
		this.features = features;
	}
}