package plu.capstone;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.io.*;

public class RSSReader {
	private static HashMap<String, ArrayList<String>> map;

	public RSSReader(String url) {
		map = new HashMap<String, ArrayList<String>>();
		map = RSSRead("https://25livepub.collegenet.com/calendars/all.rss");

	}
	private static HashMap<String, ArrayList<String>> RSSRead(String url){
		//HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		String title = "";
		ArrayList<String> details;
		String description = "";
		String link = "";
		String category = "";

		try{
			URL rssUrl = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(rssUrl.openStream()));
			String line = in.readLine();
			//skip to first item
			//while(!(line = in.readLine()).contains("</image>")){
			//	continue;
			//}
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
				//Add the event details to the hashmap
				details = new ArrayList<String>();
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
				details.add(description);
				details.add(link);
				details.add(category);
				map.put(title, details);

			}
			in.close();
		}catch(MalformedURLException ue){
			System.out.println("Malformed URL");
		}catch(IOException e){
			System.out.println("Could not read contents");
		}
		return map;
	}
	public HashMap getMap(){
		return map;
	}
}