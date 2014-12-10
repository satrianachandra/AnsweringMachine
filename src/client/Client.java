/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gstreamer.Gst;
import server.Server;
import util.Config;

/**
 *
 * @author chandra
 */
public final class Client {
    
    private GUIClient guiClient;
    private String myName;
    private String myEmail;
    
    private ClientAudioReceiver caReceiver;
    private ClientAudioSender caSender;
    
    private SipListenerClient sipListener;
    
    private List<String>messagesList = new ArrayList<>();
    
    public Client(){
        // initialize GSstreamer with some debug
        Gst.init("SIP Voicemail", new String[] { "--gst-debug-level=2",
                        "--gst-debug-no-color" });
        
        //begin slc initialization and sip init
        sipListener = new SipListenerClient(this);
        
        //Start receiving from the udpsrc
        startReceiving();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        //slc.init();  //moving init to when signin happen
        //System.out.println("Voice Mail Client listening on "+slc.getPort());
        
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                guiClient = new GUIClient(Client.this);
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
        if (myName.contains("@")){ //simple validation for email
            guiClient.getPanelWelcome().setVisible(false);
            guiClient.getPanelMain().setVisible(true);
            
            this.myName = myName.split("@")[0];
            this.myEmail = myName;
            try{
                sipListener.init();
                System.out.println("Voice Mail Client listening on "+sipListener.getPort());
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
        
    }
    
    public String getMyName(){
        return myName;
    }
    
    public String getMyEmail(){
        return myEmail;
    }
    
    
    public void updateVoiceMailMessagesList(List<String> list) {
        messagesList = list;
         if (messagesList!=null){
            String[]messagesArray = new String[messagesList.size()];
            for (int i=0;i<messagesArray.length;i++){
                messagesArray[i]=messagesList.get(i);
                System.out.println(messagesArray[i]);
            }
            guiClient.getListMessagesList().setModel(new javax.swing.DefaultComboBoxModel(messagesArray));
        }
        
    }

    public void leaveAMessage(String toName) {
        if (toName.contains("@")){ //simple validation for email
            sipListener.sendLeaveAMessageRequest(toName);
            guiClient.getButtonStopLeavingAMessage().setEnabled(true);
            guiClient.getButtonLeaveAMessage().setEnabled(false);
        }
    }

    void stopSendingMessage() {
        //stop receiver, dont stop yet
        caReceiver.stop();
        
        //stop sender
        caSender.stop();
        sipListener.sendBye();
        guiClient.getButtonLeaveAMessage().setEnabled(true);
        guiClient.getButtonStopLeavingAMessage().setEnabled(false);
    }

    void listenMessage(int selectedMessage) {
        startReceiving();
        sipListener.listenMessage(selectedMessage);
    }

    void deleteMessage(int selectedMessage) {
        messagesList.remove(selectedMessage);
        updateVoiceMailMessagesList(messagesList);
        sipListener.removeMessage(selectedMessage);
    }
    
    void forwardMessage(int selectedMessage, String destClient) {
        if (destClient.contains("@")){ //simple checking for email field
            sipListener.forwardMessage(selectedMessage,destClient);
        }
    }

    void refreshMessagesList() {
        sipListener.sendMessagesListRequest();
    }
    
    
    
    public static void main(String []args){
        
        
        //start the server
        Server server = new Server();
        System.out.println("server created");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //start the client
        Client client = new Client();
        System.out.println("client started");
    }

    
}
