package freenet.darknetconnector.FProxyConnector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;


import freenet.support.io.LineReadingInputStream;
import org.thoughtcrime.ssl.pinning.PinningTrustManager;
import org.thoughtcrime.ssl.pinning.SystemKeyStore;

import android.content.Context;
import android.util.Log;

public class DarknetAppClient {
	private static String REQUEST_CERTIFICATE = "Certificate";
    private static String REQUEST_HOME_REFERENCE = "HomeReference";
    private static String REQUEST_PUSH_REFERENCE = "PushReference";
    private static String REQUEST_END_MESSAGE = "End";
    private static String REQUEST_CLOSE_CONNECTION = "CloseConnection";
	private String ip;
	private int port;
	private boolean SSLEnabled;
	private Socket socket;
	private OutputStream out;
    private LineReadingInputStream input;
    private Context context;
    private String pin;
    public DarknetAppClient(String ip,int port,boolean SSLEnabled, Context context, String pin) {
		this.ip = ip;
		this.port = port;
		this.SSLEnabled = SSLEnabled;
		this.context = context;
		this.pin = pin;
	}
    private static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
    public void closeConnection() throws IOException {
    	out.write((REQUEST_CLOSE_CONNECTION+'\n').getBytes("UTF-8"));
    	if (socket!=null && !socket.isClosed()) {
            //socket.shutdownInput();
            //socket.shutdownOutput();
            socket.close();
        }
        if(input!=null) input.close();
        if (out!=null) out.close();
        socket = null;
        input =null;
        out = null;
    }
	public boolean startConnection() {
		boolean done = false;
		try {
			if (SSLEnabled) {
				TrustManager[] trustManagers = new TrustManager[1];
				trustManagers[0] = new PinningTrustManager(SystemKeyStore.getInstance(context), new String[] {pin}, 0);
				/*TrustManager[] trustManagers = new TrustManager[] { new X509TrustManager() {

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain, String authType)
						throws java.security.cert.CertificateException {
					for(java.security.cert.X509Certificate certificate : chain) {
						certificate.getPublicKey().getEncoded();
						MessageDigest digest=null;
						try {
							digest = MessageDigest.getInstance("SHA1");
						} catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    byte[] spki          = certificate.getPublicKey().getEncoded();
					    byte[] pinBytes           = digest.digest(spki);
					    Formatter formatter = new Formatter();
			            for (byte b : pinBytes)
			            {
			                formatter.format("%02x", b);
			            }
			            String checkPin= formatter.toString();
			            formatter.close();
			            if (checkPin.equals(pin)) return;
					}
					
				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub
					
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return new java.security.cert.X509Certificate[] {};
				}
				}};*/
				SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, trustManagers, null);
				socket = sslContext.getSocketFactory().createSocket(InetAddress.getByName(ip),port );
			}else {
				socket= new Socket(InetAddress.getByName(ip),port );
			}
			InputStream is = new BufferedInputStream(socket.getInputStream(), 4096);
			socket.setSoTimeout(1000);
	        input = new LineReadingInputStream(is);
	        out = socket.getOutputStream();
	        done = true;
	        Log.d("dumb","This has started");
		}
		catch(IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (input==null || out==null || socket==null) done= false;
		return done;
	}
	public String pullHomeReference() throws IOException {
		String homeref = "";
		out.write((REQUEST_HOME_REFERENCE+'\n').getBytes("UTF-8"));
		String sentence2;
		while (!(sentence2 = input.readLine(32768, 128, true)).isEmpty()) {
			homeref = homeref.concat(sentence2);
		}
		return homeref;
	}
}
