/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.StateChangeReturn;
import util.Util;
import util.Config;

/**
 *
 * @author chandra
 */
public class MessageRecorder extends Pipeline {
    
    private int port;
    private String savedMessageLocation;
    private String calleeEmail;
    
    public String getSavedMessageLocation(){
        return this.savedMessageLocation;
    }
    
    public String getCalleeEmail(){
        return this.calleeEmail;
    }
    
    public int getPort(){
        return port;
    }
    
    public void setPort(int port){
        this.port = port;
    }
    
    public MessageRecorder(String callerName, String calleeName, String calleeSipAddress){
        super();
        
        //create email address for notification
        this.calleeEmail = calleeName+"@"+calleeSipAddress;
        
        // create a date to mark the message
        SimpleDateFormat filenameFormatter = new SimpleDateFormat(
                        "yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String stringDate = filenameFormatter.format(date);
        
        final Element rtpSource = ElementFactory.make("udpsrc", null);
        //ask for port
        rtpSource.set("port", 0);
        Util.doOrDie("caps",
                        rtpSource.getStaticPad("src").setCaps(
                                        Caps.fromString("application/x-rtp,"
                                                        + "media=(string)audio,"
                                                        + "clock-rate=(int)16000,"
                                                        + "encoding-name=(string)SPEEX, "
                                                        + "encoding-params=(string)1, "
                                                        + "payload=(int)96")));

        final Element rtpBin = ElementFactory.make("gstrtpbin", null);
        final Element rtpDepay = ElementFactory.make("rtpspeexdepay", null);
        final Element speexdec = ElementFactory.make("speexdec", null);
        final Element audioresample = ElementFactory
                        .make("audioresample", null);
        final Element audioconvert = ElementFactory.make("audioconvert", null);
        //uses speex codec
        final Element speexenc = ElementFactory.make("speexenc", null);
        //convert to .ogg
        final Element oggmux = ElementFactory.make("oggmux", null);
        //save to file
        final Element filesink = ElementFactory.make("filesink", null);

        // create the folder if it does not exist
        final String savedMessageDir = Config.MESSAGE_RECORDING_ROOT + calleeName;
        if (!new File(savedMessageDir).exists()) {
                new File(Config.MESSAGE_RECORDING_ROOT + calleeName).mkdirs();
        }

        
        this.savedMessageLocation = Config.MESSAGE_RECORDING_ROOT + calleeName + "/"
                        + callerName + "-" + stringDate + ".ogg";
        filesink.set("location",savedMessageLocation);

        // ############## ADD THEM TO PIPELINE ####################
        addMany(rtpSource, rtpBin, rtpDepay, speexdec, audioresample,
                    audioconvert, speexenc, oggmux, filesink);

        // ####################### CONNECT EVENTS ######################"
        rtpBin.connect(new Element.PAD_ADDED() {
            public void padAdded(Element element, Pad pad) {
                System.out.println("Pad added: " + pad);
                if (pad.getName().startsWith("recv_rtp_src")) {
                    Util.doOrDie("rtpBin-depay",
                            pad.link(rtpDepay.getStaticPad("sink")).equals(
                                    PadLinkReturn.OK));
                }
            }
        });

        // ###################### LINK THEM ##########################

        Pad pad = rtpBin.getRequestPad("recv_rtp_sink_0");

        Util.doOrDie("udpSource-rtpbin", rtpSource.getStaticPad("src")
                .link(pad).equals(PadLinkReturn.OK));
        
        Util.doOrDie("rtpDepay-speexdec-audioresample-speexenc-oggmux-sink", Element
                .linkMany(rtpDepay, speexdec, audioresample,
                        audioconvert, speexenc, oggmux, filesink));

        pause();
        port = (Integer) rtpSource.get("port");
        
    }
    
    public void stopPipeline(){
        this.setState(State.NULL);
        
        //check filesize
        if (!(Util.getFileSize(this.savedMessageLocation)>0)) {
            //delete the file
            Util.deleteFile(this.savedMessageLocation);
            System.out.println("the message is deleted because it's 0 size");
            this.savedMessageLocation = null;
        }        
        
    }
    
    
    
    
}
