/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chandra
 */
public class VoiceMessageData {
    
    private String calleeName;
    private List<String>listOfMessagesFile;
    
    public VoiceMessageData(String calleeName, List<String> listOfMessagesFile){
        this.calleeName = calleeName;
        this.listOfMessagesFile = listOfMessagesFile;
    }
    
    public VoiceMessageData(String calleeName){
        this.calleeName = calleeName;
        this.listOfMessagesFile = new ArrayList<>();
    }
    
    public String getCalleeName(){
        return calleeName;
    }
    
    public List<String>getListOfMessagesFile(){
        return this.listOfMessagesFile;
    }
    
    public void setListOfMessagesFile(List<String>listOfMessagesFile){
        this.listOfMessagesFile = listOfMessagesFile;
    }
    
    
    
    
}
