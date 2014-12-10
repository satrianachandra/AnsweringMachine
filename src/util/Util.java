/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 *
 * @author chandra
 */
public class Util {
    
    public static void doOrDie(String reason,boolean result){
        if (!result){
            System.out.println("Error: "+reason);
        }
    }
    
    public static void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.delete()) {
                System.out.println(file.getName() + " is deleted!");
            } else {
                System.out.println("Delete operation is failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static long getFileSize(String filePath){
        File file =new File(filePath);
        if(file.exists()){
            return file.length();
        }else{
            return -1;
        }
    }
    
    public static void copyFile(String sourceFileName, String destFileName) throws IOException {
        File sourceFile = new File(sourceFileName);
        File destFile = new File(destFileName);
        
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    
}
