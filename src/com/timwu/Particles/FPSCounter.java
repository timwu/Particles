package com.timwu.Particles;

public class FPSCounter {
	private static final int FPS_DISPLAY_FREQUENCY = 10;
	private int fpsPrintDivider = 0;
	private String savedFps;
	private float avgTimeslice;
	
	public FPSCounter(float initialTimeslice) {
		avgTimeslice = initialTimeslice;
	}
	
	public String getFps(float timeslice) {
		avgTimeslice = (timeslice * 19 + avgTimeslice) / 20;
		if (fpsPrintDivider % FPS_DISPLAY_FREQUENCY == 0) {
			float fps = 1 / avgTimeslice;
			savedFps = String.format("%1$.2f fps", fps);
		}
		fpsPrintDivider = (fpsPrintDivider + 1) % FPS_DISPLAY_FREQUENCY;
		return savedFps;
	}
}
