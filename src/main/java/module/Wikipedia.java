package module;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bouyomi.IModule;
import bouyomi.Tag;

public class Wikipedia implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con.mute)return;
		Matcher m=Pattern.compile("https?://(ja\\.)?wikipedia\\.org/wiki/\\S++").matcher(tag.con.text);
		m.reset();
		while(m.find()) {
			String g=m.group();
			//System.out.println("Wikipediaと思われるURLを検出"+g);
			try{
				String d=URLDecoder.decode(g,"utf-8");
				if(!g.equals(d)) {
					m=Pattern.compile("https?://(ja\\.)?wikipedia\\.org/wiki/").matcher(d);
					m.find();
					int len=m.group().length();
					d=d.substring(len);
					tag.chatDefaultHost("/記事名="+d);
				}
			}catch(UnsupportedEncodingException e){
				e.printStackTrace();
			}
		}
	}
}