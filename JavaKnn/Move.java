package JavaKnn;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

class Move{
    private String classFolder;
    Move(String classFolder){
        this.classFolder = classFolder;
    }
    void move(String source, String dest, String imageName){
        dest = this.classFolder.concat("/").concat(dest);
        File directory = new File(dest);
        if(!directory.exists()){
            if(directory.mkdirs()){
                System.out.println("New DIR Created");
            }else{
                System.out.println("Failed to Create DIR");
                return;
            }
        }
        dest = dest + "/" + imageName;
        try {
            Files.move(
                Paths.get(source),
                Paths.get(dest),
                StandardCopyOption.REPLACE_EXISTING
            );
            System.out.println("File moved to: " + dest);
        } catch (IOException e) {
            System.out.println("Error moving file: " + e.getMessage());
        }
    }

    String folder_name(ArrayList<String> neighbours){
        HashMap<String, Integer> cnts = new HashMap<>();
        for(String file: neighbours){
            if(cnts.containsKey(file))
                cnts.put(file, cnts.get(file)+1);
            else cnts.put(file, 1);
        }
        int max_cnts = 0;
        String folderName = "";
        for(String key: cnts.keySet()){
            int score = cnts.get(key);
            if(score > max_cnts){
                max_cnts = score;
                folderName = key;
            }
        }
        return folderName;
    }
    String getFileNameOnly(String filename){
        String[] parts = filename.split("/");
        filename = parts[parts.length - 1];
        String folderName = "";
        for(int x = 0; x < filename.length(); x++){
            if(Character.isLetter(filename.charAt(x)))
                folderName = folderName.concat(""+filename.charAt(x));
            else break;
        }
        System.out.println(filename);
        return folderName.toUpperCase();
    }
}