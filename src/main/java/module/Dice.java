package module;

import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bouyomi.IModule;
import bouyomi.Tag;

public class Dice implements IModule{
	@Override
	public void call(Tag tag){
		String com=tag.getTag("dice","ダイス","賽子");
		if(com!=null) {
			Matcher m=Pattern.compile("[0-9]++d[0-9]++").matcher(com);
			if(!m.find())return;
			com=m.group();
			m=Pattern.compile("[0-9]++").matcher(com);
			try {
				m.find();
				int j=Integer.parseInt(m.group());
				if(j>100000000) {
					tag.chatDefaultHost("回数多すぎ(1億回以下にして)");
					return;
				}
				m.find();
				int k=Integer.parseInt(m.group());
				Random rg;
				if(j<100000) {//10万回以下はランダム性が高い物を
					rg=new SecureRandom();
				}else {//それ以上の回数だと速度優先で
					rg=new Random();
				}
				StringBuilder mes=new StringBuilder("/");
				mes.append("(").append(com).append(")");
				StringBuilder rnd=null;
				long result=0;
				if(j<80) {
					rnd=new StringBuilder("[");
					for(int i=0;i<j;i++) {
						int r=rg.nextInt(k);
						result+=r;
						if(i>0)rnd.append(",");
						rnd.append(r);
					}
					rnd.append("]");
				}else {
					for(int i=0;i<j;i++) {
						result+=rg.nextInt(k);
					}
				}
				mes.append(" -> ");
				if(rnd!=null&&rnd.length()<100)mes.append(result).append(rnd);
				else mes.append("省略");
				mes.append(" -> ").append(result);
				tag.chatDefaultHost(mes.toString());
			}catch(RuntimeException nfe) {
				tag.chatDefaultHost("エラー");
			}
		}
	}
}
