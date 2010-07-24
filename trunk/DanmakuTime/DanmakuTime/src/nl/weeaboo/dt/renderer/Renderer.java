package nl.weeaboo.dt.renderer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import nl.weeaboo.common.FastMath;
import nl.weeaboo.dt.DTLog;
import nl.weeaboo.dt.object.ITextStyle;
import nl.weeaboo.dt.object.StyledText;
import nl.weeaboo.dt.object.TextStyle;
import nl.weeaboo.game.gl.GLImage;
import nl.weeaboo.game.gl.GLManager;
import nl.weeaboo.game.text.ParagraphRenderer;
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
	private int color;
	private BlendMode blendMode;
	
	private List<DrawCommand> drawBuffer;
	
	public Renderer(GLManager glm, ParagraphRenderer pr, int w, int h, int rw, int rh) {
		this.glm = glm;
		this.pr = pr;
		
		virtualSize = new Dimension(w, h);
		realSize = new Dimension(rw, rh);
		
		clipRect = new Rectangle(0, 0, w, h);
		clipEnabled = true;
		color = 0xFFFFFFFF;
		blendMode = BlendMode.NORMAL;
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
		drawBuffer.add(new DrawRotQuadCommand(texture, clipEnabled, blendMode, color,
				cx, cy, w, h, z, angle));
		
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
	public void drawText(StyledText stext, double x, double y, short z, double angle,
			double wrapWidth, int blockAnchor)
	{
		x += translationX;
		y += translationY;
		
		drawBuffer.add(new DrawTextCommand(clipEnabled, blendMode, color,
				stext, x, y, z, angle, wrapWidth, blockAnchor));
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
		
		Point s0 = virtualToGL(clipRect.x, clipRect.y);
		Point s1 = virtualToGL(clipRect.x+clipRect.width, clipRect.y+clipRect.height);
		
		gl.glScissor(Math.min(s0.x, s1.x), Math.min(s0.y, s1.y),
				Math.abs(s1.x-s0.x), Math.abs(s1.y-s0.y));

		boolean clipping = true;

		//Setup color
		glm.pushColor();
		int color = glm.getColor();
		
		//Setup blend mode
		glm.pushBlendMode(nl.weeaboo.game.gl.BlendMode.DEFAULT);		
		BlendMode blendMode = BlendMode.NORMAL;
		
		//Draw buffered commands
		Texture cur = null;
		int buffered = 0;	
		for (DrawCommand cmd : cmds) {
			//Clipping changed
			if (cmd.clipEnabled != clipping) {
				buffered = quadFlush(gl, buffered);

				if (cmd.clipEnabled) {
					gl.glEnable(GL.GL_SCISSOR_TEST);
				} else {
					gl.glDisable(GL.GL_SCISSOR_TEST);
				}
				clipping = cmd.clipEnabled;
			}
			
			//Blend mode changed
			if (cmd.blendMode != blendMode) {
				buffered = quadFlush(gl, buffered);

				blendMode = cmd.blendMode;
				gl.glBlendFunc(blendMode.sfactor, blendMode.dfactor);
			}
			
			//Foreground color changed
			if (cmd.argb != color) {
				buffered = quadFlush(gl, buffered);

				color = cmd.argb;
				glm.setColorARGB(color);
			}
			
			//Draw command
			if (cmd.type == DrawType.ROT_QUAD) {
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
			} else if (cmd.type == DrawType.TEXT) {
				buffered = quadFlush(gl, buffered);
								
				DrawTextCommand dcmd = (DrawTextCommand)cmd;
				
				int chars[] = dcmd.stext.getCharacters();
				ITextStyle styles[] = dcmd.stext.getStyles();

				nl.weeaboo.game.text.StyledText stext = new nl.weeaboo.game.text.StyledText(chars);
				ITextStyle curStyle = null;
				int curStyleStart = 0;
				for (int n = 0; n < styles.length; n++) {
					if (styles[n] != curStyle) {
						if (curStyle != null) {
							stext.setStyle(((TextStyle)curStyle).getInnerStyle().immutableCopy(), curStyleStart, n);
						}
						curStyle = styles[n];
						curStyleStart = 0;
					}
				}				
				if (curStyle != null) {
					stext.setStyle(((TextStyle)curStyle).getInnerStyle().immutableCopy(), curStyleStart, styles.length);
				}
				
				pr.setBounds(0, 0, dcmd.wrapWidth, virtualSize.height - dcmd.y);
				TextLayout tl = pr.getLayout(glm, stext);
				float w = tl.getWidth();
				float h = tl.getHeight();
				
				float tx = 0;
				if (dcmd.anchor == 8 || dcmd.anchor == 5 || dcmd.anchor == 2) {
					tx = w/2;
				} else if (dcmd.anchor == 9 || dcmd.anchor == 6 || dcmd.anchor == 3) {
					tx = w;
				}
				
				float ty = 0;
				if (dcmd.anchor >= 4 && dcmd.anchor <= 6) {
					ty = h/2;
				} else if (dcmd.anchor <= 3) {
					ty = h;
				}
				
				gl.glPushMatrix();
				gl.glTranslatef(dcmd.x, dcmd.y, 0);
				gl.glRotated(dcmd.angle * 360.0 / 512.0, 0, 0, 1);
				gl.glTranslatef(-tx, -ty, 0);
				pr.drawLayout(glm, tl);				
				gl.glPopMatrix();
			} else {
				DTLog.error("Invalid draw command type: " + cmd.type);
				break;
			}
		}
		
		buffered = quadFlush(gl, buffered);
				
		gl.glDisable(GL.GL_SCISSOR_TEST);
		glm.popBlendMode();
		glm.popColor();
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
	
	@Override
	public BlendMode getBlendMode() {
		return blendMode;
	}

	@Override
	public int getColor() {
		return color;
	}
	
	//Setters
	@Override
	public Point virtualToReal(double x, double y) {
		float scale = Math.min(realSize.width / (float)virtualSize.width, realSize.height / (float)virtualSize.height);
		float dx = (realSize.width - scale*virtualSize.width) / 2;
		float dy = (realSize.height - scale*virtualSize.height) / 2;			
		
		Point p = new Point();
		p.x = (int)Math.round(dx + scale * x);
		p.y = (int)Math.round(dy + scale * y);
		return p;
	}
	
	protected Point virtualToGL(double x, double y) {
		Point p = virtualToReal(x, y);
		p.y = realSize.height - p.y; 
		return p;
	}
	
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
	
	@Override
	public void setBlendMode(BlendMode b) {
		blendMode = b;
	}

	@Override
	public void setColor(int argb) {
		color = argb;
	}
	
	//Inner Classes
	private enum DrawType {
		ROT_QUAD, TEXT;
	}
	
	private static class DrawCommand implements Comparable<DrawCommand> {
		
		public final DrawType type;
		public final boolean clipEnabled;
		public final BlendMode blendMode;
		public final int argb;

		private final long sortKey;
		
		public DrawCommand(int z, DrawType type, boolean clip, BlendMode blend, int argb,
				byte privateField)
		{
			this.type = type;
			this.clipEnabled = clip;
			this.blendMode = blend;
			this.argb = argb;
			
			//Key = ZZZZZZZZ ZZZZZZZZ CYYYBBBB ........ ........ ........ ........ TTTTTTTT
			sortKey = (((~z) & 0xFFFFL) << 48L)
					| ((clipEnabled ? 1 : 0) << 47L)
					| ((type.ordinal() & 7) << 44L)
					| ((blend.ordinal() & 7) << 40L)
					| ((argb & 0xFFFFFFFFL) << 8L)
					| (privateField & 0xFFL);
		}

		@Override
		public int compareTo(DrawCommand o) {
			return (sortKey >= o.sortKey ? 1 : (sortKey < o.sortKey ? -1 : 0));
		}		
	}
	
	private static class DrawRotQuadCommand extends DrawCommand {
		
		public final Texture tex;
		public final float cx, cy, w, h, angle;
			
		public DrawRotQuadCommand(Texture tex, boolean clip, BlendMode blend, int argb,
				double cx, double cy, double w, double h, short z, double angle)
		{
			this(tex, clip, blend, argb, (float)cx, (float)cy, (float)w, (float)h,
					z, (float)angle);
		}
		
		public DrawRotQuadCommand(Texture tex, boolean clip, BlendMode blend, int argb,
				float cx, float cy, float w, float h, short z, float angle)
		{			
			super(z, DrawType.ROT_QUAD, clip, blend, argb,
					(tex != null ? (byte)(tex.hashCode()&0xFF) : 0));
			
			this.tex = tex;
			this.cx = cx;
			this.cy = cy;
			this.w = w;
			this.h = h;
			this.angle = angle;
		}
	}

	private static class DrawTextCommand extends DrawCommand {
		
		public final StyledText stext;
		public final float x, y, angle, wrapWidth;
		public final int anchor;
			
		public DrawTextCommand(boolean clip, BlendMode blend, int argb, StyledText stext,
				double x, double y, short z, double angle, double wrapWidth,
				int anchor)
		{
			this(clip, blend, argb, stext, (float)x, (float)y, z, (float)angle,
					(float)wrapWidth, anchor);
		}
		
		public DrawTextCommand(boolean clip, BlendMode blend, int argb, StyledText stext,
				float x, float y, short z, float angle, float wrapWidth,
				int anchor)
		{
			super(z, DrawType.TEXT, clip, blend, argb, (byte)0);
			
			this.stext = stext;
			this.x = x;
			this.y = y;
			this.angle = angle;
			this.wrapWidth = wrapWidth;
			this.anchor = anchor;
		}
	}
	
}
