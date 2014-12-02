/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author chandra
 */
public class Config {
    
    public static String serverAddress = "127.0.0.1";
    public static final int serverPort = 5060;
    
    public static final String myClientAddress = "127.0.0.1";
    public static final int clientPort = 5050;
    //public static final int clientRTPPort = 4040;
    
    public static final String MESSAGE_RECORDING_ROOT = "/home/chandra/voicemailmessages/";
    
    public static final String WELCOME_SOUND = "/home/chandra/NetBeansProjects/AnsweringMachine/lib/voicemailgreetings.mp3";
    
    public static final String LIST_MESSAGE = "ListMessages"; 
    public static final String LIST_MESSAGE_RESULT = "ListMessagesResult";
    public static final String LISTEN_MESSAGE = "PlayMessage";
    
    
}
