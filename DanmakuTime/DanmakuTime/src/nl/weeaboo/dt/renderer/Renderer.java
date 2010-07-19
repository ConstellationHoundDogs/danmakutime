package nl.weeaboo.dt.renderer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import nl.weeaboo.common.FastMath;
import nl.weeaboo.common.Log;
import nl.weeaboo.game.gl.GLImage;
import nl.weeaboo.game.gl.GLManager;
import nl.weeaboo.game.text.ParagraphRenderer;
import nl.weeaboo.game.text.StyledText;
import nl.weeaboo.game.text.layout.TextLayout;

public class Renderer implements IRenderer {

	private GLManager glm;
	private ParagraphRenderer pr;
	private Dimension virtualSize;
	private Dimension realSize;
	
	private Texture texture;
	private Rectangle clipRect;
	private boolean clipEnabled;
	private double translationX, translationY;
	
	private List<DrawCommand> drawBuffer;
	
	public Renderer(GLManager glm, ParagraphRenderer pr, int w, int h, int rw, int rh) {
		this.glm = glm;
		this.pr = pr;
		
		virtualSize = new Dimension(w, h);
		realSize = new Dimension(rw, rh);
		
		clipRect = new Rectangle(0, 0, w, h);
		clipEnabled = true;
		drawBuffer = new ArrayList<DrawCommand>();
	}
	
	//Functions
	@Override
	public void drawQuad(double x, double y, double w, double h, short z) {
		drawRotatedQuad(x+w/2, y+h/2, w, h, z, 0);
	}

	@Override
	public void drawRotatedQuad(double cx, double cy, double w, double h, short z,
			double angle)
	{
		cx += translationX;
		cy += translationY;
		drawBuffer.add(new DrawRotQuadCommand(texture, clipEnabled, cx, cy, w, h, z, angle));
		
		/*
		GL2 gl = GLManager.getGL2(glm.getGL());
		gl.glPushMatrix();
		gl.glTranslated(cx, cy, 0);
		gl.glRotated(angle * 360.0 / 512.0, 0, 0, 1);
		
		double x = -w/2;
		double y = -h/2;
		
		GLImage image = (texture != null ? texture.getImage() : null);
		if (image != null) {			
			float uv[] = image.getUV();

			glm.setTexture(image.getTexture());
			
			gl.glBegin(GL2.GL_QUADS);
			gl.glTexCoord2f(uv[0], uv[2]); gl.glVertex2d(x,     y    );
			gl.glTexCoord2f(uv[1], uv[2]); gl.glVertex2d(x + w, y    );
			gl.glTexCoord2f(uv[1], uv[3]); gl.glVertex2d(x + w, y + h);
			gl.glTexCoord2f(uv[0], uv[3]); gl.glVertex2d(x,     y + h);
			gl.glEnd();
		} else {
			glm.setTexture(null);

			gl.glBegin(GL2.GL_QUADS);
			gl.glVertex2d(x,     y    );
			gl.glVertex2d(x + w, y    );
			gl.glVertex2d(x + w, y + h);
			gl.glVertex2d(x,     y + h);
			gl.glEnd();
		}
		
		gl.glPopMatrix();
		*/
	}
	
	@Override
	public void drawText(String txt, double x, double y, short z, double angle, double wrapWidth) {
		x += translationX;
		y += translationY;
		drawBuffer.add(new DrawTextCommand(clipEnabled, txt, x, y, z, angle, wrapWidth));
	}
	
	public void flush() {
		GL2 gl = GLManager.getGL2(glm.getGL());
				
		//Pop enqueued draw commands
		DrawCommand cmds[] = drawBuffer.toArray(new DrawCommand[drawBuffer.size()]);
		drawBuffer.clear();

		//Sort for efficiency
		Arrays.sort(cmds);

		//Setup clipping
		gl.glEnable(GL.GL_SCISSOR_TEST);
		
		float scale = Math.min(realSize.width / (float)virtualSize.width, realSize.height / (float)virtualSize.height);
		float clipDX = (realSize.width - scale*virtualSize.width) / 2;
		float clipDY = (realSize.height - scale*virtualSize.height) / 2;			
		
		gl.glScissor(Math.round(clipDX + scale * clipRect.x),
				realSize.height - Math.round(clipDY + scale * (clipRect.y+clipRect.height)),
				Math.round(scale * clipRect.width), Math.round(scale * clipRect.height));
		
		boolean clipping = true;
		
		//Draw buffered commands
		Texture cur = null;
		int buffered = 0;	
		for (DrawCommand cmd : cmds) {
			if (cmd.clipEnabled != clipping) {
				buffered = quadFlush(gl, buffered);

				if (cmd.clipEnabled) {
					gl.glEnable(GL.GL_SCISSOR_TEST);
				} else {
					gl.glDisable(GL.GL_SCISSOR_TEST);
				}
				clipping = cmd.clipEnabled;
			}
			
			if (cmd.type == DrawRotQuadCommand.type) {
				DrawRotQuadCommand dcmd = (DrawRotQuadCommand)cmd;
				GLImage image = (dcmd.tex != null ? dcmd.tex.getImage() : null);			
				if (image != null) {				
					if (cur != dcmd.tex) {
						buffered = quadFlush(gl, buffered);
						
						glm.setTexture(image.getTexture());
					}
					
					float dx = dcmd.w * .5f;
					float dy = dcmd.h * .5f;
					
					float sinA = FastMath.fastSin(dcmd.angle);
					float cosA = FastMath.fastCos(dcmd.angle);
									
					float cosX = cosA * dx;
					float sinX = sinA * dx;
					float cosY = cosA * dy;
					float sinY = sinA * dy;
					
					float p0x = -cosX + sinY;
					float p0y = -sinX - cosY;
					float p1x = cosX + sinY;
					float p1y = sinX - cosY;
					
					float uv[] = image.getUV();
					
					if (buffered == 0) {
						gl.glBegin(GL2.GL_QUADS);
					}
					
					gl.glTexCoord2f(uv[0], uv[2]);
					gl.glVertex2f(dcmd.cx + p0x, dcmd.cy + p0y);
					gl.glTexCoord2f(uv[1], uv[2]);
					gl.glVertex2f(dcmd.cx + p1x, dcmd.cy + p1y);
					gl.glTexCoord2f(uv[1], uv[3]);
					gl.glVertex2f(dcmd.cx - p0x, dcmd.cy - p0y);
					gl.glTexCoord2f(uv[0], uv[3]);
					gl.glVertex2f(dcmd.cx - p1x, dcmd.cy - p1y);
					
					buffered += 4;
				}
			} else if (cmd.type == DrawTextCommand.type) {
				buffered = quadFlush(gl, buffered);
				
				DrawTextCommand dcmd = (DrawTextCommand)cmd;
				StyledText stext = new StyledText(dcmd.text);
								
				pr.setBounds(0, 0, dcmd.wrapWidth, virtualSize.height - dcmd.y);
				TextLayout tl = pr.getLayout(glm, stext);
				float w = tl.getWidth();
				float h = tl.getHeight();
				
				gl.glPushMatrix();
				gl.glTranslatef(dcmd.x + w/2, dcmd.y + h/2, 0);
				gl.glRotated(dcmd.angle * 360.0 / 512.0, 0, 0, 1);
				gl.glTranslatef(-w/2, -h/2, 0);
				pr.drawLayout(glm, tl);				
				gl.glPopMatrix();
			} else {
				Log.error("Invalid draw command type: " + cmd.type);
				break;
			}
		}
		
		buffered = quadFlush(gl, buffered);
		
		gl.glDisable(GL.GL_SCISSOR_TEST);
	}
	
	private int quadFlush(GL2 gl, int buffered) {
		if (buffered > 0) {
			gl.glEnd();
		}
		buffered = 0;
		return buffered;
	}
	
	public void translate(double dx, double dy) {
		translationX = dx;
		translationY = dy;
	}
	
	//Getters
	@Override
	public Rectangle getClipRect() {
		return new Rectangle(clipRect);
	}
	
	@Override
	public boolean isClipEnabled() {
		return clipEnabled;
	}
	
	//Setters
	@Override
	public void setTexture(ITexture tex) {
		texture = (Texture)tex;
	}
	
	@Override
	public void setClipRect(int x, int y, int w, int h) {
		flush();
		
		clipRect.setBounds(x, y, w, h);
	}
	
	@Override
	public void setClipEnabled(boolean ce) {
		clipEnabled = ce;
	}
	
	//Inner Classes
	private static class DrawCommand implements Comparable<DrawCommand> {
		
		private final int sortKey;
		public final byte type;
		public final boolean clipEnabled;
		
		public DrawCommand(int sortKey, byte type, boolean clipEnabled) {
			this.sortKey = sortKey;
			this.type = type;
			this.clipEnabled = clipEnabled;
		}

		@Override
		public int compareTo(DrawCommand o) {
			return (sortKey >= o.sortKey ? 1 : (sortKey < o.sortKey ? -1 : 0));
		}
		
		static int mkKey(int type, Texture tex, boolean clipEnabled, int z) {
			//Key = ZZZZZZZZ ZZZZZZZZ CYYYTTTT TTTTTTTT
			return (~z << 16)
				| (clipEnabled ? 1<<15 : 0)
				| (type & 7)
				| (tex != null ? tex.hashCode() & 0xFFF : 0);			
		}
		
	}
	
	private static class DrawRotQuadCommand extends DrawCommand {
		
		static final byte type = 0;
		
		public final Texture tex;
		public final float cx, cy, w, h, angle;
			
		public DrawRotQuadCommand(Texture tex, boolean clipEnabled,
				double cx, double cy, double w, double h, short z, double angle)
		{
			this(tex, clipEnabled, (float)cx, (float)cy, (float)w, (float)h, z, (float)angle);
		}
		
		public DrawRotQuadCommand(Texture tex, boolean clipEnabled,
				float cx, float cy, float w, float h, short z, float angle)
		{			
			super(mkKey(type, tex, clipEnabled, z), type, clipEnabled);
			
			this.tex = tex;
			this.cx = cx;
			this.cy = cy;
			this.w = w;
			this.h = h;
			this.angle = angle;
		}
	}

	private static class DrawTextCommand extends DrawCommand {
		
		static final byte type = 1;
		
		public final String text;
		public final float x, y, angle, wrapWidth;
			
		public DrawTextCommand(boolean clipEnabled, String text,
				double x, double y, short z, double angle, double wrapWidth)
		{
			this(clipEnabled, text, (float)x, (float)y, z, (float)angle, (float)wrapWidth);
		}
		
		public DrawTextCommand(boolean clipEnabled, String text,
				float x, float y, short z, float angle, float wrapWidth)
		{
			super(mkKey(type, null, clipEnabled, z), type, clipEnabled);
			
			this.text = text;
			this.x = x;
			this.y = y;
			this.angle = angle;
			this.wrapWidth = wrapWidth;
		}
	}
	
}
