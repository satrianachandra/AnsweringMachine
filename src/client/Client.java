/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.Gst;
import util.Config;

/**
 *
 * @author chandra
 */
public final class Client {
    
    private GUIClient guiClient;
    private String myName;
    
    private ClientAudioReceiver caReceiver;
    private ClientAudioSender caSender;
    
    private SipListenerClient slc;
    
    public Client(){
        // initialize GSstreamer with some debug
        Gst.init("SIP Voicemail", new String[] { "--gst-debug-level=2",
                        "--gst-debug-no-color" });
        
        //begin slc initialization and sip init
        slc = new SipListenerClient(this);
        
        //Start receiving from the udpsrc
        startReceiving();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        slc.init();
        System.out.println("Voice Mail Client listening on "+slc.getPort());
        
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                guiClient = new GUIClient();
                guiClient.setVisible(true);
            }
        });
        
        
    }

    public ClientAudioReceiver getCAReceiver(){
        return caReceiver;
    }
    
    public ClientAudioSender getCASender(){
        return caSender;
    }
    
    void startSending(String serverAddress, int serverRtpPort) {
        caSender = new ClientAudioSender(Config.serverAddress, serverRtpPort);
        caSender.play();
    }

    void startReceiving() {
        caReceiver = new ClientAudioReceiver();
        caReceiver.play();
    }

    void signIn(String myName) {
        this.myName = myName;
        guiClient.getLabelMainMessage().setText("Hi, "+myName+" these are your messages: ");
    }
    
    public String getMyName(){
        return myName;
    }
    
    public static void main(String []args){
        Client client = new Client();
        System.out.println("client started");
        
        
    }
    
}
