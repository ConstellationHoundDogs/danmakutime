package nl.weeaboo.dt.renderer;

import javax.media.opengl.GL2;

public enum BlendMode {

	NORMAL(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA),
	ADD(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
	
	public final int sfactor;
	public final int dfactor;
	
	private BlendMode(int sfactor, int dfactor) {
		this.sfactor = sfactor;
		this.dfactor = dfactor;
	}	
	
}
