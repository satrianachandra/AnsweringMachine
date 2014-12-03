/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;

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
    
}
