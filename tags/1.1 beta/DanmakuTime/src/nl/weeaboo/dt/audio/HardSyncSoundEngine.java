package nl.weeaboo.dt.audio;

import java.io.IOException;

import nl.weeaboo.dt.DTLog;
import nl.weeaboo.game.sound.SoundManager;
import nl.weeaboo.ogg.OggInput;
import nl.weeaboo.ogg.OggReader;
import nl.weeaboo.ogg.StreamUtil;
import nl.weeaboo.ogg.player.AudioSink;
import nl.weeaboo.ogg.vorbis.VorbisDecoder;

/**
 * {@link ISoundEngine} implementation that favors perfect gameplaye &lt;-&gt;
 * music sync over gapless music playback.
 */
public class HardSyncSoundEngine extends AbstractSoundEngine {

	private OggReader bgmReader;
	private VorbisDecoder bgmVorbisDecoder;
	private AudioSink asink;
	
	public HardSyncSoundEngine(SoundManager sm, int fps) {
		super(sm, fps);
	}
	
	//Functions
	@Override
	public void update(int frameInc) {
		super.update(frameInc);
		
		if (bgmReader != null) {
			try {
				double bytesPerSecond = bgmVorbisDecoder.getFrameRate() * bgmVorbisDecoder.getFrameSize();
				double targetTime = frame / (double)fps;				
				double fudge1 = .20;
				double fudge2 = .50;
				
				while (!bgmReader.isEOF() && bgmVorbisDecoder.getTime() < targetTime + fudge1) {
					bgmReader.read();
									
					double time = bgmVorbisDecoder.getTime();
					
					int frames = Math.min(bgmVorbisDecoder.getFramesBuffered(),
						(int)Math.round((fudge2 + targetTime - time) * bytesPerSecond));					
					byte data[] = bgmVorbisDecoder.read(frames);
					
					//System.out.printf("%.2f %.2f\n", targetTime, time);
					
					asink.buffer(data, bgmVorbisDecoder.getTime());
				}
				
				if (bgmReader.isEOF()) {
					setBGMOggReaderInput(getBGMFilename());
				}
			} catch (IOException ioe) {
				DTLog.warning(ioe);
				stopBGM();
			}
		}		
	}
	
	protected void setBGMOggReaderInput(String path) throws IOException {		
		OggInput oggIn = StreamUtil.getOggInput(sm.getSoundInputStream(path));
		
		bgmReader.setInput(oggIn);
		bgmReader.addStreamHandler(bgmVorbisDecoder = new VorbisDecoder());
		bgmReader.readStreamHeaders();
		
		while (!bgmReader.isEOF() && !bgmVorbisDecoder.hasReadHeaders()) {
			bgmReader.read();
		}		

		frame = 0;
	}
	
	@Override
	public void stopBGM() {
		if (asink != null) {
			try {
				asink.stop();
			} catch (InterruptedException e) {
				DTLog.warning(e);
			}
			asink = null;
		}
	
		bgmReader = null;
		bgmVorbisDecoder = null;
	}
	
	//Getters
	
	//Setters
	public void setBGM(String path) {
		stopBGM();

		bgmPath = path;		
		if (bgmPath == null) {
			return;
		}
		
		boolean ok = false;
		try {						
			bgmReader = new OggReader();
			setBGMOggReaderInput(bgmPath);
			
			if (bgmVorbisDecoder.hasReadHeaders()) {				
				asink = new AudioSink(bgmVorbisDecoder.getAudioFormat());
				asink.start(10, .25f);
				
				ok = true;
			}
		} catch (Exception e) {
			DTLog.warning(e);
		} finally {
			if (!ok) {
				stopBGM();
			}
		}
	}
	
}
