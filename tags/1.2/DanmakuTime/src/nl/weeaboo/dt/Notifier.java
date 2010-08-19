package nl.weeaboo.dt;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nl.weeaboo.game.gl.GLManager;
import nl.weeaboo.game.text.MutableTextStyle;
import nl.weeaboo.game.text.ParagraphRenderer;
import nl.weeaboo.game.text.StyledText;
import nl.weeaboo.game.text.TextStyle;
import nl.weeaboo.game.text.layout.TextLayout;

/**
 * Shows notifications and errors to the user.
 */
public class Notifier {

	private static final int defaultDuration = 5000;
	
	private ParagraphRenderer pr;
	private List<MessageInfo> messages;
	
	public Notifier(ParagraphRenderer p) {
		pr = p;
		pr.setDefaultStyle(new TextStyle("DejaVuSans", Font.BOLD, 14, 9));
		
		messages = new LinkedList<MessageInfo>();
	}
	
	//Functions
	public void addMessage(Object source, String message) {
		addMessage(source, message, Color.WHITE);
	}
	public void addMessage(Object source, String message, Color color) {
		addMessage(source, message, color, 1f);
	}
	public synchronized void addMessage(Object source, String message, Color color,
			float relDuration)
	{
		if (source != null) {
			for (Iterator<MessageInfo> it = messages.iterator(); it.hasNext(); ) {
				MessageInfo mi = it.next();
				if (mi.source == source) {
					it.remove();
				}
			}
		}
		
		messages.add(new MessageInfo(source, message, color,
				Math.round(relDuration*defaultDuration)));
	}
	
	public synchronized void update(int dt) {
		for (Iterator<MessageInfo> it = messages.iterator(); it.hasNext(); ) {
			MessageInfo mi = it.next();
			mi.time += dt;
			if (mi.time > mi.duration) {
				it.remove();
			}
		}
	}
	
	public synchronized void draw(GLManager glm, int w, int h) {		
		int pad = 16;
		
		pr.setBounds(pad, pad, w-2*pad, h-2*pad);

		StyledText finalText = new StyledText("");
		for (MessageInfo mi : messages) {
			StyledText stext = new StyledText(mi.message + "\n");
			
			MutableTextStyle style = pr.getDefaultStyle().mutableCopy();
			style.setColor(mi.color);
			style.setOutlineSize(style.getFontSize() / 8);
			style.setOutlineColor(mi.color.darker().darker().darker());
			stext.setStyle(style.immutableCopy());
			
			finalText.append(stext);
		}

		TextLayout tl = pr.getLayout(glm, finalText);
		float ty = pr.getBounds().height - tl.getHeight();
		
		glm.translate(0, ty);
		pr.drawText(glm, finalText);
		glm.translate(0, -ty);
	}
	
	//Getters
	public TextStyle getTextStyle() {
		return pr.getDefaultStyle();
	}
	
	//Setters
	
	//Inner Classes
	private static class MessageInfo {
		
		public Object source;
		public String message;
		public Color color;
		public int time;
		public int duration;
		
		public MessageInfo(Object s, String m, Color c, int d) {
			source = s;
			message = m;
			color = c;
			duration = d;
		}
	}
	
}
