/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.File;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;
import util.Util;
import util.Config;

/**
 *
 * @author chandra
 */
public class ClientAudioReceiver extends Pipeline {
    
    private int port;
    
    public int getPort(){
        return port;
    }
    
    public void setPort(int port){
        this.port = port;
    }
    
    public ClientAudioReceiver(){
        super();
        System.out.println("ha0");
        final Element rtpSource = ElementFactory.make("udpsrc", null);
        //ask for port
        rtpSource.set("port", 0);
        
        
        System.out.println("ha1");
        
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
        
        //to autoaudiosink
        final Element audiosink = ElementFactory.make("autoaudiosink", null);

        System.out.println("ha2");
        
        // ############## ADD THEM TO PIPELINE ####################
        addMany(rtpSource, rtpBin, rtpDepay, speexdec, audioresample,
                    audioconvert, audiosink);

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
                        audioconvert, audiosink));

        pause();
        
        port = (Integer) rtpSource.get("port");
        System.out.println("Client started receving on "+port);
        
    }
    
    
}
