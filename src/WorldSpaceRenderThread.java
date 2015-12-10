import javax.media.opengl.GLCanvas;

public class WorldSpaceRenderThread extends Thread {
	private GLCanvas canvas;
	private boolean running = true;
	private int updatesPerSecond = 60;

	public WorldSpaceRenderThread(GLCanvas space) {
		this.canvas = space;
	}

	public WorldSpaceRenderThread(GLCanvas space, int updatesPerSecond) {
		this.canvas = space;
		this.updatesPerSecond = updatesPerSecond;
	}

	public void run() {
		long lastTime = System.currentTimeMillis();
		while (running) {
			if ((System.currentTimeMillis() - lastTime)/1000f > 1.0f / updatesPerSecond) {
				float delta = (System.currentTimeMillis() - lastTime)/1000f;
				canvas.display();
				lastTime = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * Stops the thread from running.
	 */
	public void stopRunning(){
		running = false;
	}
}
