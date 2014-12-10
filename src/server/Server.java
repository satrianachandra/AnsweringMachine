/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import util.Config;
import server.FileStreamer;
import server.MessageRecorder;
import gov.nist.javax.sip.address.SipUri;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sdp.SdpException;
import javax.sdp.SessionDescription;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import org.gstreamer.Bus;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;
import org.gstreamer.State;
import util.SdpTool;
import util.SendMailTLS;
import util.Util;

/**
 *
 * @author chandra
 */
public class Server implements SipListener{

    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private SipStack sipStack;

    /** My IP address (to put in SDP offers) */
    private String myAddress;
    /** SIP listening port */
    private int myPort;
    /** Main application API */
    //private App app;

    //private List<MessageRecorder>listOfMessageRecorders;
    
    private List<VoiceMessageData>listOfVoiceMessageDatas;
    
    SendMailTLS sendMail;
    
    
    public Server(){
        this.myAddress = Config.serverAddress;
        this.myPort = Config.serverPort;
        
        //listOfMessageRecorders = new ArrayList<>();
        listOfVoiceMessageDatas = new ArrayList<>();
        
        // initialize GSstreamer with some debug
        Gst.init("SIP Voicemail", new String[] { "--gst-debug-level=2",
                        "--gst-debug-no-color" });
        
        init();
        System.out.println("Voice Mail Server listening on "+this.myPort);
        
        sendMail =  new SendMailTLS();
        
    }
    
    //processing INVITE, BYE, ACK,
    @Override
    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransaction = requestEvent
                        .getServerTransaction();

        if (serverTransaction == null) {
                System.out.println("Request " + request.getMethod()
                                + " with no server transaction yet");
        } else {
                System.out.println("Request " + request.getMethod()
                                + " with server transaction id "
                                + serverTransaction.getBranchId() + " and dialog id "
                                + serverTransaction.getDialog().getDialogId());
        }
        
        if (request.getMethod().equals(Request.INVITE)) {
               processInvite(requestEvent, serverTransaction);
        }else if (request.getMethod().equals(Request.MESSAGE)) {
               processMessage(requestEvent, serverTransaction);
        }else if (request.getMethod().equals(Request.ACK)) {
               // processAck(requestEvent, serverTransaction);
        } else if (request.getMethod().equals(Request.BYE)) {
               processBye(requestEvent, serverTransaction);
        } else if (request.getMethod().equals(Request.CANCEL)) {
                processCancel(requestEvent, serverTransaction);
        } else {
                try {
                        serverTransaction.sendResponse(messageFactory.createResponse(
                                        202, request));

                        // send one back
                        SipProvider prov = (SipProvider) requestEvent.getSource();
                        Request refer = requestEvent.getDialog().createRequest("REFER");
                        requestEvent.getDialog().sendRequest(
                                        prov.getNewClientTransaction(refer));

                } catch (SipException e) {
                        e.printStackTrace();
                } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                } catch (ParseException e) {
                        e.printStackTrace();
                }
        }
        
        
        
    }

    @Override
    public void processResponse(ResponseEvent re) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
                transaction = timeoutEvent.getServerTransaction();
        } else {
                transaction = timeoutEvent.getClientTransaction();
        }
        System.out.println("Transaction timeout, state = "
                        + transaction.getState() + ", dialog = "
                        + transaction.getDialog() + ", dialogState = "
                        + transaction.getDialog().getState());
    }

    @Override
    public void processIOException(IOExceptionEvent ioee) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent tte) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dte) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void processBye(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
        Request request = requestEvent.getRequest();
        try {
                // 200 OK
                System.out.println("Got a bye");
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                
                //stop the receiver, it should actually first search from who:
                String caller = ((SipUri) ((FromHeader) request
                                .getHeader("from")).getAddress().getURI())
                                .getAuthority().getUser();
                VoiceMessageData aVoiceMessageData = getVoiceDataByName(caller);
                MessageRecorder aMessageRecorder = aVoiceMessageData.getMessageRecorder();
                aMessageRecorder.stopPipeline();
                
                //send the mail notification to the callee of this message recorder
                if (aMessageRecorder.getSavedMessageLocation()!=null){
                    sendMail.sendmail(aMessageRecorder.getCalleeEmail(), aMessageRecorder.getSavedMessageLocation());
                }
                
        } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
        }
    }
    
    public void processCancel(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
        Request request = requestEvent.getRequest();
        try {
                System.out.println("VoiceMailServer got a cancel");
                if (serverTransactionId == null) {
                        System.err
                                        .println("Got a cancel for an unexisting transaction");
                        return;
                }
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                if (requestEvent.getDialog().getState() != DialogState.CONFIRMED) {
                        response = messageFactory.createResponse(
                                        Response.REQUEST_TERMINATED, request);
                        requestEvent.getServerTransaction().sendResponse(response);
                }

        } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
        }
    }
    
    private void processMessage(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        ServerTransaction st = requestEvent.getServerTransaction();
        
        if (st == null) {
            try {
                st = sipProvider.getNewServerTransaction(request);
                if (st.getState() != TransactionState.COMPLETED) {
                    Header messageTypeHeader = request.getHeader("Message-Type");
                    String messageTypeHeaderString0 = messageTypeHeader.toString().split(" ")[1];
                    String messageTypeHeaderString = messageTypeHeaderString0.substring(0,messageTypeHeaderString0.length()-2);
                    
                    String caller = ((SipUri) ((FromHeader) request
                                .getHeader("from")).getAddress().getURI())
                                .getAuthority().getUser();
                    String callerSipAddress = ((SipUri) ((FromHeader) request
                                .getHeader("from")).getAddress().getURI())
                                .getAuthority().getHost().getHostname();
                    
                    System.out.println("Receive request Message from "+caller);
                    if (messageTypeHeaderString.equalsIgnoreCase(Config.LIST_MESSAGE)){

                        //add this user to the voice messages list, if not yet,
                        //this is not a good approach, we actually need to send SIGN_IN message
                        //then add the user into this voice messages list
                        boolean exist=false;
                        int userPosition = -1;
                        for (int i=0;i<listOfVoiceMessageDatas.size();i++){
                            VoiceMessageData aData = listOfVoiceMessageDatas.get(i);
                            if (aData.getCalleeName().equalsIgnoreCase(caller)){
                                exist=true;
                                userPosition = i;
                            }
                        }

                        if (!exist){
                            listOfVoiceMessageDatas.add(new VoiceMessageData(caller, callerSipAddress));
                            userPosition = listOfVoiceMessageDatas.size()-1;
                        }
                        
                        System.out.println(caller+" is requesting to list his/her messages");
                        File f = new File(Config.MESSAGE_RECORDING_ROOT+caller+"/");
                        if (f.list()!=null){
                            List<String> fileNames = new ArrayList<String>(Arrays.asList(f.list()));
                            
                            listOfVoiceMessageDatas.get(userPosition).setListOfMessagesFile(fileNames);
                            
                            Response response = messageFactory.createResponse(
                                Response.OK, request);

                            Header extensionHeader = headerFactory.createHeader("Message-Type",
                                    Config.LIST_MESSAGE_RESULT);
                            response.addHeader(extensionHeader);


                            ByteArrayOutputStream bosFileNames = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(bosFileNames);
                            oos.writeObject(fileNames);
                            byte[] bytesFileNames = bosFileNames.toByteArray();

                            response.setContent(bytesFileNames,
                                    headerFactory.createContentTypeHeader(
                                            "application", "ListMessagesResult"));

                            st.sendResponse(response);
                        }
                        
                    }else if(messageTypeHeaderString.equalsIgnoreCase(Config.LISTEN_MESSAGE)){
                        String contentString = new String(request.getRawContent());
                        System.out.println("|"+contentString+"|");
                        String[]contentStringSplitted = contentString.split("#");
                        
                        int selectedFile = Integer.parseInt(contentStringSplitted[0]);
                        System.out.println("playing file "+selectedFile);
                        
                        String clientIP = contentStringSplitted[1];
                        int clientsrtpport = Integer.parseInt(contentStringSplitted[2]);
                        
                        VoiceMessageData callersData = getVoiceDataByName(caller);
                        play(caller,callersData.getListOfMessagesFile().get(selectedFile),clientIP,clientsrtpport);
                        
                        
                    }else if (messageTypeHeaderString.equalsIgnoreCase(Config.DELETE_MESSAGE)){
                        String contentString = new String(request.getRawContent());
                        int messageToDelPos = Integer.parseInt(contentString);
                        
                        VoiceMessageData callersData = getVoiceDataByName(caller);
                        String fileLocation = Config.MESSAGE_RECORDING_ROOT+caller+"/"+callersData.getListOfMessagesFile().get(messageToDelPos);
                        System.out.println("file to delete: "+fileLocation);
                        Util.deleteFile(fileLocation);
                        callersData.getListOfMessagesFile().remove(messageToDelPos);
                        
                    }else if (messageTypeHeaderString.equalsIgnoreCase(Config.FORWARD_MESSAGE)){
                        String contentString = new String(request.getRawContent());
                        String[] contentArray = contentString.split("#");
                        int messageToForwardPos = Integer.parseInt(contentArray[0]);
                        String destClient = contentArray[1];
                        String destClientName = destClient.split("@")[0];
                        
                        //get the file to copy
                        VoiceMessageData callersData = getVoiceDataByName(caller);
                        String fileName = callersData.getListOfMessagesFile().get(messageToForwardPos);
                        String fileLocationSrc = Config.MESSAGE_RECORDING_ROOT+caller+"/"+fileName;
                        
                        //copy the file to the right place
                        String fileLocationDest = Config.MESSAGE_RECORDING_ROOT+destClientName+"/"+fileName;
                        Util.copyFile(fileLocationSrc, fileLocationDest);
                        
                        //notify the destuser by email
                        sendMail.sendmail(destClient, fileLocationDest);
                    }

                }
            } catch (TransactionAlreadyExistsException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransactionUnavailableException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex){
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            System.out.println("st is not null");
        }
        
    }
    
    private void processInvite(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        
        ServerTransaction st = requestEvent.getServerTransaction();
        // create the transaction if not existing yet
        if (st == null) {
            try {
                st = sipProvider.getNewServerTransaction(request);
                if (st.getState() != TransactionState.COMPLETED) {
                    // get info from client's SDP offer
                    SessionDescription clientSdp = null;
                    clientSdp = SdpTool.fromString(new String(request.getRawContent()));

                    String clientAddr = SdpTool.getIpAddress(clientSdp);
                    System.out.println("Client SDP medias "
                            + clientSdp.getMediaDescriptions(false).toString());
                    int myRtpListenPort = -1;
                    int clientRtpPort = SdpTool.getAudioMediaPort(clientSdp);
                    if (clientRtpPort != -1) {
                        System.out.println("Client wants sound at " + clientAddr
                                + ":" + clientRtpPort);

                        // get caller and callee names from request
                        String callee = ((SipUri) ((ToHeader) request
                                .getHeader("to")).getAddress().getURI())
                                .getAuthority().getUser();
                        String caller = ((SipUri) ((FromHeader) request
                                .getHeader("from")).getAddress().getURI())
                                .getAuthority().getUser();
                        
                        String calleeSipAddress = ((SipUri) ((ToHeader) request
                                .getHeader("to")).getAddress().getURI())
                                .getAuthority().getHost().getHostname();
                        System.out.println("callee sip address: "+calleeSipAddress);
                        System.out.println("callee " + callee + ", caller " + caller);
                        myRtpListenPort = recordMessage(clientAddr, clientRtpPort,
                                callee, caller,calleeSipAddress);
                    } else {
                        System.err
                                .println("Client didn't give any port for audio stream");
                        // TODO cancel everything with proper message
                    }

                    // create my SDP offer
                    SessionDescription serverSdp = SdpTool.fromString("v=0\n"// protocol
                            // version
                            + "o=Voicemail "
                            + new Date().getTime()
                            + " "
                            + new Date().getTime()
                            + " IN IP4 "
                            + myAddress
                            + "\n"
                            + "c= IN IP4 "
                            + myAddress
                            + "\n"
                            + // originator
                            "s=Voicemail\n"
                            + // session name
                            "t=0 0\n"
                            + "m=audio "
                            + myRtpListenPort
                            + " RTP/AVP 96\n"
                            + "a=rtcp:"
                            + (myRtpListenPort + 1)
                            + "\n"
                            + "a=rtpmap:96 speex/16000");
                    // + "a=fmtp:96 mode=\"10,any\"");
                    System.out.println("MyRTPPort: "+myRtpListenPort);
                    // prepare response message
                    Response response = messageFactory.createResponse(
                            Response.OK, request);
                    Address address = addressFactory
                            .createAddress("Voicemail <sip:" + myAddress + ":"
                                    + myRtpListenPort + ">");
                    ContactHeader contactHeader = headerFactory
                            .createContactHeader(address);
                    response.addHeader(contactHeader);
                    ToHeader toHeader = (ToHeader) response
                            .getHeader(ToHeader.NAME);
                    toHeader.setTag("4321"); // Application is supposed to set.
                    response.setContent(serverSdp.toString().getBytes(),
                            headerFactory.createContentTypeHeader(
                                    "application", "sdp"));

                    st.sendResponse(response);

                }
                
            } catch (TransactionAlreadyExistsException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransactionUnavailableException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SdpException | ParseException | InvalidArgumentException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SipException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    
    private void init(){
        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "voicemailserver");
        
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                        "voicemailserverdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                        "voicemailserverlog.txt");

        try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);
        } catch (PeerUnavailableException e) {
                // could not find
                // gov.nist.jain.protocol.ip.sip.SipStackImpl
                // in the classpath
                e.printStackTrace();
                System.err.println(e.getMessage());
                if (e.getCause() != null)
                        e.getCause().printStackTrace();
                System.exit(0);
        }
        
        try {
                // create all the useful factories provided by JAIN
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
                //sdpTool = new SdpTool();
                
                //start listening
                ListeningPoint lp = sipStack.createListeningPoint(myAddress,
                                myPort, "udp");

                SipProvider sipProvider = sipStack.createSipProvider(lp);
                sipProvider.addSipListener(this);
        } catch (Exception ex) {
                System.err.println(ex.getMessage());
                ex.printStackTrace();
        }
        System.out.println("SIP init completed");
        
    }
    
    
    private int recordMessage(String clientAddr, int clientRtpPort, String calleeName, String callerName, String calleeSipAddress) {
        // prepare and start receiving message
        final MessageRecorder messageRecorder = new MessageRecorder(callerName, calleeName,calleeSipAddress);
        VoiceMessageData aVoiceMessageData = getVoiceDataByName(callerName);
        aVoiceMessageData.setMessageRecorder(messageRecorder);
        
        // send the busy tone
        final FileStreamer busyTone = new FileStreamer(Config.WELCOME_SOUND,clientAddr, clientRtpPort);
        busyTone.getBus().connect(new Bus.EOS() {
                public void endOfStream(GstObject source) {
                        /*
                         * when the welcome message has been fully played, launch
                         * recording of message
                         */
                        busyTone.stop();
                        messageRecorder.play();
                }
        });
        // play it
        busyTone.play();

        
        return messageRecorder.getPort();
    }
    
    
    /*
    public static void main(String []args){
        //start the server
        Server server = new Server();
        System.out.println("server created");
        
    }
    */

    private VoiceMessageData getVoiceDataByName(String name){
        for(VoiceMessageData aData: listOfVoiceMessageDatas){
            if (aData.getCalleeName().equalsIgnoreCase(name)){
                return aData;
            }
        }
        return null;
    }
    
    private void play(String calleeName, String filePath,String clientAddr, int clientRtpPort) {
        final FileStreamer fileStreamer = new FileStreamer(Config.MESSAGE_RECORDING_ROOT+calleeName+"/"+filePath,clientAddr, clientRtpPort);
        fileStreamer.getBus().connect(new Bus.EOS() {
                public void endOfStream(GstObject source) {
                        /*
                         * when the welcome message has been fully played, launch
                         * recording of message
                         */
                        fileStreamer.stop();
                }
        });
        // play it
        fileStreamer.play();
    }

    
    

   
}
