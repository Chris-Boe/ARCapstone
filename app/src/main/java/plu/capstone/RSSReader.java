package plu.capstone;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.net.*;
import java.util.HashMap;
import java.io.*;

public class RSSReader extends IntentService{
	private static HashMap<String, CustomEvent> map;
	public static final String urlInMessage = "";
	private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Pacific Lutheran University/Events");
	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 */
	public RSSReader() {
		super(RSSReader.class.getName());
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		Log.d("RSS Read: ", "Service Started!");

		map = RSSRead(intent.getStringExtra(urlInMessage));
		JSONObject json = new JSONObject(map);
		dbRef.setValue(map);
		Log.d("RSS Read: ", "After intent finished read");

	}

	private static HashMap<String, CustomEvent> RSSRead(String url) {
		Log.d("RSS Read: ", "RSSREAD Started!");

		map = new HashMap<String, CustomEvent>();
		String title = "";
		String description = "";
		String location = "";
		String link = "";
		String category = "";
		try{
			URL rssUrl = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(rssUrl.openStream()));
			String line = in.readLine();
			//skip to first item
			while(!(line = in.readLine()).contains("</image>")){
				continue;
			}
			//read file loop
			while(line != null){
				while((line = in.readLine()) != null && !(line.contains("</item>"))){
					if(line.contains("<title>")){
						try{
							int firstPos = line.indexOf("<title>");
							String temp = line.substring(firstPos);
							temp = temp.replace("<title>", "");
							int lastPos = temp.indexOf("</title>");
							temp = temp.substring(0, lastPos);
							title = temp;
						}catch(IndexOutOfBoundsException indexE){
							continue; //continue if closing and opening tags are not on same line
						}
					}
					//get description
					else if(line.contains("<description>")){
						try{
							int firstPos = line.indexOf("<description>");
							String temp = line.substring(firstPos);
							temp = temp.replace("<description>", "");
							int lastPos = temp.indexOf("</description>");
							temp = temp.substring(0, lastPos);
							//check for location
							if(temp.contains("Lagerquist") || temp.contains("MBR") || temp.contains("Mary Baker Russell")){
								location = "MBR";
							}else if(temp.contains("Olson")){
								location = "Olson Auditorium";
							}else if(temp.contains("Anderson University Center") || temp.contains("Scandinavian") || temp.contains("AUC")){
								location = "Anderson University Center";
							}else if(temp.contains("Harstad")){
								location = "Harstad";
							}else if(temp.contains("Hauge") || temp.contains("Admin")){
								location = "Hauge Admin Building";
							}else if(temp.contains("Hinderlie")){
								location = "Hinderlie";
							}else if(temp.contains("Hong")){
								location = "Hong Hall";
							}else if(temp.contains("Ingram")){
								location = "Ingram";
							}else if(temp.contains("Karen Hille Phillips") || temp.contains("KHP")){
								location = "Karen Hille Phillips";
							}else if(temp.contains("Kreidler")){
								location = "Kreidler";
							}else if(temp.contains("Morken")){
								location = "Morken Center";
							}else if(temp.contains("Library") || temp.contains("library")){
								location = "Mortvedt Library";
							}else if(temp.contains("Names")){
								location = "Names Fitness Center";
							}else if(temp.contains("Ordal")){
								location = "Ordal";
							}else if(temp.contains("Pflueger")){
								location = "Pflueger";
							}else if(temp.contains("Ramstad")){
								location = "Ramstad";
							}else if(temp.contains("Rieke")){
								location = "Rieke Science Center";
							}else if(temp.contains("South")){
								location = "South Hall";
							}else if(temp.contains("Stuen")){
								location = "Stuen";
							}else if(temp.contains("Tingelstad")){
								location = "Tingelstad";
							}else if(temp.contains("Wang")){
								location = "Wang Center";
							}else if(temp.contains("Xavier")){
								location = "Xavier";
							}
							description = temp + "\n";
						}catch(IndexOutOfBoundsException indexE){
							continue; //continue if closing and opening tags are not on same line
						}
					}

					//get link
					else if(line.contains("<link>")){
						try{
							int firstPos = line.indexOf("<link>");
							String temp = line.substring(firstPos);
							temp = temp.replace("<link>", "");
							int lastPos = temp.indexOf("</link>");
							temp = temp.substring(0, lastPos);
							link = temp + "\n";
						}catch(IndexOutOfBoundsException indexE){
							continue; //continue if closing and opening tags are not on same line
						}
					}

					//get category (yyyy/mm/dd (day))
					else if(line.contains("<category>")){
						try{
							int firstPos = line.indexOf("<category>");
							String temp = line.substring(firstPos);
							temp = temp.replace("<category>", "");
							int lastPos = temp.indexOf("</category>");
							temp = temp.substring(0, lastPos);
							category = temp + "\n";
						}catch(IndexOutOfBoundsException indexE){
							continue; //continue if closing and opening tags are not on same line
						}
					}else if(line.contains("<x-trumba:ealink>") || line.contains("guid") || line.contains("pubDate")){ //no tags, continued description line
						continue;
					}else{
						description += line;
					}


				}
				//Keys must not contain '/', '.', '#', '$', '[', or ']'
				title = title.replaceAll("/", "");
				title = title.replaceAll("\\.", "");
				title = title.replaceAll("#", "");
				title = title.replaceAll("\\$", "");
				title = title.replaceAll("\\[", "");
				title = title.replaceAll("]", "");
				//Add the customEvent details to the hashmap
				description = description.replaceAll("&lt;", "");
				description = description.replaceAll("br/&gt;", "");
				description = description.replaceAll("nbsp;", "");
				description = description.replaceAll("&amp;", "");
				description = description.replaceAll("ndash;", "-");
				description = description.replaceAll("p&gt;", "");
				description = description.replaceAll("b&gt;", "");
				description = description.replaceAll("/:", ":");
				description = description.replaceAll("br /&gt;", "");
				description = description.replaceAll("#39;", "'");
				description = description.replaceAll("/ Organization", "Organization");
				description = description.replaceAll("#160;", "");
				CustomEvent customEvent = new CustomEvent(description, location, link, category);
				map.put(title, customEvent);

			}
			in.close();
		}catch(MalformedURLException ue){
			Log.d("Malformed URL", ue.toString());
		}catch(IOException e){
			Log.d("Could not read contents", e.toString());
		}
		Log.d("RSS Read: ", "Finished with size: "+map.size());
		return map;
	}
	public HashMap getMap(){
		return map;
	}
}