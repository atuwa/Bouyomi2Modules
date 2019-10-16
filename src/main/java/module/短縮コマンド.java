package module;

import java.text.SimpleDateFormat;
import java.util.Date;

import bouyomi.IModule;
import bouyomi.Tag;

public class 短縮コマンド implements IModule{

	@Override
	public void call(Tag tag){

	}
	public void precall(Tag t) {
		if(t.con.text.equals("今何時？")) {
			SimpleDateFormat sdf=new SimpleDateFormat("aahh時mm分ss秒をお知らせします");
			//t.con.text=sdf.format(new Date());
			t.chatDefaultHost(sdf.format(new Date()));
		}
		if(t.con.text.isEmpty()||t.con.text.charAt(0)!='$')return;
		else if(t.con.text.equals("$yr"))t.con.text="痩せろデブをもう一回引けるようにする";
		else if(t.con.text.equals("$yh")||t.con.text.equals("$kh"))t.con.text="今日引いた人達";
		else if(t.con.text.equals("$yd"))t.con.text="痩せろデブ";
		else if(t.con.text.equals("$kd")) {
			t.con.mentions.add("581268794794573870");
			t.con.text="気のせいでしょ";
		}
		else if(t.con.text.equals("$gk"))t.con.text="グレートカイコガ";
		else if(t.con.text.equals("$mk"))t.con.text="燃えろカイコガ";
		else if(t.con.text.equals("$?"))t.con.text="!help";
		else if(t.con.text.equals("$zh")) {
			SimpleDateFormat sdf=new SimpleDateFormat("aahh時mm分ss秒をお知らせします");
			t.con.text=sdf.format(new Date());
		}
	}
}
