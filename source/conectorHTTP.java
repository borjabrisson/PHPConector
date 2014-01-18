package PHPConector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import agosal.android.custom.ApplicationContextProvider;
import android.util.Log;

import com.example.integration.R;
//Libreria para Streams de lectura y escritura

//import org.apache.http.NameValuePair;

//Libreria para conexión URLConnection
//https://code.google.com/p/json-simple/wiki/DecodingExamples

public class conectorHTTP {
	public String location = "http://10.230.174.59/http.php";//192.168.1.36/http.php";
//	URLConnection con = null;
	HttpsURLConnection con = null;
	/**
	 * connect: Generamos la URL y establecemos la conexión.
	 * @param location
	 * @throws IOException: Está excepción será lanzada cuando se produzca un error o interrupción
	 * durante la comunicación
	 */
	protected void sslProtocol() {
		String https_url = "https://10.230.174.59/http.php";
		URL url;

		try {
			// Carga del fichero que tiene los certificados de los servidores en
			// los que confiamos.
//			ApplicationContextProvider.getContext().getResources().openRawResource(R.raw.mystore);
			InputStream fileCertificadosConfianza =
					ApplicationContextProvider.getContext().getResources().openRawResource(R.raw.mystore);
//			InputStream fileCertificadosConfianza = new FileInputStream(new File("keystore"));
			Log.d("SSL","Abrimos el keystore ");
			KeyStore ksCertificadosConfianza;
			  try {
				  ksCertificadosConfianza = KeyStore.getInstance( KeyStore.getDefaultType());
				  Log.d("SSL","Instancia  ");
					ksCertificadosConfianza.load(fileCertificadosConfianza,"123456".toCharArray());
					Log.d("SSL","clave  ");
	            } finally {
	            	fileCertificadosConfianza.close();
	            }
			  Log.d("SSL","Cargamos el certificado  ");
			// Ponemos el contenido en nuestro manager de certificados de
			// confianza.
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ksCertificadosConfianza);

			// Creamos un contexto SSL con nuestro manager de certificados en
			// los que confiamos.
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);
			SSLSocketFactory sslSocketFactory = context.getSocketFactory();
		    
			// Abrimos la conexión y le pasamos nuestro contexto SSL
			url = new URL(https_url);
			// URLConnection conexion = url.openConnection();
			// ((HttpsURLConnection)
			// conexion).setSSLSocketFactory(sslSocketFactory);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setSSLSocketFactory(sslSocketFactory);
			
			Log.d("SSL","abrimos conexion");
			//dumpl all cert info
		     print_https_cert(con);
	 
		     //dump all the content
		     print_content(con);

		} catch (NoSuchAlgorithmException e) {
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
		} catch (KeyManagementException e) {
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void connect(String location) throws IOException {
//		URL url;
//		url = new URL(location);
//		this.con = url.openConnection();
		sslProtocol();
		
	}
	
	public void connect() throws IOException{
		connect(this.location);
	}

	public void desconnect(){
		this.con = null;
	}
	
	/**
	 * sendRequest: Convertimos los datos solicitados por el usuario en formato POST y lo enviamos al Servidor Web
	 * mediante Http.
	 * @param data: Contiene los datos que el usuario desea enviar mediante el Post.
	 * @throws IOException: Está excepción será lanzada cuando se produzca un error o interrupción
	 * durante la comunicación
	 */
	
	public HttpPost sendRequest(Map<String, String> data) throws IOException {
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

	/**
	 * readRespond: Lee todo el contenido enviado por el WebService como respuesta a su petición previa.
	 * @return String enviado por el WebService. En este caso se tratará de uns String codificada
	 * mediante Json.
	 * @throws IOException: Está excepción será lanzada cuando se produzca un error o interrupción
	 * durante la comunicación
	 */

	public String readRespond() throws IOException { 
		/**
		 * Se crea un Stream de entrada a partir de la conexión URL establecida.
		 */
		BufferedReader in;
		in = new BufferedReader(
				new InputStreamReader(this.con.getInputStream()));
		/**
		 * Leemos todo el mensaje enviado.
		 */
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = in.readLine()) != null) {
			sb.append(line + "\n");
			System.out.println(line);
		}
		in.close();

		return sb.toString();
	}
	
	private void print_https_cert(HttpsURLConnection con){
		 
	    if(con!=null){
	 
	      try {
	 
		System.out.println("Response Code : " + con.getResponseCode());
		System.out.println("Cipher Suite : " + con.getCipherSuite());
		System.out.println("\n");
	 
		Certificate[] certs = con.getServerCertificates();
		for(Certificate cert : certs){
		   System.out.println("Cert Type : " + cert.getType());
		   System.out.println("Cert Hash Code : " + cert.hashCode());
		   System.out.println("Cert Public Key Algorithm : " 
	                                    + cert.getPublicKey().getAlgorithm());
		   System.out.println("Cert Public Key Format : " 
	                                    + cert.getPublicKey().getFormat());
		   System.out.println("\n");
		}
	 
		} catch (SSLPeerUnverifiedException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	 
	     }
	 
	   }
	 
	   private void print_content(HttpsURLConnection con){
		if(con!=null){
	 
		try {
	 
		   System.out.println("****** Content of the URL ********");			
		   BufferedReader br = 
			new BufferedReader(
				new InputStreamReader(con.getInputStream()));
	 
		   String input;
	 
		   while ((input = br.readLine()) != null){
		      System.out.println(input);
		   }
		   br.close();
	 
		} catch (IOException e) {
		   e.printStackTrace();
		}
	 
	       }
	 
	   }
	
	

}
