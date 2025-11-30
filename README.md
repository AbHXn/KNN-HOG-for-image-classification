# KNN Image Classifier

This is a Java-based K-Nearest Neighbors (KNN) image classifier. It classifies test images based on a set of training images and moves the classified images into the appropriate folders.

## Folder Structure

- `train_data/` → Contains the training images. Images must be named as `ClassName<number>.png` (e.g., `Car1.png`).  
- `runner/` → Place test images here. This is the folder where the program executes.  
- `library/` → The program moves classified images here, inside folders named after their predicted classes.

## How It Works

- `trainDir` → Path to `train_data/` (training images).  
- `runDir` → Path to `runner/` (images to classify).  
- `targetDir` → Path to `library/` (where classified images are stored).  
- `dRate` → Neighbor selection ratio for KNN. It is a fraction (0–1) of the total training images used as neighbors. For example, `dRate = 0.1` means the closest 10% of training images are considered during classification.  

### Example:

```java
// Create a KNN classifier
KNN classifier = new KNN("train_data", "library", "runner", 0.1);

// Classify a single image
classifier.knn_solver("runner/test1.png", "test1.png");
```

## Usage

1. Compile the program:

```bash
javac Main.java
```

2. Run the program:

```bash
java Main
```

- Place test images inside `runner/`.  
- Classified images will automatically be moved to `library/` under their predicted class folder.

## Notes

- Training images must **not** be in subfolders, only in `train_data/`.  
- Test images are classified based on nearest neighbors determined by the `dRate` fraction.  
- The classifier uses **Euclidean distance** on HOG features extracted from the images.

