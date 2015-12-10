import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class WorldSpace {

	// listeners
	private ArrayList<WorldSpaceUpdateListener> listenerUpdates = new ArrayList<WorldSpaceUpdateListener>();
	private ArrayList<WorldSpaceCollisionListener> listenerCollisions = new ArrayList<WorldSpaceCollisionListener>();
	public void registerUpdateListener(WorldSpaceUpdateListener e){
		synchronized(listenerUpdates){
			if(!listenerUpdates.contains(e)){
				listenerUpdates.add(e);
			}
		}
	}
	public void registerCollisionListener(WorldSpaceCollisionListener e){
		synchronized(listenerUpdates){
			if(!listenerCollisions.contains(e)){
				listenerCollisions.add(e);
			}
		}
	}
	public void unregisterUpdateListener(WorldSpaceUpdateListener e){
		synchronized(listenerUpdates){
			listenerUpdates.remove(e);
		}
	}
	public void unregisterCollisionListener(WorldSpaceCollisionListener e){
		synchronized(listenerUpdates){
			listenerCollisions.remove(e);
		}
	}
	
	// Vars
	private ArrayList<WorldObject> existingObjects = new ArrayList<WorldObject>();
	private GL gl;
	Shader shader;
	private GLCanvas canvas;
	private WorldSpaceUpdateThread updateThread;
	private WorldSpaceRenderThread renderThread;

	// Input related
	private int mouseButton;
	private int mouseX, mouseY;
	private ArrayList<Integer> keysPressed = new ArrayList<Integer>();
	
	// Camera related
	private WorldObject camObject;
	private Vector3f camPositionAroundObject = new Vector3f(0,0,.05f); // The position around the camera object the camera is located at.
	private float camRotationAroundObjectX = 0; // X Plane rotation of the camera around the camera object
	private float camRotationAroundObjectY = 15; // Y Plane rotation of the camera around the camera object

	private int camXRotateFactor = 0; // Direction the camera object is rotating on the X plane (-1 = left, 1 = right, 0 = nothing)
	private float camXRotateVal = 0; // Value of rotation of the camera object
	private int camYRotateFactor = 0; // Direction the camera object is rotating on the Y plane (-1 = up, 1 = down, 0 = nothing)
	private float camYRotateVal = 0; // Value of rotation of the camera object
	private int camElevateFactor = 0; // Direction the camera object is elevating or de-elevating (-1 = up, 1 = down, 0 = nothing)
	private boolean camMoveForward = false; // Whether the camera object is moving forward or not
	private boolean camMoveBackwards = false; // Whether the camera object is moving backwards or not
	private boolean camMoveLeft = false; // Whether the camera object is moving left or not
	private boolean camMoveRight = false; // Whether the camera object is moving right or not
	private boolean camMoveUp = false; // Whether the camera object is moving up or not
	private boolean camMoveDown = false; // Whether the camera object is moving down or not
	private float camObjectSpeed = 5f; // Speed the camera object moves forward at

	// Static variables
	public static final Vector3f _zAxis = new Vector3f(0,0,-1);
	public static final Vector3f _yAxis = new Vector3f(0,-1,0);
	public static final Vector3f _xAxis = new Vector3f(-1,0,0);
	
	public WorldSpace(GL gl, GLCanvas canvas) {
		this.gl = gl;
		this.canvas = canvas;
		this.init();
	}

	/**
	 * Called when the WorldSpace object is created.
	 */
	public void init() {
		shader = new Shader(gl);
        shader.load("shaders/sss.vert", Shader.Type.VERTEX);
        shader.load("shaders/sss.frag", Shader.Type.FRAGMENT);
		shader.link();
	    float  MaterialThickness = 0.01f;
		Vector3f ExtinctionCoefficient = new Vector3f(0.1f,0.1f,0.1f);
		Vector4f LightColor = new Vector4f(0.5f,0.1f,0.1f,0.0f);
		Vector4f BaseColor = new Vector4f(1.0f,1.0f,1.0f,0.0f);
		Vector4f SpecColor = new Vector4f(0.2f,0.2f,0.2f,0.0f);
		float SpecPower = 22f;
		float RimScalar = 22.0f;
		Vector3f LightPosition = new Vector3f(0.0f,4.0f,-2.0f);
		shader.setUniformVariables(MaterialThickness, ExtinctionCoefficient, LightColor, BaseColor, SpecColor, SpecPower, RimScalar, LightPosition);
	}

	
	/*
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 * Updating and Drawing.
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 */
	
	/**
	 * Draws all objects contained within the world space.
	 */
	public void draw() {
		synchronized (this.existingObjects) {
			setCameraPosition();

	    	for (int i = 0; i < this.existingObjects.size(); i++) {
	    		WorldObject obj = this.existingObjects.get(i);
				drawObject(obj);
			}
		}
	}

	
	/**
	 * Update method for the world. Updates are provide with a float that
	 * represents the amount of seconds since the last update.
	 * 
	 * @param delta
	 *            Seconds since the last update.
	 */
	public void update(float delta) {
		if(keysPressed.contains(KeyEvent.VK_LEFT)){
			camXRotateFactor = -1;
		} else if (keysPressed.contains(KeyEvent.VK_RIGHT)){
			camXRotateFactor = 1;
		}
		
		if(keysPressed.contains(KeyEvent.VK_UP)){
			camYRotateFactor = 1;
			
		} else if(keysPressed.contains(KeyEvent.VK_DOWN)){
			camYRotateFactor = -1;
		}
		
/*		if(keysPressed.contains(KeyEvent.VK_SPACE)){
			camElevateFactor = 1;
			
		} else if(keysPressed.contains((int)'X')){
			camElevateFactor = -1;
		}*/
		
		if(keysPressed.contains((int)'S')){
			camMoveBackwards = true;
		} else if (keysPressed.contains((int)'W')){
			camMoveForward = true;
		}

		if(keysPressed.contains((int)'A')){
			camMoveLeft = true;
			
		} else if(keysPressed.contains((int)'D')){
			camMoveRight = true;
		}
		
		if(keysPressed.contains(KeyEvent.VK_SPACE)){
			camMoveUp = true;
			
		} else if(keysPressed.contains((int)'X')){
			camMoveDown = true;
		}
		
		
		synchronized (this.existingObjects) {
			if(this.camObject != null){
				synchronized(this.camObject){
//					WorldObject obj = this.getWorldObject("Forward");
//					Vector3f forw = this.camObject.getForwardVector3f();
//					forw.scale(.02f);
//					forw.add(this.camObject.getPos());
//					obj.setPosition(forw);
					
					if(camXRotateFactor != 0){
						camXRotateVal += camXRotateFactor * delta * 180;
						this.camObject.setRotX(camXRotateVal);
					}
					
					if(camYRotateFactor != 0){
						camYRotateVal -= camYRotateFactor * delta * 180;
						this.camObject.setRotY(camYRotateVal);
					}
					
/*					if(camElevateFactor != 0){
						float elevateValue = delta * this.camObjectSpeed * this.camElevateFactor * 0.01f;
						Vector3f movement = new Vector3f(0,elevateValue,0);
						this.camObject.applyPosition(movement);
					}*/
					
					if(camMoveForward){
						Vector3f forward = this.camObject.getForwardVector3f();
						forward.scale(0.01f*camObjectSpeed*delta);
						this.camObject.applyPosition(forward);
					}
					if(camMoveBackwards){
						Vector3f forward = this.camObject.getForwardVector3f();
						forward.scale(-0.01f*camObjectSpeed*delta);
						this.camObject.applyPosition(forward);
					}
					
					if(camMoveLeft){
						Vector3f left = this.camObject.getLeftVector3f();
						left.scale(0.01f*camObjectSpeed*delta);
						this.camObject.applyPosition(left);
					}
					if(camMoveRight){
						Vector3f left = this.camObject.getLeftVector3f();
						left.scale(-0.01f*camObjectSpeed*delta);
						this.camObject.applyPosition(left);
					}
					if(camMoveUp){
						Vector3f up = this.camObject.getUpVector3f();
						up.scale(-0.01f*camObjectSpeed*delta);
						this.camObject.applyPosition(up);
					}
					if(camMoveDown){
						Vector3f up = this.camObject.getUpVector3f();
						up.scale(0.01f*camObjectSpeed*delta);
						this.camObject.applyPosition(up);
					}
					camXRotateFactor = 0;
					camYRotateFactor = 0;
/*					camElevateFactor = 0;*/
					camMoveLeft = false;
					camMoveRight = false;
					camMoveForward = false;
					camMoveBackwards = false;
					camMoveUp = false;
					camMoveDown = false;
				}
			}
			
			for(int i = 0; i <  this.existingObjects.size(); i++){
				WorldObject o1 = this.existingObjects.get(i);
				if(o1.getRadius() <= 0){
					continue;
				}
				for(int j = i+1; j <  this.existingObjects.size(); j++){
					WorldObject o2 = this.existingObjects.get(j);
					if(o2.getRadius() <= 0){
						continue;
					}
					Vector3f dif = new Vector3f(o1.getPos());
					dif.sub(o2.getPos());
					float distance = dif.length();
					if(distance <= o2.getRadius() + o1.getRadius()){
						this.broadcastCollision(o1, o2);
					}
				}
			}
		}
		
		synchronized(listenerUpdates){
			for(WorldSpaceUpdateListener wsul : listenerUpdates){
				wsul.worldSpaceUpdate(this, delta);
			}
		}
	}

	
	/*
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 * Manipulating object private methods.
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 */

	private void drawObject(WorldObject obj) {
		if(this.camObject == null){
			return;
		}
		Vector3f pos = obj.getPos();
		Vector3f rot = obj.getRot();
		Vector3f scale = obj.getScale();

		Vector3f cpos = this.camObject.getPos();
		Vector3f crot = this.camObject.getRot();
		Vector3f cposa = this.camPositionAroundObject;
		gl.glPushMatrix();
		
		if(obj != this.camObject){
			gl.glTranslatef(-cposa.x, -cposa.y, -cposa.z);

			gl.glRotatef(this.camRotationAroundObjectX, 0, 1, 0);
			gl.glRotatef(this.camRotationAroundObjectY, 1, 0, 0);

			gl.glRotatef(crot.z, 0,0,1);
			gl.glRotatef(crot.x, 0,1,0);
			gl.glRotatef(crot.y, 1,0,0);

			gl.glTranslatef( pos.x-cpos.x, pos.y-cpos.y, pos.z-cpos.z);
			gl.glRotatef(rot.z, 0,0,1);
			gl.glRotatef(rot.x, 0,1,0);
			gl.glRotatef(rot.y, 1,0,0);
		} else { ;
			gl.glTranslatef(-cposa.x, -cposa.y, -cposa.z);
			gl.glRotatef(this.camRotationAroundObjectX, 0, 1, 0);
			gl.glRotatef(this.camRotationAroundObjectY, 1, 0, 0);
			//gl.glRotatef(crot.w, crot.x, crot.y, crot.z);
		}
		gl.glScalef(scale.x, scale.y,scale.z);
		
		
		// shader stuff
		int i = obj.getShader();
		if (i!=0){
			switch(obj.getShader()) {
				case 1: shader.setBaseColor(new Vector4f(0.7f,0.7f,0.7f,0.4f)); //white blood cell
						shader.setLightColor(new Vector4f(0.3f,0.3f,0.3f,0.0f));
						shader.setSpecColor(new Vector4f(0.5f,0.0f,0.5f,0.0f));
						shader.setLightPosition(new Vector3f(0.0f,0.2f,-1.0f));
						shader.setExtinctionCoefficient(new Vector3f(7f,4f,7f));
						shader.setMaterialThickness(0.015f);
						break;
						
				case 2: shader.setBaseColor(new Vector4f(1.0f,1.0f,1.0f,0.0f)); //red blood cell
		        		shader.setLightColor(new Vector4f(0.5f,0.1f,0.1f,0.0f));
						shader.setSpecColor(new Vector4f(0.2f,0.2f,0.2f,0.0f));
						shader.setLightPosition(new Vector3f(0.0f,0.0f,-2.0f));
						shader.setExtinctionCoefficient(new Vector3f(10,2,2f));
						shader.setMaterialThickness(0.01f);
		        		break;
						
				case 3: shader.setBaseColor(new Vector4f(0.1f,0.0f,0.0f,0.0f)); //tube
						shader.setLightColor(new Vector4f(0.1f,0.0f,0.0f,0.2f));
						shader.setSpecColor(new Vector4f(0.1f,0.0f,0.0f,0.2f));
						shader.setLightPosition(new Vector3f(0f,0f,-0.27f));
						shader.setExtinctionCoefficient(new Vector3f(0.1f,0.1f,0.1f));
						shader.setMaterialThickness(0.2f);
						break;
						
				case 4: shader.setBaseColor(new Vector4f(0.3f,0.4f,0.0f,0.0f)); //bacteria
						shader.setLightColor(new Vector4f(0.1f,0.1f,0.0f,0.2f));
						shader.setSpecColor(new Vector4f(0.f,0.1f,0.0f,0.2f));
						shader.setLightPosition(new Vector3f(0f,0f,-0.27f));
						shader.setExtinctionCoefficient(new Vector3f(0.1f,0.1f,0.1f));
						shader.setMaterialThickness(0.01f);
						break;
			}
		}
		obj.getModel().draw(gl);
		gl.glPopMatrix();
	}

	private void setCameraPosition() {
        gl.glRotatef(180, 1, 0, 0);
        gl.glRotatef(180, 0, 0, 1);
        gl.glRotatef(180, 0, 1, 0);
	}
	
	/**
	 * Broadcasts a collision between the two objects.
	 * @param o1
	 * @param o2
	 */
	private void broadcastCollision(WorldObject o1, WorldObject o2){
		synchronized(listenerCollisions){
			for(WorldSpaceCollisionListener wsul : listenerCollisions){
				wsul.worldSpaceCollision(o1, o2);
			}
		}
	}

	/*
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 * Input handling.
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 */
	
	
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		if(!keysPressed.contains(c)){
			keysPressed.add(c);
		}
	}
	
	
	public void keyReleased(KeyEvent e){
		int c = e.getKeyCode();
		keysPressed.remove(new Integer(c));
	}
	
	public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        mouseButton = e.getButton();
    }

    public void mouseReleased(MouseEvent e) {
        mouseButton = MouseEvent.NOBUTTON;
    }

    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (mouseButton == MouseEvent.BUTTON3) {
        } else if (mouseButton == MouseEvent.BUTTON2) {
        } else if (mouseButton == MouseEvent.BUTTON1) {
			camXRotateVal += (x-mouseX)/800.0f * 360f;
			this.camObject.setRotX(camXRotateVal);
			camYRotateVal += (y-mouseY)/800.0f * 360f;
			this.camObject.setRotY(camYRotateVal); 
        }
        
        mouseX = x;
        mouseY = y;
    }
    
//    public void mouseMoved(MouseEvent e) {
//        int x = e.getX();
//        int y = e.getY();
//    	camXRotateVal += (x-mouseX)/800.0f * 360f;
//		this.camObject.setRotX(camXRotateVal);
//		camYRotateVal += (y-mouseY)/800.0f * 360f;
//		this.camObject.setRotY(camYRotateVal);
//        mouseX = x;
//        mouseY = y;
//    }
    
    
    
    
	/*
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 * Object adding, removing, getting, and information about objects.
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 */

    /**
     * Returns the object that is currently acting as the camera object.
     * @return
     */
    public WorldObject getCameraObject(){
    	return this.camObject;
    }
    
    /**
     * Sets the object that will act as the object the camera is focused on.
     * @param obj
     * @return
     */
    public WorldObject setCameraObject(WorldObject obj){
		if(this.camObject == null){
			this.camObject = obj;
		}
		synchronized(this.camObject){
			this.camObject = obj;
		}
		return obj;
    }
    
    /**
     * Retrieves an existing object with the given name. Gets the first object with that name.
     * @param name
     * @return
     */
    public WorldObject getWorldObject(String name){
		synchronized(this.existingObjects){
	    	for (int i = 0; i < this.existingObjects.size(); i++) {
				if (this.existingObjects.get(i).getName().equals(name)) {
					return this.existingObjects.get(i);
				}
			}
		}
		System.out.println("Object with name:" + name + " does not exist or has already been deleted and could not be retrieved.");
		return null;
    }
    
    /**
     * Retrieves an existing object by its unique id.
     * @param id
     * @return
     */
    public WorldObject getWorldObject(int id){
		synchronized(this.existingObjects){
	    	for (int i = 0; i < this.existingObjects.size(); i++) {
				if (this.existingObjects.get(i).getId() == id) {
					return this.existingObjects.get(i);
				}
			}
		}
		System.out.println("Object with id:" + id + " does not exist or has already been deleted and was unable to be retrieved.");
		return null;
    }
    
	/**
	 * Creates and adds a WorldObject object to the existing world objects. Also
	 * it returns the created object.
	 * 
	 * @param name
	 * @param filename
	 * @return The created object.
	 */
	public WorldObject addWorldObject(String name, String filename) {
		File f = new File(filename);
		if (f.exists() == false) {
			System.out
					.println("File does not exist. Object was not created and null was returned.");
		}
		ObjModel obj = new ObjModel(filename);
		WorldObject result = new WorldObject(name, obj);
		synchronized(this.existingObjects){
			this.existingObjects.add(result);
		}
		return result;
	}
	
	/**
	 * Creates and adds a WorldObject object to the existing world objects. Also
	 * it returns the created object.
	 * 
	 * @param name
	 * @param filename
	 * @return The created object.
	 */
	public WorldObject addWorldObject(String name, String filename, int shaderIndex) {
		File f = new File(filename);
		if (f.exists() == false) {
			System.out
					.println("File does not exist. Object was not created and null was returned.");
		}
		ObjModel obj = new ObjModel(filename);
		WorldObject result = new WorldObject(name, obj, shaderIndex);
		synchronized(this.existingObjects){
			this.existingObjects.add(result);
		}
		return result;
	}

	/**
	 * Creates and adds a WorldObject object to the existing world objects. Also
	 * it returns the created object.
	 * 
	 * @param name
	 * @param obj
	 * @return The created object.
	 */
	public WorldObject addWorldObject(String name, ObjModel obj) {
		WorldObject result = new WorldObject(name, obj);
		synchronized(this.existingObjects){
			this.existingObjects.add(result);
		}
		return result;
	}
	
	/**
	 * Creates and adds a WorldObject object to the existing world objects. Also
	 * it returns the created object.
	 * 
	 * @param name
	 * @param obj
	 * @return The created object.
	 */
	public WorldObject addWorldObject(String name, ObjModel obj, int shaderIndex) {
		WorldObject result = new WorldObject(name, obj, shaderIndex);
		synchronized(this.existingObjects){
			this.existingObjects.add(result);
		}
		return result;
	}

	/**
	 * Adds the provided object to the existing world object list.
	 * 
	 * @param obj
	 */
	public void addWorldObject(WorldObject obj) {
		synchronized(this.existingObjects){
			this.existingObjects.add(obj);
		}
	}

	/**
	 * Deletes the first occurrence of the given object.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean deleteWorldObject(WorldObject obj) {
		synchronized(this.existingObjects){
			if (!this.existingObjects.contains(obj)) {
				System.out .println("Object does not exist or has already been deleted.");
				return false;
			}
			this.existingObjects.remove(obj);
			System.out.println("Object has been deleted.");
		}
		return true;
	}

	/**
	 * Deletes the first occurrence of the given object identified by its unique
	 * id.
	 * 
	 * @param id
	 * @return
	 */
	public WorldObject deleteWorldObject(int id) {
		synchronized(this.existingObjects){
			for (int i = 0; i < this.existingObjects.size(); i++) {
				if (this.existingObjects.get(i).getId() == id) {
					System.out.println("Object with id:" + id + " has been deleted.");
					return this.existingObjects.remove(i);
				}
			}
			System.out.println("Object with id:" + id + " does not exist or has already been deleted.");
		}
		return null;
	}

	/**
	 * Deletes the first occurrence of the given object identified by its name.
	 * Only one instance will be removed if multiple objects have the same name.
	 * 
	 * @param id
	 * @return
	 */
	public WorldObject deleteWorldObject(String name) {
		synchronized(this.existingObjects){
			for (int i = 0; i < this.existingObjects.size(); i++) {
				if (this.existingObjects.get(i).getName().equals(name)) {
					System.out.println("Object with name:" + name + " has been deleted.");
					return this.existingObjects.remove(i);
				}
			}
		}
		System.out.println("Object with name:" + name + " does not exist or has already been deleted.");
		return null;
	}

	/**
	 * Returns a list containing the names of every object that exists in the
	 * space. Duplicates names will be returned if they exist.
	 * 
	 * @return List containing names of all objects.
	 */
	public List<String> getExistingObjectNames() {
		List<String> result = new ArrayList<String>();
		synchronized(this.existingObjects){
	    	for (int i = 0; i < this.existingObjects.size(); i++) {
	    		WorldObject obj = this.existingObjects.get(i);
				result.add(obj.getName());
			}
		}
		return result;
	}

	/**
	 * Returns a list containing the integer ids of every object that exists in
	 * the space.
	 * 
	 * @return List containing interger ids of all objects.
	 */
	public List<Integer> getExistingObjectIds() {
		List<Integer> result = new ArrayList<Integer>();
		synchronized(this.existingObjects){
			for (WorldObject obj : this.existingObjects) {
				result.add(obj.getId());
			}
		}
		return result;
	}

	/*
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 * Thread related methods.
	 * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	 */

	/**
	 * Starts updating the world space update thread. If an old thread was
	 * running, it will be shut off and replaced. This defaults to 100 updates a
	 * second.
	 */
	public void startUpdating() {
		if (this.updateThread != null) {
			this.stopUpdating();
		}
		this.updateThread = new WorldSpaceUpdateThread(this, 100);
		this.updateThread.start();
	}

	/**
	 * Starts updating the world space update thread. If an old thread was
	 * running, it will be shut off and replaced.
	 * 
	 * @param updatesPerSecond
	 *            The amount of updates every second.
	 */
	public void startUpdating(int updatesPerSecond) {
		if (this.updateThread != null) {
			this.stopUpdating();
		}
		this.updateThread = new WorldSpaceUpdateThread(this, updatesPerSecond);
		this.updateThread.start();
	}

	/**
	 * Stops the world space from updating.
	 */
	public void stopUpdating() {
		this.updateThread.stopRunning();
	}
	
	/**
	 * Starts updating the world space update thread. If an old thread was
	 * running, it will be shut off and replaced. This defaults to 100 updates a
	 * second.
	 */
	public void startRendering() {
		if (this.renderThread != null) {
			this.stopUpdating();
		}
		this.renderThread = new WorldSpaceRenderThread(canvas, 60);
		this.renderThread.start();
	}

	/**
	 * Starts updating the world space update thread. If an old thread was
	 * running, it will be shut off and replaced.
	 * 
	 * @param updatesPerSecond
	 *            The amount of updates every second.
	 */
	public void startRendering(int updatesPerSecond) {
		if (this.renderThread != null) {
			this.stopUpdating();
		}
		this.renderThread = new WorldSpaceRenderThread(canvas, updatesPerSecond);
		this.renderThread.start();
	}

	/**
	 * Stops the world space from updating.
	 */
	public void stopRendering() {
		this.renderThread.stopRunning();
	}
	
}
