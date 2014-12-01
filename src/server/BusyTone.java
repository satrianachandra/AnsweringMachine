/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import javax.tools.Tool;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.good.RTPBin;
import util.Util;

/**
 *
 * @author chandra
 */
public class BusyTone extends Pipeline {
    
    
    public BusyTone(String ipAddress, int port){
        super();
        
        Element fileSrc = ElementFactory.make("filesrc", null);
        fileSrc.set("location", "/home/chandra/NetBeansProjects/AnsweringMachine/lib/voicemailgreetings.mp3");
        
        Element decodebin = ElementFactory.make("decodebin", null);
        
        final Element audioconvert = ElementFactory.make("audioconvert", null);
        final Element audioresample = ElementFactory
                        .make("audioresample", null);
        Element capsRateFilter = ElementFactory.make("capsfilter", null);
        capsRateFilter.setCaps(Caps.fromString("audio/x-raw-int,rate=16000"));
        
        final Element encoder = ElementFactory.make("speexenc", null);
        encoder.set("quality", 6); // quality in [0,10]
        encoder.set("vad", true); // voice activity detection
        encoder.set("dtx", true); // discontinuous transmission
        
        Element rtpPay = ElementFactory.make("rtpspeexpay", null);
        Element capsRtpFilter = ElementFactory.make("capsfilter", null);
        capsRtpFilter.setCaps(Caps.fromString("application/x-rtp,"
                        + "payload=(int)96"));
        
        RTPBin rtpBin = new RTPBin((String) null);

        // asking this put the gstrtpbin plugin in sender mode
        Pad rtpSink0 = rtpBin.getRequestPad("send_rtp_sink_0");

        Element udpSink = ElementFactory.make("udpsink", "udpSinkAudio");
        udpSink.set("host", ipAddress);
        udpSink.set("port", port);
        System.out.println("sending busy tone to "+port);
        udpSink.set("async", false);

        // ############## ADD THEM TO PIPELINE ####################
        addMany(fileSrc, decodebin, audioconvert, audioresample, capsRateFilter,
                        encoder, rtpPay, capsRtpFilter, rtpBin, udpSink);

        // ####################### CONNECT EVENT ######################"
        decodebin.connect(new Element.PAD_ADDED() {
                public void padAdded(Element element, Pad pad) {
                        System.out.println("\nGot new input pad: " + pad);
                        Util.doOrDie("decodebin-audioconvert",
                                        pad.link(audioconvert.getStaticPad("sink")).equals(
                                                        PadLinkReturn.OK));
                }
        });

        // ###################### LINK THEM ##########################
        Util.doOrDie("src,decodebin", linkMany(fileSrc, decodebin));

        Util.doOrDie("audioconvert,audioresample,capsRateFilter,encoder,rtppay,capsFilter",
                        linkMany(audioconvert, audioresample, capsRateFilter, encoder,
                                        rtpPay, capsRtpFilter));
        Util.doOrDie("capsfilter-rtpbin", capsRtpFilter
                        .getStaticPad("src").link(rtpSink0).equals(PadLinkReturn.OK));
        Util.doOrDie("rtpbin-udpSink",
                        rtpBin.getStaticPad("send_rtp_src_0")
                                        .link(udpSink.getStaticPad("sink"))
                                        .equals(PadLinkReturn.OK));
        
    }
}
