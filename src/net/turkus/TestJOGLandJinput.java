package net.turkus;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
 

public class TestJOGLandJinput implements GLEventListener {
	// Jinput won't work properly with a keyboard on Linux 
	// until you chmod the keyboard. 
	// Find out where your keyboard is with 
	// grep "/dev/input/event" /var/log/Xorg.0.log
	// then
	// sudo chmod a+r /dev/input/event(whichever one it was)
	
	// See example OpenGL setup at 
	// https://gist.github.com/gouessej/3420e2b6f632efdddf98
	
	// OpenGL/JOGL stuff
	private static final int FPS = 60;
	static GLU glu = new GLU();
	private Texture texture[] = new Texture[2];	
	private float aspect;
	
	// Jinput stuff
	private float lX,lY,rX,rY, lZ, rZ; // controller axes (left and right xyz)
	static int controllerIndex=0;
	static int stickIndex; 
	static int gamepadIndex;
    static int keyboardIndex;
    static int[] analogCompIndex = new int[6];
    static float deadStickZone=0.02f;
	
	// Random, probably temporary, stuff
	private float randomCounterFromStart = 0;
	private float rotate = 0;
	

	@Override
	public void display(GLAutoDrawable gLDrawable) {
		
		if(gamepadIndex != 99){pollGamepad();}
		if(keyboardIndex != 99){pollKeyboard();}
		update();
		
		final GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		
		// 3D elements
        gl.glFrustum(-0.5, 0.5, -aspect/2, aspect/2, 1, 5);
        gl.glColor3d(1, 1, 1);
        
        // How about some spheres?
        GLUquadric quad = glu.gluNewQuadric();
        glu.gluQuadricTexture(quad, true);
        glu.gluQuadricDrawStyle(quad, GLU.GLU_FILL);
        glu.gluQuadricNormals(quad, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(quad, GLU.GLU_OUTSIDE);
        
        gl.glPushMatrix();
        gl.glTranslated(rX, rY, (-rZ * 1.5) - 3.5);
        gl.glRotatef(rotate, 1, 1, 1);
        gl.glEnable(GL.GL_TEXTURE_2D);
        texture[0].bind(gl);
        glu.gluSphere(quad, 0.3, 32, 32);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        
        gl.glPushMatrix();
        gl.glTranslated(lX, lY, (-lZ * 1.5) - 3.5);
        gl.glRotatef(rotate, 1, -1, -1);
        gl.glEnable(GL.GL_TEXTURE_2D);
        texture[1].bind(gl);
        glu.gluSphere(quad, 0.3, 32, 32);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glPopMatrix();
        
		// 2D elements (console)
        gl.glLoadIdentity();
		gl.glOrtho(-1, 1, -1, 1, -1, 1); // 2D!
		// Monitor lines near the left and right edges of the window
        gl.glLineWidth(6);  // Turns out this can be limited depending on the device...
        
        gl.glBegin(GL.GL_LINES);
        //Left monitor line
        gl.glColor3d(0.6, 0.0, 0.3);
        gl.glVertex3d(-0.95, -0.90, 0.0);
        gl.glColor3d(0.0, 0.0, 1.0);
        gl.glVertex3d(-0.95, lZ * 0.95, 0.0); 
        //Right monitor line
        gl.glColor3d(1.0, 0.0, 0.0);
        gl.glVertex3d(0.95, -0.90, 0.0);
        gl.glColor3d(0.0, 1.0, 0.0);
        gl.glVertex3d(0.95, rZ * 0.95, 0.0);
        gl.glEnd();
        
        gl.glLineWidth(1);
        //gl.glColor3d(1, 1, 1);//don't leave colors all screwy
                
	}
 
	private void pollGamepad(){
		Controller gamepad = ControllerEnvironment.getDefaultEnvironment().getControllers()[gamepadIndex];
		Component[] gamepadComps = gamepad.getComponents();
		gamepad.poll();
		// TODO: display a table with all polled values
		lX = gamepadComps[analogCompIndex[0]].getPollData() - 0.3f;
		lY = gamepadComps[analogCompIndex[1]].getPollData();
		lZ = gamepadComps[analogCompIndex[2]].getPollData();
		rX = gamepadComps[analogCompIndex[3]].getPollData() + 0.3f;
		rY = gamepadComps[analogCompIndex[4]].getPollData();
		rZ = gamepadComps[analogCompIndex[5]].getPollData();
		for(int i = 0; i < gamepadComps.length; i++){
			if(gamepadComps[i].getPollData() != 0){
				boolean itsAnalog = false;
				for(int ind: analogCompIndex){
					if(i == ind){
						itsAnalog = true;
					}
				}
				if(!itsAnalog){
					System.out.println(i  + ": " + gamepadComps[i].getPollData() + ", ");
				}
				
			}
		}
	}
	
	private void pollKeyboard(){
		Controller kbd = ControllerEnvironment.getDefaultEnvironment().getControllers()[keyboardIndex];
		Component[] keyboardComps = kbd.getComponents();
		kbd.poll();
		for(int i = 0; i<keyboardComps.length;i++){
			if(keyboardComps[i].getPollData() != 0){
				System.out.println(i + ": " + keyboardComps[i].getPollData() + ", ");
			}
		}
	}
	
	private void update(){
		randomCounterFromStart += 0.4;
		rotate += 1;
	}
	
	@Override
	public void init(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		
		loadTextures(gl);
		
		initControllers();
	}
	
	public void loadTextures(GL2 gl){
            try {
            	String toLoad = "images/" + "EarthBig.png";
            	System.out.println("Loading image for " + toLoad);
                InputStream stream = getClass().getResourceAsStream(toLoad);
                texture[0] = TextureIO.newTexture(stream, false, "png");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
            	String toLoad = "images/" + "Io.png";
            	System.out.println("Loading image for " + toLoad);
                InputStream stream = getClass().getResourceAsStream(toLoad);
                texture[1] = TextureIO.newTexture(stream, false, "png");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
    }
 
	@Override
	public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
		aspect = (float) height / (float) width;
	}
 
	@Override
	public void dispose(GLAutoDrawable gLDrawable) {
	}
	
	private static void initControllers(){
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		Controller[] cs = ce.getControllers();
		gamepadIndex = 99;
		keyboardIndex = 99;
		stickIndex = 99;
		for (int i = 0; i < cs.length; i++) {
			if(cs[i].getType()==Controller.Type.GAMEPAD){gamepadIndex=i;}
			if(cs[i].getType()==Controller.Type.KEYBOARD){keyboardIndex=i;}
			if(cs[i].getType()==Controller.Type.STICK){stickIndex=i;}
		}
		if(gamepadIndex != 99){
			Component[] comps = cs[gamepadIndex].getComponents();
			if (comps.length > 0) {
				int analogCompCounter=0;
				for(int i=0;i<comps.length;i++){
					if(comps[i].isAnalog()){
						analogCompIndex[analogCompCounter]=i;
						analogCompCounter++;
					}
				}	
			}
		}
		
	    System.out.println("Keyboard found at index " + keyboardIndex);
	    System.out.println("Stick found at index " + stickIndex);
		System.out.println("Gamepad found at index " + gamepadIndex + "\n");
		
		
		if(cs.length > 0){
			for(int t = 0; t < cs.length; t++){
				printControllerDetails(cs[t]);
			}
		}
	}
	
	private static void printControllerDetails(Controller c) {
		// shows basic information about this controller
		System.out.println("name: " + c.getName());
		System.out.println("type: " + c.getType());
		System.out.println("port: " + c.getPortType());

		// shows information about each Axis instance of this controller
		printAxesDetails(c.getComponents());

		// shows details about this controller's sub-controller		
		Controller[] subControllers = c.getControllers();
		if (subControllers.length > 0) {
			for (int i = 0; i < subControllers.length; i++) {
				System.out.println("subcontroller: " + i);
				printControllerDetails(subControllers[i]);
			}
		}else System.out.println("no subcontrollers found\n");
	}
	
	private static void printAxesDetails(Component[] comps) {
		if (comps.length > 0) {
			System.out.println("axes:");

			// iterate through all axes and print their information
			for (int i = 0; i < comps.length; i++) {
				System.out.println(
					i
						+ " - "
						+ comps[i].getName()
						+ " - "
						+ comps[i].getIdentifier()
						+ " - "
						+ (comps[i].isRelative() ? "relative" : "absolute")
						+ " - "
						+ (comps[i].isAnalog() ? "analog" : "digital")
						+ " - "
						+ comps[i].getDeadZone()
						+ " - "
						+ comps[i].getPollData());
			}
		}

	}

 
	public static void main(String[] args) {
		GLProfile glp = GLProfile.getDefault();
		System.out.println(glp.getName());
		GLCapabilities caps = new GLCapabilities(glp);
		final GLCanvas canvas = new GLCanvas(caps);
		final Frame frame = new Frame("Yep");
		final FPSAnimator animator = new FPSAnimator(canvas, FPS);
		canvas.addGLEventListener(new TestJOGLandJinput());
		frame.add(canvas);
		frame.setSize(1280, 960);
		frame.setResizable(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				animator.stop();
				frame.dispose();
				System.exit(0);
			}
		});
		frame.setVisible(true);
		animator.start();
		canvas.requestFocus();
	}
}
