package nl.weeaboo.dt.renderer;

import java.awt.Dimension;

import nl.weeaboo.game.gl.GLTexUtil;

public class Blur {
	
	//Functions	
	public static int[] process(int src[], Dimension sz, int magnitude,
			boolean extendBorder, boolean crop)
	{
		int k = 1<<magnitude;

		Dimension newSize = new Dimension(sz.width + 2 * k, sz.height + 2 * k);
		int dst[] = new int[newSize.width * newSize.height];
		GLTexUtil.copyDataIntoImage(src, 0, dst, 0, newSize.width, k, k, sz.width, sz.height);
		
		if (extendBorder) {
			extend(dst, newSize.width, newSize.height, k);
		}
		
		//long time = System.nanoTime();
		dst = blur(dst, newSize.width, newSize.height, magnitude);
		//System.out.printf("Fast Box Blur (%dx%d kernel=%d): %.2fms\n",
		//		newSize.width, newSize.height, 1<<magnitude,
		//		(float)((System.nanoTime()-time)/1000000.0));
		
		int result[] = dst;
		if (crop) {
			result = src;
			GLTexUtil.copyImageIntoData(dst, 0, newSize.width,
					result, 0, sz.width,
					k, k, sz.width, sz.height);
		} else {
			sz.setSize(newSize);			
		}		
		return result;
	}
	
	public static void extend(int dst[], int w, int h, int r) {
		{ //Extend Y
			int borderOffset = w*r;
			int offset = 0;
			for (int y = 0; y < r; y++) {			
				for (int x = r; x < w-r; x++) dst[offset+x] = dst[borderOffset+x];
				for (int x = 0; x < r; x++)   dst[offset+x] = dst[offset+r];
				for (int x = w-r; x < w; x++) dst[offset+x] = dst[offset+w-r-1];
				offset += w;
			}
	
			borderOffset = w*(h-r-1);
			offset = w*(h-r);
			for (int y = 0; y < r; y++) {			
				for (int x = r; x < w-r; x++) dst[offset+x] = dst[borderOffset+x];
				for (int x = 0; x < r; x++)   dst[offset+x] = dst[offset+r];
				for (int x = w-r; x < w; x++) dst[offset+x] = dst[offset+w-r-1];
				offset += w;
			}
		}
		
		{ //Extend X
			int offset = 0;
			for (int y = 0; y < h; y++) {			
				for (int x = Math.min(Math.min(y, h-y), r)-1; x >= 0; x--) {
					dst[offset+x] = dst[offset+r];
				}
				offset += w;
			}

			offset = 0;
			for (int y = 0; y < h; y++) {			
				for (int x = w-Math.min(Math.min(y, h-y), r); x < w; x++) {
					dst[offset+x] = dst[offset+w-r-1];
				}
				offset += w;
			}
		}
	}

	public static int[] blur(int in[], int w, int h, int magnitude) {
		int out[] = new int[in.length];		
		fastBlur(out, in, w, h, magnitude, true);
		fastBlur(in, out, w, h, magnitude, false);		
		return in;
	}
	private static void fastBlur(int out[], int in[], int w, int h, int magnitude,
			boolean horizontal)
	{
		int k  = 1<<magnitude;
		int k2 = k>>>1;
		int round = k2;

		int amin = k2;
		int amax;
		int bmax;
		if (horizontal) {
			amax = h-k2;
			bmax = w-k2;
		} else {
			amax = w-k2;
			bmax = h-k2;
		}
		
		int baseOffset;
		int baseOffsetInc;
		int offsetInc;
		
		if (horizontal) {
			 baseOffset = amin * w;
			 baseOffsetInc = w;
			 offsetInc = 1;
		} else {
			baseOffset = amin;
			baseOffsetInc = 1;
			offsetInc = w;
		}
		
		int do1 = (-k2-1) * offsetInc;
		int do2 = (k-k2-1) * offsetInc;
		int firstPixelOffset = offsetInc * k2;
		for (int a = amin; a < amax; a++) {
			
			//Init initial pixel
			int offset = baseOffset;
			int sr = 0, sg = 0, sb = 0, sa = 0;
			for (int b = 0; b < k; b++) {
				int c = in[offset];
				sa += (c>>24) & 0xFF;
				sr += (c>>16) & 0xFF;
				sg += (c>> 8) & 0xFF;
				sb += (c    ) & 0xFF;
				offset += offsetInc;
			}
			
			offset = baseOffset + firstPixelOffset;
			out[offset] = (saturate((sa+round)>>magnitude)<<24)
				| (saturate((sr+round)>>magnitude)<<16)
				| (saturate((sg+round)>>magnitude)<<8)
				| (saturate((sb+round)>>magnitude));
			
			for (int b = k2+1; b < bmax; b++) {
				offset += offsetInc;
				
				int c = in[offset+do1];
				sa -= (c>>24) & 0xFF;
				sr -= (c>>16) & 0xFF;
				sg -= (c>> 8) & 0xFF;
				sb -= (c    ) & 0xFF;					
				
				c = in[offset+do2];
				sa += (c>>24) & 0xFF;
				sr += (c>>16) & 0xFF;
				sg += (c>> 8) & 0xFF;
				sb += (c    ) & 0xFF;					
				
				out[offset] = (saturate((sa+round)>>magnitude)<<24)
					| (saturate((sr+round)>>magnitude)<<16)
					| (saturate((sg+round)>>magnitude)<<8)
					| (saturate((sb+round)>>magnitude));				
			}
			
			baseOffset += baseOffsetInc;
		}					
	}
	
	private static int saturate(int c) {
		return (c > 255 ? 255 : (c < 0 ? 0 : c));
	}
	
	//Getters
	
	//Setters
	
}
 