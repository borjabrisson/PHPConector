package PHPConector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

import android.util.Log;

@SuppressWarnings( "rawtypes" )
public class JsonHandler {
	protected int errorCode;
	protected ContainerFactory containerFactory = new ContainerFactory() {
		public List<Object> creatArrayContainer() {
			return new LinkedList<Object>();
		}
		public Map<String, Object> createObjectContainer() {
			return new LinkedHashMap<String, Object>();
		}
	};


	public void getContent(String jsonText) {
		Log.d(this.getClass().toString(), "getContent");
		parserContent(jsonText);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> parserContent(String jsonText) {
		Log.d(this.getClass().toString(), "parserContent");
		Log.d(this.getClass().toString(), jsonText);
	
		Map<String, Object> content = new HashMap<String, Object>();
		JSONParser parser = new JSONParser();
		try {
			content = (Map) parser.parse(jsonText, containerFactory);
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			content.put("errorCode", -12345);
			Log.e(this.getClass().toString()+"::parserContent", e.toString());
		}
		return content;
	}

	@SuppressWarnings("unchecked")
	public String createSPCall(String user, String pass, String bd, String sp,
			String args) {
		JSONObject Json = new JSONObject();

		Json.put("type", "SPA");
		Json.put("user", user);
		Json.put("pass", pass);
		Json.put("bd", bd);
		Json.put("sp", sp);
		Json.put("args", args);

		return Json.toString();
	}

	@SuppressWarnings("unchecked")
	public String createSelectCall(String user, String pass, String bd,
			String clause) {
		JSONObject Json = new JSONObject();
		Json.put("type", "Query");
		Json.put("user", user);
		Json.put("pass", pass);
		Json.put("bd", bd);
		Json.put("clause", clause);
		return Json.toString();
	}
	
	public void showContent() {
		System.out.println("==toJSONString()==");
		System.out.println("Codigo Error: " + errorCode);
	}
	
	public void showContent(int type, Map<String, Object> content) {
		System.out.println("==showContent()==");

		switch (type) {
			case 1: // Mensaje asociado a una petición de query.
				break;
			case 2: // Mensaje asociado a la ejecución de SP.
				break;
			default:
				break;
		}
	}
}
