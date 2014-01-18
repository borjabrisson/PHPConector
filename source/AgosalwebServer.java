package PHPConector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.example.integration.R;

import agosal.android.custom.ApplicationContextProvider;
import android.os.AsyncTask;
import android.util.Log;


public class AgosalwebServer extends AsyncTask<String, String, Map<String,Object>>{

	/**
	 * @userName_Pass: Se tratan de las credenciales que posee el usuario para poder hacer uso del
	 * WebService.
	 * @url: Dirección en la que se aloja el servicio.
	 * @conector: Conector Http utilizado para la comunicación.
	 * @parser: Parser Json que se encargará de traducir el Json devuelto por el servicio en una estructura
	 * utilizable por Java.
	 * @lastError: Almacenará la cause del último error de comunicación.
	 */
	protected String userName="tester",pass="password";
	protected String url = ApplicationContextProvider.getContext().getResources().getString(R.string.url_webServer);
	protected MyHttpClient mycon = new MyHttpClient();
	protected JsonHandler parser = new JsonHandler();
	protected String userDB = "";
	protected String generalDB = "agosal2";
	protected String lastError;
	
	
	/**
	 * execProcedure: Recibe la petición de ejecutar un SP. Genera el mensaje para realizar la solicitud
	 * al WebService, se comunica con él y traduce el mensaje Json, procediente del WebService, en una
	 * estructura manejable en JAVA.
	 * @param bd: Base de Datos a utilizar
	 * @param SP
	 * @param args
	 * @throws IOException: Está excepción será lanzada cuando se produzca un error o interrupción
	 * durante la comunicación
	 */
	
	public Map<String,Object> execProcedure(String bd,String SP,String args) throws  IOException{
		Map<String, String> postData = new HashMap<String, String>();
		postData.put("type", "SP");
		postData.put("user", this.userName);	postData.put("pass", this.pass);
		postData.put("bd", bd);					
		postData.put("SP", SP);					postData.put("args", args);

		HttpPost post = mycon.sendRequest(url, postData);
		
		HttpContext context= new BasicHttpContext();
		HttpResponse getResponse = mycon.execute(post,context);
		
		HttpEntity responseEntity = getResponse.getEntity();
	    InputStream is = responseEntity.getContent();
		String jsonText = convertStreamToString(is);
		
//		Certificate[] certs= (Certificate[])context.getAttribute("PEER_CERTIFICATES");
//
//		System.out.println("Cert Public Key Algorithm : " +certs[0].getPublicKey().getAlgorithm());
//		System.out.println("Cert Public Key Format : " +certs[0].getPublicKey().getFormat());
//		for(Certificate cert : certs){
//		   System.out.println("Cert Type : " + cert.getType());
//		   System.out.println("Cert Hash Code : " + cert.hashCode());
//		   System.out.println("Cert Public Key Algorithm : " 
//	                                    + cert.getPublicKey().getAlgorithm());
//		   System.out.println("Cert Public Key Format : " 
//	                                    + cert.getPublicKey().getFormat());
//		   System.out.println("\n");
//		}
		return parser.parserContent(jsonText);
	}
	
	
	/**
	 * execQuery: Recibe la petición de ejecutar un Query. Genera el mensaje para realizar la solicitud
	 * al WebService, se comunica con él y traduce el mensaje Json, procediente del WebService, en una
	 * estructura manejable en JAVA.
	 * @param bd: Base de Datos a utilizar
	 * @param clause: Cláusula SQL a ejecutar para la consulta.
	 * @throws IOException: Está excepción será lanzada cuando se produzca un error o interrupción
	 * durante la comunicación
	 */

	public Map<String,Object> execCheckCredential() throws  IOException,ClientProtocolException{
		Map<String, String> postData = new HashMap<String, String>();
		
		postData.put("type", "CheckCredentials");
		postData.put("user", this.userName);	postData.put("pass", this.pass);
		
		HttpPost post = mycon.sendRequest(url, postData);
		HttpResponse getResponse = mycon.execute(post);
		HttpEntity responseEntity = getResponse.getEntity();
	    InputStream is = responseEntity.getContent();
		String jsonText = convertStreamToString(is);

		return parser.parserContent(jsonText);
	}
	
	public Map<String,Object> execQuery(String bd,String clause) throws  IOException,ClientProtocolException{
		Map<String, String> postData = new HashMap<String, String>();
		
		postData.put("type", "Select");
		postData.put("user", this.userName);	postData.put("pass", this.pass);
		postData.put("bd", bd);					postData.put("clause", clause);
		
		HttpPost post = mycon.sendRequest(url, postData);
		HttpResponse getResponse = mycon.execute(post);
		HttpEntity responseEntity = getResponse.getEntity();
	    InputStream is = responseEntity.getContent();
		String jsonText = convertStreamToString(is);

		return parser.parserContent(jsonText);

	//	parser.showContent();
	}
	
	public void setCredential(String user,String pass){
		this.userName =user;
		this.pass = pass;
	}
	
	
	public void setUserDB(String db){
		userDB = db;
	}
	public String getUserDB(){
		return userDB;
	}
	public String getGeneralDB(){
		return generalDB;
	}
	
	public void clearCredential(){
		this.userName ="";
		this.pass = "";
		this.userDB="";
	}
	
	public void setURL(String url){
		this.url = url;
	}
	
	public String getError(){
		return lastError;
	}
	
	public static String convertStreamToString(InputStream is) throws IOException,ClientProtocolException{
        /*
         * To convert the InputStream to String we use the
         * Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to
         * read. We use the StringWriter class to produce the string.
         */
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } 
        return "";
    }
	
	/**
	 * doInBackground: Método derivado del uso de AsyncTask. Se encarga de generar el mensaje a enviar al
	 * WebService, según el tipo indicado.
	 * Además captura y gestiona cualquier tipo de error en la comunicación.
	 */
	@Override
	protected Map<String,Object> doInBackground(String... params) {
		try {
			if (params[0]== "Select"){
				Log.d("doInBackground", "Llegaaa SElect");
				return this.execQuery(params[1],params[2]);//"bd", "SP", "args");
			}
			else{
				if (params[0]== "CheckCredentials"){
					return this.execCheckCredential();
				}
				else{
				Log.d("doInBackground", "Llegaaa SP");
				return this.execProcedure(params[1],params[2],params[3]);//"bd", "SP", "args");
				}
			}
		} catch (IOException e) {
			Log.e("AgosalwebServer::doInBackground","Error en la conexión", e);
			lastError = e.getCause().getMessage();
			return null;
		}

	}
	
//	 protected void onPostExecute(JSONObject json){
//		  asyncTaskListener.onFinish(json);  
//	 }
	public AgosalwebServer toClone(){
		AgosalwebServer obj = new AgosalwebServer();
		obj.setURL(url);
		obj.setCredential(userName, pass);
		obj.setUserDB(userDB);
		return obj;
		
	}
	
	
	public Map<String,Object> sendAction(String bd,String action,String args){
		Map<String,Object> data= null;
		try {
			data = execute("SP", bd,action,args).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	public Map<String,Object> CheckCredential (){
		Map<String,Object> data= null;
		try {
			data = execute("CheckCredentials").get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	public Map<String,Object> sendQuery(String bd,String query){
		Map<String,Object> data= null;
		try {
			data = execute("Select", bd,query).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

}
