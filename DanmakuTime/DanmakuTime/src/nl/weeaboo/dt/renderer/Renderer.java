package nl.weeaboo.dt.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;

import nl.weeaboo.common.FastMath;
import nl.weeaboo.game.gl.GLImage;
import nl.weeaboo.game.gl.GLManager;

public class Renderer implements IRenderer {

	private GLManager glm;	
	private Texture texture;
	
	private List<DrawRotQuadCommand> drawBuffer;
	
	public Renderer(GLManager glm) {
		this.glm = glm;
		
		drawBuffer = new ArrayList<DrawRotQuadCommand>();
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
		drawBuffer.add(new DrawRotQuadCommand(texture, cx, cy, w, h, z, angle));
		
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
	
	public void flush() {
		GL2 gl = GLManager.getGL2(glm.getGL());
		
		//Pop enqueued draw commands
		DrawRotQuadCommand cmds[] = drawBuffer.toArray(new DrawRotQuadCommand[drawBuffer.size()]);
		drawBuffer.clear();

		//Sort for efficiency
		Arrays.sort(cmds);
		
		//Draw buffered commands
		Texture cur = null;
		int buffered = 0;		
		for (DrawRotQuadCommand cmd : cmds) {									
			GLImage image = (cmd.tex != null ? cmd.tex.getImage() : null);
			if (image != null) {	
				if (cur != cmd.tex) {
					if (buffered > 0) {
						gl.glEnd();
					}
					buffered = 0;
					
					glm.setTexture(image.getTexture());
				}
			
				float dx = cmd.w * .5f;
				float dy = cmd.h * .5f;
				
				float sinA = FastMath.fastSin(cmd.angle);
				float cosA = FastMath.fastCos(cmd.angle);
								
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
				gl.glVertex2f(cmd.cx + p0x, cmd.cy + p0y);
				gl.glTexCoord2f(uv[1], uv[2]);
				gl.glVertex2f(cmd.cx + p1x, cmd.cy + p1y);
				gl.glTexCoord2f(uv[1], uv[3]);
				gl.glVertex2f(cmd.cx - p0x, cmd.cy - p0y);
				gl.glTexCoord2f(uv[0], uv[3]);
				gl.glVertex2f(cmd.cx - p1x, cmd.cy - p1y);
				
				buffered += 4;
			}
		}
		
		if (buffered > 0) {
			gl.glEnd();			
		}
	}
	
	//Getters
	
	//Setters
	@Override
	public void setTexture(ITexture tex) {
		texture = (Texture)tex;
	}
	
	//Inner Classes
	private static class DrawCommand implements Comparable<DrawCommand> {
		
		private final int sortKey;
		
		public DrawCommand(int sortKey) {
			this.sortKey = sortKey;
		}

		@Override
		public int compareTo(DrawCommand o) {
			return (sortKey >= o.sortKey ? 1 : (sortKey < o.sortKey ? -1 : 0));
		}
		
	}
	
	private static class DrawRotQuadCommand extends DrawCommand {
		
		public final Texture tex;
		public final float cx, cy, w, h, angle;
			
		public DrawRotQuadCommand(Texture tex, double cx, double cy, double w, double h,
				short z, double angle)
		{
			this(tex, (float)cx, (float)cy, (float)w, (float)h, z, (float)angle);
		}
		
		public DrawRotQuadCommand(Texture tex, float cx, float cy, float w, float h,
				short z, float angle)
		{			
			super((~z<<16)|(tex.hashCode()&0xFFFF));
			
			this.tex = tex;
			this.cx = cx;
			this.cy = cy;
			this.w = w;
			this.h = h;
			this.angle = angle;
		}
		
	}
	
}
