import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;


public class WorldObject {
	private Vector3f position;
	private Vector3f rotation;
	private Vector3f scale;
	private ObjModel model;
	private float radius;
	private int shaderIndex;
	
	private int id;
	private String name = "Unnamed Object";
	
	// Static variables
	private static int idCounter = 1; // Global world object id finder

	private WorldObject(){
		this.id = WorldObject.idCounter++;
		this.position = new Vector3f(0,0,0);
		this.rotation = new Vector3f(0,0,0);
		this.scale = new Vector3f(1,1,1);
	}
	public WorldObject(String name, ObjModel obj){
		this();
		this.name = name;
		this.model = obj;
	}
	
	public WorldObject(String name, ObjModel obj, int shaderIndex){
		this();
		this.name = name;
		this.model = obj;
		this.shaderIndex = shaderIndex;
	}
	
	public WorldObject(String name, String filename){
		this();
		this.model = new ObjModel(filename);
		this.name = name;
	}
	public WorldObject(String name, String filename, int shaderIndex){
		this();
		this.model = new ObjModel(filename);
		this.name = name;
		this.shaderIndex = shaderIndex;
		
	}

	public void setShader(int i){
		this.shaderIndex = i;
	}
	
	public int getShader(){
		return this.shaderIndex;
	}
	
	/**
	 * Sets the radius of this object for collision.
	 * @param r
	 */
	public void setRadius(float r){
		this.radius = r;
	}

	/**
	 * Gets the radius of this object for collision.
	 * @param r
	 */
	public float getRadius(){
		return this.radius;
	}
	
	
	/**
	 * Applies a vector to the position vector of the given object.
	 * Does the same thing as "applyMovement"
	 * @param vec
	 * @return The new position of the object
	 */
	public Vector3f applyPosition(Vector3f vec){
		position.add(vec);
		return position;
	}
	/**
	 * Applies a vector of rotation to the given object.
	 * @param vec
	 * @return The new rotation of the object
	 */
	public Vector3f applyRotation(Vector3f vec){
		rotation.add(vec);
		return rotation;
	}
	/**
	 * Applies a vector of rotation to the given object.
	 * @param vec
	 * @return The new rotation of the object
	 */
	public Vector3f applyRotation(float rotX, float rotY, float rotZ){
		Vector3f rotAdd = new Vector3f(rotX, rotY, rotZ);
		return this.applyRotation(rotAdd);
	}
	/**
	 * Applies a vector of scale to the given object.
	 * @param vec
	 * @return The new scale of the object
	 */
	public Vector3f applyScale(Vector3f vec){
		scale.add(vec);
		return scale;
	}	
	/**
	 * Applies a vector of movement to the given object.
	 * @param vec
	 * @return The new position of the object
	 */
	public Vector3f setPosition(Vector3f vec){
		position = vec;
		return position;
	}
	/**
	 * Applies a vector of rotation to the given object.
	 * @param vec
	 * @return The new rotation of the object
	 */
	public Vector3f setRotation(Vector3f vec){
		rotation = vec;
		return rotation;
	}
	/**
	 * Sets the X rotation of the object.
	 * @param rotX
	 * @return The new rotation of the object
	 */
	public Vector3f setRotX(float rotX){
		rotation.x = rotX;
		return rotation;
	}
	/**
	 * Sets the Y rotation of the object.
	 * @param rotX
	 * @return The new rotation of the object
	 */
	public Vector3f setRotY(float rotY){
		rotation.y = rotY;
		return rotation;
	}
	/**
	 * Sets the Z rotation of the object.
	 * @param rotX
	 * @return The new rotation of the object
	 */
	public Vector3f setRotZ(float rotZ){
		rotation.z = rotZ;
		return rotation;
	}
	/**
	 * Sets the rotation 
	 * @param rotX
	 * @param rotY
	 * @param rotZ
	 * @return
	 */
	public Vector3f setRotation(float rotX, float rotY, float rotZ){
		rotation = new Vector3f(rotX, rotY, rotZ);
		return rotation;
	}
	/**
	 * Applies a vector of scale to the given object.
	 * @param vec
	 * @return The new scale of the object
	 */
	public Vector3f setScale(Vector3f vec){
		scale = vec;
		return scale;
	}
	/**
	 * Returns the 3d ObjModel for this object.
	 * @return
	 */
	public ObjModel getModel(){
		return model;
	}
	/**
	 * Returns the position vector for this object in 3D space.
	 * @return
	 */
	public Vector3f getPos(){
		return position;
	}
	/**
	 * Returns the rotation vector for this object in 3D space.
	 * @return
	 */
	public Vector3f getRot(){
		return rotation;
	}
	/**
	 * Returns the scaling vector for this object.
	 * @return
	 */
	public Vector3f getScale(){
		return scale;
	}	
	/**
	 * Returns the unique id for this given object.
	 * @return
	 */
	public int getId(){
		return id;
	}
	/**
	 * Returns the string name for this object.
	 * @return
	 */
	public String getName(){
		return name;
	}
	/**
	 * Sets the name of the given object to the provided name.
	 * @param name
	 * @return The new name of the object.
	 */
	public String setName(String name){
		this.name = name;
		return name;
	}


	private static final Vector3f _zAxis = new Vector3f(0,0,-1);
	private static final Vector3f _yAxis = new Vector3f(0,-1,0);
	private static final Vector3f _xAxis = new Vector3f(-1,0,0);
	
	/**
	 * Returns the vector that represents the forward direction for this object normalized.
	 * @return
	 */
	public Vector3f getForwardVector3f(){
		Vector3f result = new Vector3f(0,0,-1);
		float DTR = (float) (Math.PI / 180f);
		float rx = this.getRot().x * DTR;
		float ry = this.getRot().y * DTR;
		float rz = this.getRot().z * DTR;
		
		AxisAngle4f zaa = new AxisAngle4f(_zAxis, rz);
		AxisAngle4f yaa = new AxisAngle4f(_xAxis, ry);
		AxisAngle4f xaa = new AxisAngle4f(_yAxis, rx);
		Matrix4f zMatrix = new Matrix4f();
		Matrix4f yMatrix = new Matrix4f();
		Matrix4f xMatrix = new Matrix4f();
		
		zMatrix.set(zaa);
		yMatrix.set(yaa);
		xMatrix.set(xaa);

		zMatrix.transform(result);
		xMatrix.transform(result);
		yMatrix.transform(result);
		
		result.normalize();
		return result;
	}
	
	/**
	 * Returns the vector that represents the left direction for this object normalized.
	 * @return
	 */
	public Vector3f getLeftVector3f(){
		Vector3f result = new Vector3f(-1,0,0);
		float DTR = (float) (Math.PI / 180f);
		float rx = this.getRot().x * DTR;
		float ry = this.getRot().y * DTR;
		float rz = this.getRot().z * DTR;
		
		AxisAngle4f zaa = new AxisAngle4f(_zAxis, rz);
		AxisAngle4f yaa = new AxisAngle4f(_xAxis, ry);
		AxisAngle4f xaa = new AxisAngle4f(_yAxis, rx);
		Matrix4f zMatrix = new Matrix4f();
		Matrix4f yMatrix = new Matrix4f();
		Matrix4f xMatrix = new Matrix4f();
		
		zMatrix.set(zaa);
		yMatrix.set(yaa);
		xMatrix.set(xaa);

		zMatrix.transform(result);
		xMatrix.transform(result);
		yMatrix.transform(result);
		
		result.normalize();
		return result;
	}
	
	/**
	 * Returns the vector that represents the up direction for this object normalized.
	 * @return
	 */
	public Vector3f getUpVector3f(){
		Vector3f result = new Vector3f(0,-1,0);
		float DTR = (float) (Math.PI / 180f);
		float rx = this.getRot().x * DTR;
		float ry = this.getRot().y * DTR;
		float rz = this.getRot().z * DTR;
		
		AxisAngle4f zaa = new AxisAngle4f(_zAxis, rz);
		AxisAngle4f yaa = new AxisAngle4f(_xAxis, ry);
		AxisAngle4f xaa = new AxisAngle4f(_yAxis, rx);
		Matrix4f zMatrix = new Matrix4f();
		Matrix4f yMatrix = new Matrix4f();
		Matrix4f xMatrix = new Matrix4f();
		
		zMatrix.set(zaa);
		yMatrix.set(yaa);
		xMatrix.set(xaa);

		zMatrix.transform(result);
		xMatrix.transform(result);
		yMatrix.transform(result);
		
		result.normalize();
		return result;
	}
}
