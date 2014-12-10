/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import util.Config;
import client.ClientAudioReceiver;
import client.ClientAudioSender;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sdp.SessionDescription;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
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
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import org.gstreamer.Gst;
import util.SdpTool;

/**
 *
 * @author chandra
 */
public class SipListenerClient implements SipListener{
    
    private Client client;
    
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private SipStack sipStack;
    private ListeningPoint lp;
    private SipProvider sipProvider;
    private ContactHeader contactHeader;
    
    private Dialog dialog;
    
    private String myAddress;
    private int myPort;
    
    private final String peerHostPort = Config.serverAddress+":"+Config.serverPort;
    private final String transport = "udp";
   // private int myRTPPort;
    
    public SipListenerClient(Client clientt){
        this.myAddress = Config.myClientAddress;
        this.myPort = Config.clientPort;
        this.client = clientt;
    }
    
    public int getPort(){
        return myPort;
    }
    
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
        
        // We are the UAC so the only request we get is the BYE.
        if (request.getMethod().equals(Request.BYE))
                processBye(requestEvent, serverTransaction);
        else {
            try {
                    serverTransaction.sendResponse(messageFactory.createResponse(
                                    202, request));
            } catch (SipException | InvalidArgumentException | ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
        }
        
        
    }

    // Save the created ACK request, to respond to retransmitted 2xx
    private Request ackRequest;

    
    @Override
    public void processResponse(ResponseEvent responseReceivedEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        ClientTransaction tid = responseReceivedEvent.getClientTransaction();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        
        System.out.println("Response received : Status Code = "
				+ response.getStatusCode() + " " + cseq);
        if (tid == null) {
            // RFC3261: MUST respond to every 2xx
            if (ackRequest != null && dialog != null) {
                System.out.println("re-sending ACK");
                try {
                    dialog.sendAck(ackRequest);
                } catch (SipException se) {
                    se.printStackTrace();
                }
            }
            return;
        }
        
        System.out.println("transaction state is " + tid.getState());
        System.out.println("Dialog = " + tid.getDialog());
        if (tid.getDialog()!=null){
            System.out.println("Dialog State is " + tid.getDialog().getState());
        }
        
        try {
            if (response.getStatusCode() == Response.OK) {
                if (cseq.getMethod().equals(Request.INVITE)) {
                    System.out.println("Dialog after 200 OK  " + dialog);
                    System.out.println("Dialog State after 200 OK  "
                            + dialog.getState());
                    ackRequest = dialog.createAck(((CSeqHeader) response
                            .getHeader(CSeqHeader.NAME)).getSeqNumber());
                    System.out.println("Sending ACK");
                    dialog.sendAck(ackRequest);
                    
                    
                    
                    SessionDescription serverSdp = null;
                    serverSdp = SdpTool.fromString(new String(response.getRawContent()));

                    String clientAddr = SdpTool.getIpAddress(serverSdp);
                    System.out.println("Server's SDP medias "
                            + serverSdp.getMediaDescriptions(false).toString());
                    
                    int serverRtpPort = SdpTool.getAudioMediaPort(serverSdp);
                    
                    if (serverRtpPort!= -1){
                        //Start sending to the udpsink
                        System.out.println("Start sendint to server at port: "+serverRtpPort);
                        client.startSending(Config.serverAddress, serverRtpPort);
                        //caSender = new ClientAudioSender(Config.serverAddress, serverRtpPort);
                        //caSender.play();
                    }
                    
                    
                    
                } else if (cseq.getMethod().equals(Request.CANCEL)) {
                    if (dialog.getState() == DialogState.CONFIRMED) {
                                    // oops cancel went in too late. Need to hang up the
                        // dialog.
                        System.out
                                .println("Sending BYE -- cancel went in too late !!");
                        Request byeRequest = dialog.createRequest(Request.BYE);
                        ClientTransaction ct = sipProvider
                                .getNewClientTransaction(byeRequest);
                        dialog.sendRequest(ct);

                    }

                }else if (cseq.getMethod().equals(Request.MESSAGE)){
                    Header messageTypeHeader = response.getHeader("Message-Type");
                    String messageTypeHeaderString0 = messageTypeHeader.toString().split(" ")[1];
                    String messageTypeHeaderString = messageTypeHeaderString0.substring(0,messageTypeHeaderString0.length()-2);
                    if (messageTypeHeaderString.equalsIgnoreCase(Config.LIST_MESSAGE_RESULT)){
                        System.out.println("received list message result");
                        
                        byte[] bytesContent = response.getRawContent();
                        
                        ByteArrayInputStream bisFilleNames = new ByteArrayInputStream(bytesContent);
                        ObjectInputStream ois = new ObjectInputStream(bisFilleNames);
                        List<String> fileNamesList = (List<String>)ois.readObject();
                        client.updateVoiceMailMessagesList(fileNamesList);
                        
                    }
                    
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        

    }

    @Override
    public void processTimeout(TimeoutEvent te) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
        }
    }
    
    
    public final void init(){
        
        
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        
        Properties properties = new Properties();
        
        properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
                        + transport);
        properties.setProperty("javax.sip.STACK_NAME", "voicemailclient");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                        "voicemailclientdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                        "voicemailclientlog.txt");
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                        "false");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");

        try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);
                System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException e) {
                // could not find
                // gov.nist.jain.protocol.ip.sip.SipStackImpl
                // in the classpath
                e.printStackTrace();
                System.err.println(e.getMessage());
                System.exit(0);
        }
        
        try {
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
                lp = sipStack.createListeningPoint(myAddress,
                                myPort, "udp");
                System.out.println("listeningPoint = " + lp);
                sipProvider = sipStack.createSipProvider(lp);
                System.out.println("SipProvider = " + sipProvider);
                SipListenerClient listener = this;
                sipProvider.addSipListener(listener);

                String fromName = client.getMyName();
                String fromSipAddress = "gmail.com";
                String fromDisplayName = client.getMyName();

                String toSipAddress = "gmail.com";
                String toUser = "Callee1";
                String toDisplayName = "The Callee 1";

                // create >From Header
                SipURI fromAddress = addressFactory.createSipURI(fromName,
                                fromSipAddress);

                Address fromNameAddress = addressFactory.createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
                FromHeader fromHeader = headerFactory.createFromHeader(
                                fromNameAddress, "12345");

                // create To Header
                SipURI toAddress = addressFactory
                                .createSipURI(toUser, toSipAddress);
                Address toNameAddress = addressFactory.createAddress(toAddress);
                toNameAddress.setDisplayName(toDisplayName);
                ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                                null);

                // create Request URI
                SipURI requestURI = addressFactory.createSipURI(toUser,
                                peerHostPort);

                // Create ViaHeaders

                ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
                String ipAddress = lp.getIPAddress();
                ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
                                sipProvider.getListeningPoint(transport).getPort(),
                                transport, null);

                // add via headers
                viaHeaders.add(viaHeader);

                

                // Create a new CallId header
                CallIdHeader callIdHeader = sipProvider.getNewCallId();

                // Create a new Cseq header
                CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                                Request.MESSAGE);

                // Create a new MaxForwardsHeader
                MaxForwardsHeader maxForwards = headerFactory
                                .createMaxForwardsHeader(70);

                
                // Create the request.
                Request request = messageFactory.createRequest(requestURI,
                                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                                toHeader, viaHeaders, maxForwards);
                // Create contact headers
                String host = Config.myClientAddress;

                SipURI contactUrl = addressFactory.createSipURI(fromName, host);
                contactUrl.setPort(lp.getPort());
                contactUrl.setLrParam();

                // Create the contact name address.
                SipURI contactURI = addressFactory.createSipURI(fromName, host);
                contactURI.setPort(sipProvider.getListeningPoint(transport)
                                .getPort());

                Address contactAddress = addressFactory.createAddress(contactURI);

                // Add the contact address.
                contactAddress.setDisplayName(fromName);

                contactHeader = headerFactory.createContactHeader(contactAddress);
                request.addHeader(contactHeader);

                // You can add extension headers of your own making
                // to the outgoing SIP request.
                // Add the extension header.
                Header extensionHeader = headerFactory.createHeader("Message-Type",
                                Config.LIST_MESSAGE);
                request.addHeader(extensionHeader);
                
                //byte[] contents = client.getMyName().getBytes();

                //request.setContent(contents, contentTypeHeader);

                // Create the client transaction.
                ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);

                System.out.println("inviteTid = " + inviteTid);

                // send the request out.

                inviteTid.sendRequest();
                System.out.println("first list request sent to server");
                
                dialog = inviteTid.getDialog();

        } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
                //usage();
        }

        
    }
    
    public void sendBye(){
        try {
            Request byeRequest = dialog.createRequest(Request.BYE);
            ClientTransaction ct = sipProvider
                            .getNewClientTransaction(byeRequest);
            dialog.sendRequest(ct);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        System.out.println("Sent bye.");
        
        
    }
    
    
    public void sendLeaveAMessageRequest(String toName){
        //currently we assume that sip address = email of users,
        //just to make it simpler
        String justTheName = toName.split("@")[0];
        String justTheHost = toName.split("@")[1];
        
        try {
            /////////////
            String fromName = client.getMyName();
            String fromSipAddress = client.getMyEmail().split("@")[1];
            String fromDisplayName = client.getMyName();

            String toSipAddress = justTheHost;
            String toUser = justTheName;
            String toDisplayName = justTheName;

            // create >From Header        
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, "12345");

            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                    null);

            // create Request URI
            SipURI requestURI = addressFactory.createSipURI(toUser,
                    peerHostPort);

                // Create ViaHeaders
            ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
            String ipAddress = lp.getIPAddress();
            ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
                    sipProvider.getListeningPoint(transport).getPort(),
                    transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            // Create contact headers
            String host = Config.myClientAddress;

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(lp.getPort());
            contactUrl.setLrParam();

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint(transport)
                    .getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

                // You can add extension headers of your own making
            // to the outgoing SIP request.
            // Add the extension header.
            //Header extensionHeader = headerFactory.createHeader("My-Header",
            //                "my header value");
            //request.addHeader(extensionHeader);
            // create my SDP offer
            SessionDescription clientSdp = SdpTool.fromString("v=0\n"// protocol
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
                    + client.getCAReceiver().getPort()
                    + " RTP/AVP 96\n"
                    + "a=rtcp:"
                    + (client.getCAReceiver().getPort() + 1)
                    + "\n"
                    + "a=rtpmap:96 speex/16000");
            byte[] contents = clientSdp.toString().getBytes();

            request.setContent(contents, contentTypeHeader);
            
            /////
            ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);
            System.out.println("inviteTid = " + inviteTid);
            
            // send the request
            inviteTid.sendRequest();
            System.out.println("call request sent to server");
            dialog = inviteTid.getDialog();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        System.out.println("Leave a Message Request sent.");
        
    }
    
   

    private void sendMessageRequest(String messageType, String contentString ){
        // Create the request.
        
        try {
            String fromName = client.getMyName();
            String fromSipAddress = "gmail.com";
            String fromDisplayName = client.getMyName();
            // create From Header
            String toUser = "server";
            String toSipAddress = "gmail.com";
            String toDisplayName = "";

                
            
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                            fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = headerFactory.createFromHeader(
                            fromNameAddress, "12345");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                            Request.MESSAGE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                                .createMaxForwardsHeader(70);

            
            // create To Header
            SipURI toAddress = addressFactory
                            .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                            null);

            // create Request URI
            SipURI requestURI = addressFactory.createSipURI(toUser,
                            peerHostPort);

            // Create ViaHeaders

            ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
            String ipAddress = lp.getIPAddress();
            ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
                            sipProvider.getListeningPoint(transport).getPort(),
                            transport, null);

            // add via headers
            viaHeaders.add(viaHeader);
            
            // Create the request.
            Request request = messageFactory.createRequest(requestURI,
                            Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                            toHeader, viaHeaders, maxForwards);

            
            Header extensionHeader = headerFactory.createHeader("Message-Type",
                    messageType);
            request.addHeader(extensionHeader);
            
            byte[] contents =  contentString.getBytes();

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", Config.LISTEN_MESSAGE);
            request.setContent(contents, contentTypeHeader);

            // Create the client transaction.
            ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);

            System.out.println("inviteTid = " + inviteTid);

            // send the request out.

            inviteTid.sendRequest();
            
            System.out.println("first list request sent to server");

            dialog = inviteTid.getDialog();
        } catch (ParseException ex) {
            Logger.getLogger(SipListenerClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SipException ex) {
            Logger.getLogger(SipListenerClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(SipListenerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        

    }
    
    public void sendMessagesListRequest(){
        String contentString = "";
        String messageType = Config.LIST_MESSAGE;
        sendMessageRequest(messageType, contentString);
    }
    
    void listenMessage(int selectedMessage) { 
        String contentString = String.valueOf(selectedMessage)+"#"+myAddress+"#"+client.getCAReceiver().getPort();
        String messageType = Config.LISTEN_MESSAGE;
        sendMessageRequest(messageType, contentString);
    }

    void removeMessage(int selectedMessage) {
        String contentString = String.valueOf(selectedMessage);
        String messageType = Config.DELETE_MESSAGE;
        sendMessageRequest(messageType, contentString);
        
    }

    void forwardMessage(int selectedMessage, String destClient) {
        String contentString = String.valueOf(selectedMessage)+"#"+destClient;
        String messageType = Config.FORWARD_MESSAGE;
        sendMessageRequest(messageType, contentString);
    }

    
    
}
