package nl.weeaboo.dt.object;

import org.luaj.vm.LUserData;

import nl.weeaboo.dt.field.IField;
import nl.weeaboo.dt.input.IInput;
import nl.weeaboo.dt.renderer.BlendMode;
import nl.weeaboo.dt.renderer.IRenderer;
import nl.weeaboo.dt.renderer.ITexture;

public interface IDrawable {

	// === Functions ===========================================================

	/**
	 * @param input User input (keyboard presses)
	 */
	public void update(IInput input);

	/**
	 * @param renderer The 'drawing' object, abstracts over the underlying
	 *        drawing engine.
	 */
	public void draw(IRenderer renderer);

	/**
	 * Attempt to destroy this drawable. The operation may be blocked, in which
	 * case the return value will be <code>false</code>.
	 * 
	 * @return <code>true</code> if this drawable is now destroyed.
	 */
	public boolean destroy();

	// === Getters =============================================================

	/**
	 * @return The lua object associated with this drawable.
	 */
	public LUserData getLuaObject();
	
	/**
	 * @return <code>true</code> if the object is scheduled to be removed next
	 *         frame.
	 */
	public boolean isDestroyed();

	/**
	 * @return The drawable's X-coordinate
	 */
	public double getX();

	/**
	 * @return The drawable's Y-coordinate
	 */
	public double getY();

	/**
	 * @return The drawable's width
	 */
	public double getWidth();

	/**
	 * @return The drawable's height
	 */
	public double getHeight();
	
	/**
	 * @return The horizontal scale factor
	 */
	public double getScaleX();

	/**
	 * @return The vertical scale factor
	 */
	public double getScaleY();
	
	/**
	 * @return The drawable's angle (between 0.0 and 512.0)
	 */
	public double getDrawAngle();

	/**
	 * @return The drawable's current texture (used by the default
	 *         implementation of #draw(IRenderer) to texture the rendered quad.
	 */
	public ITexture getTexture();

	/**
	 * @return The drawable's Z-coordinate
	 */
	public short getZ();

	/**
	 * @return <code>true</code> if clipping is enabled for this drawable
	 */
	public boolean isClip();
	
	/**
	 * @return The current blending color
	 */
	public int getColor();
	
	/**
	 * @return The alpha component of the current blending color, between
	 *         <code>0.0</code> and <code>1.0</code>
	 */
	public double getAlpha();
	
	/**
	 * @return The blend mode used by this object when rendering
	 */
	public BlendMode getBlendMode();
	
	/**
	 * @return <code>true</code> if the visibility is set to visible (ignoring
	 *         alpha and texture).
	 */
	public boolean isVisible();
	
	// === Setters =============================================================

	/**
	 * Sets the field for this drawable and adds the drawable to it.
	 * 
	 * @param field The field to add this drawable to
	 */
	public void setField(IField field);
	
	/**
	 * Changes the drawable's position on-screen
	 * 
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 */
	public void setPos(double x, double y);

	/**
	 * @param a The drawable's rotation (between 0.0 and 512.0)
	 */
	public void setDrawAngle(double a);

	/**
	 * Changed the current texture
	 * 
	 * @param tex The new texture
	 * 
	 * @see #getTexture()
	 */
	public void setTexture(ITexture tex);

	/**
	 * Changes the draw layer
	 * 
	 * @param z The new Z-coordinate, higher Z is deeper into the screen.
	 */
	public void setZ(short z);

	/**
	 * Enable/disable clipping for this drawable 
	 */
	public void setClip(boolean c);
	
	/**
	 * Changes the color this object is blended with
	 * 
	 * @param argb The new blending color, packed as an <code>int</code> (in
	 *        ARGB order)
	 */
	public void setColor(int argb);
	
	/**
	 * Changes the color this object is blended with
	 */
	public void setColor(double r, double g, double b, double a);
	
	/**
	 * Convenience method for just changing the alpha part of the blending
	 * color.
	 * 
	 * @param a The new alpha component, between <code>0.0</code> and
	 *        <code>1.0</code>
	 *        
	 * @see #setColor(int)
	 */
	public void setAlpha(double a);
	
	/**
	 * Changes the default blend mode with which this object gets drawn
	 * 
	 * @param b The new blend mode
	 */
	public void setBlendMode(BlendMode b);
	
	/**
	 * Changes the width scale factor
	 * 
	 * @param sx The new width scale factor
	 */
	public void setScaleX(double sx);
	
	/**
	 * Changes the height scale factor
	 * 
	 * @param sy The new height scale factor
	 */	
	public void setScaleY(double sy);
	
	/**
	 * Changes the width and height scale factors in one call
	 */
	public void setScale(double sx, double sy);
	
	/**
	 * Toggles visibility on/off
	 */
	public void setVisible(boolean v);
}
