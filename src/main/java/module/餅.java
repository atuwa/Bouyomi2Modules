package module;

import java.security.SecureRandom;

import bouyomi.DailyUpdate;
import bouyomi.DailyUpdate.IDailyUpdate;
import bouyomi.DiscordBOT;
import bouyomi.IModule;
import bouyomi.Tag;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class 餅 implements IDailyUpdate,IModule{

	private String[] 候補= {"餅","汚","焼","臭","腐","喰","妖","便","醜","悪","性","危"};
	private String 餅ID="306490014009917442";
	private String 対象サーバID="566942640986390528";
	private String 対象チャンネルID="566943792033169418";
	private String 今の名前;
	public 餅() {
		DailyUpdate.Ragister("餅",this);
	}
	@Override
	public void call(Tag tag){
		if(tag.getGuild()!=null&&!対象サーバID.equals(tag.getGuild().getId()))return;
		今の名前=tag.getUserName(餅ID);
		String s=tag.getTag("餅君名前更新");
		//if(s!=null&&(tag.isAdmin()||餅ID.equals(tag.con.userid))) {
		if(s!=null) {
			tag.chatDefaultHost(ランダム書き換え());
		}
	}
	@Override
	public void update(){
		if(DiscordBOT.bots==null||DiscordBOT.bots.isEmpty())return;
		DiscordBOT.DefaultHost.send(対象サーバID,対象チャンネルID,ランダム書き換え());
	}
	private String ランダム書き換え() {
		if(今の名前==null||今の名前.equals("null")) {
			今の名前=DiscordBOT.DefaultHost.getNick(対象サーバID,餅ID);
		}
		int rnd=new SecureRandom().nextInt(候補.length);
		名前更新(候補[rnd]);
		StringBuilder sb=new StringBuilder("餅君の名前を");
		sb.append(今の名前).append(" に書き換え");
		if(名前書き換え(今の名前))sb.append("成功");
		else sb.append("失敗");
		return sb.toString();
	}
	private boolean 名前書き換え(String s) {
		if(DiscordBOT.bots==null||DiscordBOT.bots.isEmpty())return false;
		try {
			Guild g=DiscordBOT.DefaultHost.jda.getGuildById(対象サーバID);
			Member member=g.getMemberById(餅ID);
			g.getController().setNickname(member,今の名前).queue();
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public void 名前更新(String update) {
		if(今の名前==null||今の名前.isEmpty())return;
		int index=今の名前.indexOf("(");
		if(index<0)index=今の名前.indexOf("（");
		if(index<0)return;//カッコを含まない時
		int ki=今の名前.indexOf(')');//半角
		int zi=今の名前.indexOf('）');//全角
		if(ki<0)ki=zi;
		if(ki<0)return;//閉じカッコが無い時
		if(ki<index+1)return;//閉じカッコの位置がおかしい時
		if(ki==index+1) {
			removeTag("", update);
			return;//0文字
		}
		String tag=今の名前.substring(index+1,ki);
		removeTag(tag, update);
	}
	public void removeTag(String val,String update) {
		StringBuilder sb0=new StringBuilder();
		sb0.append("(").append(val);//これ半角しか削除できない
		String remove=sb0.toString();
		int index=今の名前.indexOf(remove);
		//System.out.println("タグ消去　"+remove+"&index="+index);
		if(index<0) {
			StringBuilder sb1=new StringBuilder();
			sb1.append("（").append(val);//こっちで全角のカッコを処理
			remove=sb1.toString();
			index=今の名前.indexOf(remove);
			//System.out.println("タグ消去　"+remove+"&index="+index);
			if(index<0)return;
		}
		StringBuilder sb=new StringBuilder();
		if(index>0)sb.append(今の名前.substring(0,index));//カッコで始まる時以外
		if(今の名前.length()>index+remove.length())sb.append(今の名前.substring(index+remove.length()+1));
		sb.append("(").append(update).append(")");
		今の名前=sb.toString();
	}
}