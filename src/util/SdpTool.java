/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;

/**
 *
 * @author chandra
 */
public class SdpTool {
    
    private static final SdpFactory sdpFactory = SdpFactory.getInstance();
    
    /**
	 * Create a SDP payload populated from a string. For format, please refer to
	 * {@link SdpFactory#createSessionDescription(String)}
	 * 
	 * @param sdp
	 *            SDP message as a String
	 * @return the SDP message populated, or null if a parsing problem happens
	 */
	public static SessionDescription fromString(String sdp) {
		SessionDescription ret = null;
		try {
			ret = sdpFactory.createSessionDescription(sdp);
		} catch (SdpParseException e) {
			e.printStackTrace();
		}
		return ret;
	}
        
        
        //retrieve IP address from SDP
        public static String getIpAddress(SessionDescription sdp) {
		String ret = null;
		try {
			ret = sdp.getOrigin().getAddress();
		} catch (SdpParseException e) {
			e.printStackTrace();
		}
		return ret;
	}
        
        public static int getAudioMediaPort(SessionDescription sdp) {
            int port = -1;
            try {
                    for (Object iter : sdp.getMediaDescriptions(false)) {
                            Media media = ((MediaDescription) iter).getMedia();
                            if (media.getMediaType().equals("audio")) {
                                    port = media.getMediaPort();
                                    break;
                            }
                    }
            } catch (SdpException e) {
                    e.printStackTrace();
            }
            return port;
	}

}
