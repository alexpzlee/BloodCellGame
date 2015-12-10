import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import javax.vecmath.Vector3f;
import java.awt.event.*;

class BloodCellGame extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, ActionListener, WorldSpaceUpdateListener, WorldSpaceCollisionListener {

    /* GL, display, model transformation, and mouse control variables */
    private final GLCanvas canvas;
    private final GLU glu = new GLU();
    private GL gl;
    private int winW = 800, winH = 800;
    private boolean wireframe = false;
    private boolean cullface = true;
    private boolean flatshade = false;
    private float znear, zfar;

    
    private WorldSpace WORLDSPACE;
    public BloodCellGame() {
        super("Blood Cell Game");
        canvas = new GLCanvas();
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        getContentPane().add(canvas);
        setSize(winW, winH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        canvas.requestFocus();

    }
    public static void main(String[] args) {
    	
        new BloodCellGame();
    }
    public void keyPressed(KeyEvent e) {
    	WORLDSPACE.keyPressed(e);
    	
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }
    public void keyReleased(KeyEvent e) {
    	WORLDSPACE.keyReleased(e);
    }
    public void display(GLAutoDrawable drawable) {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, wireframe ? GL.GL_LINE : GL.GL_FILL);
        gl.glShadeModel(flatshade ? GL.GL_FLAT : GL.GL_SMOOTH);
        if (cullface)
            gl.glEnable(GL.GL_CULL_FACE);
        else
            gl.glDisable(GL.GL_CULL_FACE);

        gl.glLoadIdentity();
        
    	WORLDSPACE.draw();
    }

	public void worldSpaceCollision(WorldObject o1, WorldObject o2) {
		if(o1 == WORLDSPACE.getCameraObject()&&o2.getShader()==4){
			o1.applyScale(new Vector3f(.0005f,.0005f,.0005f));
			o1.setRadius(o1.getRadius()+.00025f);
			WORLDSPACE.deleteWorldObject(o2);
		} else if(o2 == WORLDSPACE.getCameraObject()&&o1.getShader()==4){
			o2.applyScale(new Vector3f(.0005f,.0005f,.0005f));
			o2.setRadius(o2.getRadius()+.00025f);
			WORLDSPACE.deleteWorldObject(o1);
		}
	}
	
	public void worldSpaceUpdate(WorldSpace space, float delta) {
//		System.out.println("Update");
	}
    
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL();
        znear = 0.01f;
        zfar = 1000.f;
        WORLDSPACE = new WorldSpace(gl, canvas);
        //forward object
        WorldObject cam = WORLDSPACE.addWorldObject("Cam", "models/white_blood_cell.obj",1);
        cam.setScale(new Vector3f(.005f,.005f,.005f));
        cam.setPosition(new Vector3f(0, 0, 0));
        cam.setRadius(.0025f);
        
        WorldObject obj4 = WORLDSPACE.addWorldObject("Test4", "models/straight_tube.obj",3);
        obj4.setPosition(new Vector3f(0,0,-0.05f));
        obj4.setScale(new Vector3f(.2f,.2f,.2f));
        obj4.setRotation(0,90,0);
        //generate cells
        for(int i = 0; i<99; i++){
        	if(i%2==0){
        		WorldObject red = WORLDSPACE.addWorldObject("Red"+i, "models/red_blood_cell.obj",2);
        		red.setScale(new Vector3f(.005f,.005f,.005f));
        		red.setPosition(new Vector3f((((Math.round(Math.random()))*2)-1)*(float)Math.random()*.015f, (((Math.round(Math.random()))*2)-1)*(float)Math.random()*.015f, (float)Math.random()*-.15f));
        		red.setRotation(new Vector3f((float)Math.random()*360, (float)Math.random()*360,(float)Math.random()*360));
                red.setRadius(.0025f);
        	}
        		
        	if(i%20==0){
        		WorldObject white = WORLDSPACE.addWorldObject("White"+i, "models/white_blood_cell.obj",1);
        		white.setScale(new Vector3f(.005f,.005f,.005f));
        		white.setPosition(new Vector3f((((Math.round(Math.random()))*2)-1)*(float)Math.random()*.015f, (((Math.round(Math.random()))*2)-1)*(float)Math.random()*.015f, (float)Math.random()*-.15f));
        		white.setRotation(new Vector3f((float)Math.random()*360, (float)Math.random()*360,(float)Math.random()*360));
        		white.setRadius(.0025f);
        	}
        		
        	if(i%6==0){
        		WorldObject bacteria = WORLDSPACE.addWorldObject("Bacteria"+i, "models/bacteria.obj",4);
        		bacteria.setScale(new Vector3f(.005f,.005f,.005f));
        		bacteria.setPosition(new Vector3f((((Math.round(Math.random()))*2)-1)*(float)Math.random()*.015f, (((Math.round(Math.random()))*2)-1)*(float)Math.random()*.015f, (float)Math.random()*-.15f));
        		bacteria.setRotation(new Vector3f((float)Math.random()*360, (float)Math.random()*360,(float)Math.random()*360));
        		bacteria.setRadius(.0025f);
        	}

        	
        }

        
        WORLDSPACE.setCameraObject(cam);
        WORLDSPACE.registerCollisionListener(this);
        WORLDSPACE.registerUpdateListener(this);
        WORLDSPACE.startUpdating();
        WORLDSPACE.startRendering();
        
    
        gl.glClearColor(.5f, 0.0f, 0.0f, 0.2f);
        gl.glClearDepth(1.0f);

        // white light at the eye
        float light0_position[] = {0, 0, 1, 0};
        float light0_diffuse[] = {1, 1, 1, 1};
        float light0_specular[] = {1, 1, 1, 1};
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light0_position, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, light0_diffuse, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, light0_specular, 0);

        //red light
        float light1_position[] = {-.1f, .1f, 0, 0};
        float light1_diffuse[] = {.6f, .05f, .05f, 1};
        float light1_specular[] = {.6f, .05f, .05f, 1};
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, light1_position, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, light1_diffuse, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, light1_specular, 0);

        float position[] = {.1f, .1f, 0, 0};
        float diffuse[] = {.05f, .05f, .6f, 1};
        float specular[] = {.05f, .05f, .6f, 1};
        float spotDirection[] = {-1.0f, -1.0f, 0.f};
        gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, position, 0);
        gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GL.GL_LIGHT2, GL.GL_SPECULAR, specular, 0);
        gl.glLightfv(GL.GL_LIGHT2, GL.GL_SPOT_DIRECTION, spotDirection, 0);

//        //material
//        float mat_ambient[] = {0, 0, 0, 1};
//        float mat_specular[] = {.8f, .8f, .8f, 1};
//        float mat_diffuse[] = {.4f, .4f, .4f, 1};
//        float mat_shininess[] = {128};
//        gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, mat_ambient, 0);
//        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
//        gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, mat_diffuse, 0);
//        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);
//
//        float bmat_ambient[] = {0, 0, 0, 1};
//        float bmat_specular[] = {0, .8f, .8f, 1};
//        float bmat_diffuse[] = {0, .4f, .4f, 1};
//        float bmat_shininess[] = {128};
//        gl.glMaterialfv(GL.GL_BACK, GL.GL_AMBIENT, bmat_ambient, 0);
//        gl.glMaterialfv(GL.GL_BACK, GL.GL_SPECULAR, bmat_specular, 0);
//        gl.glMaterialfv(GL.GL_BACK, GL.GL_DIFFUSE, bmat_diffuse, 0);
//        gl.glMaterialfv(GL.GL_BACK, GL.GL_SHININESS, bmat_shininess, 0);
//
//        float lmodel_ambient[] = {0, 0, 0, 1};
//        gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);
//        gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, 1);

        gl.glEnable(GL.GL_NORMALIZE);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT0);
        gl.glEnable(GL.GL_LIGHT1);
        gl.glEnable(GL.GL_LIGHT2);

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        gl.glCullFace(GL.GL_BACK);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glShadeModel(GL.GL_SMOOTH);
        
		
	}

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        winW = width;
        winH = height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.f, (float) width / (float) height, znear, zfar);
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }
    public void mousePressed(MouseEvent e) {
    	WORLDSPACE.mousePressed(e);
    }
    public void mouseReleased(MouseEvent e) {
    	WORLDSPACE.mouseReleased(e);
    }
    public void mouseDragged(MouseEvent e) {
    	WORLDSPACE.mouseDragged(e);
    }
    public void mouseMoved(MouseEvent e) {
//    	WORLDSPACE.mouseMoved(e);
    }
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }
    public void keyTyped(KeyEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
    }
    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
}
