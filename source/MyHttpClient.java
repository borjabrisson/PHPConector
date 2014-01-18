package PHPConector;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;

import agosal.android.custom.ApplicationContextProvider;
import android.content.Context;

import com.example.integration.R;

public class MyHttpClient extends DefaultHttpClient {

    final Context context;

    public MyHttpClient() {
        this.context = ApplicationContextProvider.getContext();
    }

    @Override
    protected ClientConnectionManager createClientConnectionManager() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        // Register for port 443 our SSLSocketFactory with our keystore
        // to the ConnectionManager
        registry.register(new Scheme("https", newSslSocketFactory(), 443));
        return new SingleClientConnManager(getParams(), registry);
    }

    private SSLSocketFactory newSslSocketFactory() {
        try {
            // Get an instance of the Bouncy Castle KeyStore format
            KeyStore trusted = KeyStore.getInstance("BKS");
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            InputStream in = context.getResources().openRawResource(R.raw.mystore);
            try {
                // Initialize the keystore with the provided trusted certificates
                // Also provide the password of the keystore
                trusted.load(in, "123456".toCharArray());
            } finally {
                in.close();
            }
            // Pass the keystore to the SSLSocketFactory. The factory is responsible
            // for the verification of the server certificate.
            SSLSocketFactory sf = new SSLSocketFactory(trusted);
            // Hostname verification from certificate
            // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return sf;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


	public HttpPost sendRequest(String location,Map<String, String> data) throws IOException {
		// Create a new HttpClient and Post Header
		HttpPost httppost = new HttpPost(location);

		// Add your data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		/**
		 * Se codifican los datos a enviar en formato POST.
		 */
		Iterator<Entry<String, String>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> ent = (Map.Entry<String, String>) it
					.next();
			nameValuePairs.add(new BasicNameValuePair(ent.getKey().toString(),
					ent.getValue().toString()));
		}
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return httppost;
	}

	public String getAll(HttpResponse response){
		try {
		    HttpEntity responseEntity = response.getEntity();
		    InputStream is = responseEntity.getContent();
		    return is.toString();
	
		} catch (ClientProtocolException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return null;
	}
	
//	Certificate[] certs = con.getServerCertificates();
//	for(Certificate cert : certs){
//	   System.out.println("Cert Type : " + cert.getType());
//	   System.out.println("Cert Hash Code : " + cert.hashCode());
//	   System.out.println("Cert Public Key Algorithm : " 
//                                    + cert.getPublicKey().getAlgorithm());
//	   System.out.println("Cert Public Key Format : " 
//                                    + cert.getPublicKey().getFormat());
//	   System.out.println("\n");
//	}



}