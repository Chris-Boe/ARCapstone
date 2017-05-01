package plu.capstone.util;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plu.capstone.Models.CustomEvent;

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
		String icon = "building";
		int end;
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
								location = "Mary Baker Russell";
								icon="music";
							}else if(temp.contains("Olson")){
								location = "Olson Auditorium";
                                icon="olson";
							}else if(temp.contains("Anderson University Center") || temp.contains("Scandinavian") || temp.contains("AUC")){
								location = "University Center";
                                icon="uc";
							}else if(temp.contains("Harstad")){
								location = "Harstad";
                                icon="dorm";
							}else if(temp.contains("Hauge") || temp.contains("Admin")){
								location = "Hauge Admin Building";
                                icon="building";
							}else if(temp.contains("Hinderlie")){
								location = "Hinderlie";
                                icon="dorm";
							}else if(temp.contains("Hong")){
								location = "Hong Hall";
                                icon="dorm";
							}else if(temp.contains("Ingram")){
								location = "Ingram";
                                icon="ingram";
							}else if(temp.contains("Karen Hille Phillips") || temp.contains("KHP")){
								location = "Karen Hille Phillips";
                                icon="khpbuilding";
							}else if(temp.contains("Kreidler")){
								location = "Kreidler";
                                icon="dorm";
							}else if(temp.contains("Morken")){
								location = "Morken Center";
                                icon="morken";
							}else if(temp.contains("Library") || temp.contains("library")){
								location = "Mortvedt Library";
                                icon="library";
							}else if(temp.contains("Names")){
								location = "Names Fitness Center";
                                icon="gym";
							}else if(temp.contains("Ordal")){
								location = "Ordal";
                                icon="dorm";
							}else if(temp.contains("Pflueger")){
								location = "Pflueger";
                                icon="dorm";
							}else if(temp.contains("Ramstad")){
								location = "Ramstad";
                                icon="building";
							}else if(temp.contains("Rieke")){
								location = "Rieke Science Center";
                                icon="rieke";
							}else if(temp.contains("South")){
								location = "South Hall";
                                icon="dorm";
							}else if(temp.contains("Stuen")){
								location = "Stuen";
                                icon="dorm";
							}else if(temp.contains("Tingelstad")){
								location = "Tingelstad";
                                icon="dorm";
							}else if(temp.contains("Wang")){
								location = "Wang Center";
                                icon="wang";
							}else if(temp.contains("Xavier")){
								location = "Xavier";
                                icon="library";
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
				title = title.replaceAll("&38;", "&");
				//Add the customEvent details to the hashmap
				if(description.contains("a href")){
					description = description.replaceAll("a href=","");
					description = description.replaceAll("mailto:", "");
					int beg = description.indexOf("target=");
					end = description.indexOf("/a&gt;");
					String remove = description.substring(beg, end);
					description = description.replaceAll(remove, "");
				}
				if(description.contains("Organization")) {
					end = description.indexOf("Organization")-2;
					description = description.substring(0, end);
				}
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
				description = description.replaceAll("#160;", "");
				description = description.replaceAll("b&g", "");
				description = description.replaceAll("/a&gt;", "");
				description = description.replaceAll("/ ", "");
				description = description.replaceAll("quot;", "\"");
				//description = description.replaceAll("&gt;", "&");

				//Find the time
				Pattern pattern1 = Pattern.compile("[1]?[0-9](:[0-9][0-9])?([am]|[pm])?-[1]?[0-9](:[0-9][0-9])?([am]|[pm])?");
				Matcher matcher = pattern1.matcher(description);
				String time = "";
				boolean foundTime = false;
				if(matcher.find()){
					time = matcher.group();
					Log.d("Time", title + " " + time);
					foundTime = true;
				}
				String startTime = "";
				String endTime = "";
				if(foundTime) {
					int stop = time.indexOf("-");
					startTime = time.substring(0, stop);
					endTime = time.substring(stop);
					if (startTime.contains(":")) {
						startTime += ":00";
					} else {
						startTime += ":00:00";
					}
					if (endTime.contains(":")) {
						endTime += ":00";
					} else {
						endTime += ":00:00";
					}
					if(startTime.contains("-"))
						startTime = startTime.replaceAll("-", "");
					if(endTime.contains("-"))
						endTime = endTime.replaceAll("-", "");
					if(!startTime.contains("a") && !startTime.contains("12") && endTime.contains("p")){
						if(startTime.contains("a"))
							startTime = startTime.replaceAll("a", "");
						if(startTime.contains("p"))
							startTime = startTime.replaceAll("p", "");
						int colon = startTime.indexOf(":");
						int change = (Integer.parseInt(startTime.substring(0, colon)));
						change += 12;
						startTime = change + "" + startTime.substring(colon);
					}
					if(endTime.contains("p") && !endTime.contains("12")){
						if(endTime.contains("a"))
							endTime = endTime.replaceAll("a", "");
						if(endTime.contains("p"))
							endTime = endTime.replaceAll("p", "");
						int colon = endTime.indexOf(":");
						int change = (Integer.parseInt(endTime.substring(0, colon)));
						change += 12;
						endTime = change + "" + endTime.substring(colon);
					}
					if(endTime.contains("a"))
						endTime = endTime.replaceAll("a", "");
					int colon = startTime.indexOf(":");
					if((Integer.parseInt(startTime.substring(0, colon)))<10){
						startTime = "0"+startTime;
					}
					colon = endTime.indexOf(":");
					if((Integer.parseInt(endTime.substring(0, colon)))<10){
						endTime = "0"+endTime;
					}
					Log.d("NewTime", startTime + "-" + endTime);
				}
				//end find the time
				CustomEvent customEvent = new CustomEvent(description, location, link, category, startTime, endTime, icon);
				map.put(title, customEvent);
				//Log.d("RSS", description);
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