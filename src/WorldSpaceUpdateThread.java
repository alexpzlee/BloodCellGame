public class WorldSpaceUpdateThread extends Thread {
	private WorldSpace space;
	private boolean running = true;
	private int updatesPerSecond = 60;

	public WorldSpaceUpdateThread(WorldSpace space) {
		this.space = space;
	}

	public WorldSpaceUpdateThread(WorldSpace space, int updatesPerSecond) {
		this.space = space;
		this.updatesPerSecond = updatesPerSecond;
	}

	public void run() {
		long lastTime = System.currentTimeMillis();
		while (running) {
			if ((System.currentTimeMillis() - lastTime)/1000f > 1.0f / updatesPerSecond) {
				float delta = (System.currentTimeMillis() - lastTime)/1000f;
				space.update(delta);
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
