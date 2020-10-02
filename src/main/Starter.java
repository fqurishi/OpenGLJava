package main;
import objects.*;
import commands.*;

import java.nio.*;
import javax.swing.*;
import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public class Starter extends JFrame implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener
{	private GLCanvas myCanvas;
	private int renderingProgramShadows, renderingProgram, renderingProgramLight, renderingProgramCubeMap, renderingProgramEnvironment,
			renderingProgramTess, renderingProgramBump, renderingProgramGeo, renderingProgram3D;
	private int vao[] = new int[1];
	private int vbo[] = new int[22];
	//
	//gl4
	private GL4 gl;

	// model stuff
	private ImportedModel pyramid;
	private ImportedModel ground;
	private Torus myTorus;
	private Sphere mySphere;
	private int numPyramidVertices, numTorusVertices, numGroundVertices, numTorusIndices, numPyramidIndices, 
			numSphereVertices, numSphereIndices;
	
	// location of torus, pyramid, light, axis, and camera
	private Vector3f torusLoc = new Vector3f(0.0f, 45.0f, 0.0f);
	private Vector3f sphereLoc = new Vector3f(280.0f, -19.0f, 0.0f);
	private Vector3f sphere2Loc = new Vector3f(-280.0f, -19.0f, 0.0f);
	private Vector3f sphere3Loc = new Vector3f(0.0f, -19.0f, 280.0f);
	private Vector3f pyrLoc = new Vector3f(0.0f, 3.0f, 0.0f);
	private Vector3f lineLoc = new Vector3f(0.0f, 0.0f, 0.0f);
	private Camera camera = new Camera(-270.0f, 100.0f, 330.0f);
	private Vector3f lightLoc = new Vector3f(0.0f, 60.0f, -50.0f);
	private float terLocX, terLocY, terLocZ;
	private float tessInner = 30.0f;
	private float tessOuter = 20.0f;
	
	// white light properties
	private float[] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private float[] lightAmbient = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
	private float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	private float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		
	// gold material
	private float[] GmatAmb = Utils.goldAmbient();
	private float[] GmatDif = Utils.goldDiffuse();
	private float[] GmatSpe = Utils.goldSpecular();
	private float GmatShi = Utils.goldShininess();
	
	// silver material
	private float[] BmatAmb = Utils.silverAmbient();
	private float[] BmatDif = Utils.silverDiffuse();
	private float[] BmatSpe = Utils.silverSpecular();
	private float BmatShi = Utils.silverShininess();
	// no material
	private float[] ZmatAmb = Utils.zeroAmbient();
	private float[] ZmatDif = Utils.zeroDiffuse();
	private float[] ZmatSpe = Utils.zeroSpecular();
	private float ZmatShi = Utils.zeroShininess();
	
	private float[] thisAmb, thisDif, thisSpe, matAmb, matDif, matSpe;
	private float thisShi, matShi;
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();
	private Matrix4f shadowMVP1 = new Matrix4f();
	private Matrix4f shadowMVP2 = new Matrix4f();
	private Matrix4f b = new Matrix4f();
	
	// texture stuff
	private int teal;
	private int skyboxTexture;
	private int TileTexture;
	private int rockyTexture;
	private int rockyTextureNM;
	private int rockyTextureHM;
	private int grassyTexture;
	private int grassyTextureNM;
	private int grassyTextureHM;
	private int stoneTexture;
	private int stoneTextureNM;
	private int stoneTextureHM;
	private int sandTexture;
	private int sandTextureNM;
	private int sandTextureHM;
	

	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private Matrix4f mvpMat = new Matrix4f(); // model-view-perspective matrix
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	private Matrix4f cubeVmat = new Matrix4f();
	private int vLoc, mvLoc, mvpLoc, projLoc, nLoc, sLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc;
	private float aspect;
	private float amt = 0.0f;
	private float amt2 = 0.0f;
	private float calcDistance = 0.0f;
	private float rotAmt = 0.0f;
	private Vector3f currentLightPos = new Vector3f();
	private float[] lightPos = new float[3];
	private Vector3f origin = new Vector3f(0.0f, 0.0f, 0.0f);
	private Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
	
	//noise stuff
	private int noiseTexture;
	private int noiseHeight= 300;
	private int noiseWidth = 300;
	private int noiseDepth = 300;
	private double[][][] noise = new double[noiseHeight][noiseWidth][noiseDepth];
	private java.util.Random random = new java.util.Random();
	
	// variables for misc functions
	private boolean spaceCheck = true;
	private boolean tCheck = true;
	private boolean lightCheck = true;
	private int colorFlag = 0;
	
	public Starter()
	{	setTitle("Faisl Qurishi - Java OpenGl Demo");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addMouseWheelListener(this);
		myCanvas.addMouseMotionListener(this);
		SpaceCommand spaceCommand = new SpaceCommand(this);
		LightCommand lightCommand = new LightCommand(this);
		TCommand tCommand = new TCommand(this);
		this.add(myCanvas);
		
		
		
		JComponent contentPane = (JComponent) this.getContentPane();
		
		//camera set up
		ForwardCommand.getInstance().setCamera(camera);
		UpCommand.getInstance().setCamera(camera);
		DownCommand.getInstance().setCamera(camera);
		BackCommand.getInstance().setCamera(camera);
		RightCommand.getInstance().setCamera(camera);
		LeftCommand.getInstance().setCamera(camera);
		PanLeftCommand.getInstance().setCamera(camera);
		PanRightCommand.getInstance().setCamera(camera);
		PitchUpCommand.getInstance().setCamera(camera);
		PitchDownCommand.getInstance().setCamera(camera);
		
		
		
		// get the "focus is in the window" input map for the content pane
		int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
		InputMap imap = contentPane.getInputMap(mapName);
		String ACTION_KEY = "space";
		String ACTION_KEY2 = "pitch left";
		String ACTION_KEY3 = "pitch right";
		String ACTION_KEY4 = "backward";
		String ACTION_KEY5 = "forward";
		String ACTION_KEY6 = "right";
		String ACTION_KEY7 = "left";
		String ACTION_KEY8 = "down";
		String ACTION_KEY9 = "up";
		String ACTION_KEY10 = "pitch down";
		String ACTION_KEY11 = "pitch up";
		String ACTION_KEY12 = "lights";
		String ACTION_KEY13 = "t";
		// create a keystroke object to represent the  key
		KeyStroke space = KeyStroke.getKeyStroke(' ');
		KeyStroke wKey = KeyStroke.getKeyStroke('w');
		KeyStroke sKey = KeyStroke.getKeyStroke('s');
		KeyStroke aKey = KeyStroke.getKeyStroke('a');
		KeyStroke dKey = KeyStroke.getKeyStroke('d');
		KeyStroke qKey = KeyStroke.getKeyStroke('q');
		KeyStroke eKey = KeyStroke.getKeyStroke('e');
		KeyStroke upKey = KeyStroke.getKeyStroke("UP");
		KeyStroke downKey = KeyStroke.getKeyStroke("DOWN");
		KeyStroke leftKey = KeyStroke.getKeyStroke("LEFT");
		KeyStroke rightKey = KeyStroke.getKeyStroke("RIGHT");
		KeyStroke lKey = KeyStroke.getKeyStroke('l');
		KeyStroke tKey = KeyStroke.getKeyStroke('t');
		imap.put(space, ACTION_KEY);
		imap.put(wKey, ACTION_KEY5);
		imap.put(aKey, ACTION_KEY4);
		imap.put(sKey, ACTION_KEY6);
		imap.put(dKey, ACTION_KEY7);
		imap.put(qKey, ACTION_KEY9);
		imap.put(eKey, ACTION_KEY8);
		imap.put(leftKey, ACTION_KEY3);
		imap.put(rightKey, ACTION_KEY2);
		imap.put(downKey, ACTION_KEY10);
		imap.put(upKey, ACTION_KEY11);
		imap.put(lKey, ACTION_KEY12);
		imap.put(tKey, ACTION_KEY13);
		
		// get the action map for the content pane
		ActionMap amap = contentPane.getActionMap();
		amap.put(ACTION_KEY, spaceCommand);
		amap.put(ACTION_KEY2, PanRightCommand.getInstance());
		amap.put(ACTION_KEY3, PanLeftCommand.getInstance());
		amap.put(ACTION_KEY4, LeftCommand.getInstance());
		amap.put(ACTION_KEY5, BackCommand.getInstance());
		amap.put(ACTION_KEY6, ForwardCommand.getInstance());
		amap.put(ACTION_KEY7, RightCommand.getInstance());
		amap.put(ACTION_KEY8, UpCommand.getInstance());
		amap.put(ACTION_KEY9, DownCommand.getInstance());
		amap.put(ACTION_KEY10, PitchDownCommand.getInstance());
		amap.put(ACTION_KEY11, PitchUpCommand.getInstance());
		amap.put(ACTION_KEY12, lightCommand);
		amap.put(ACTION_KEY13, tCommand);
		//have the JFrame request keyboard focus
		this.requestFocus();
		
		camera.panR(40.0f);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	public void display(GLAutoDrawable drawable)
	{	gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(0.0f, 0.0f, 0.2f, 1.0f);		
		gl.glClear(GL_COLOR_BUFFER_BIT);
		
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_POLYGON_OFFSET_FILL);	//  for reducing
		gl.glPolygonOffset(3.0f, 5.0f);		//  shadow artifacts

		passOne();
		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		
		skyboxPass();
	
		gl.glDrawBuffer(GL_FRONT);
		passTwo();
		
		amt += 0.5;
		
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(renderingProgramShadows);
		if (lightCheck){
			currentLightPos.set(lightLoc);
		}
		lightVmat.identity().setLookAt(currentLightPos, origin, up);	// vector from light to origin
		lightPmat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		
		// draw the torus
		if(tCheck){
		mMat.identity();
		mMat.translate(torusLoc.x(), torusLoc.y(), torusLoc.z());
		mMat.rotateX((float)Math.toRadians(amt));
		mMat.rotateZ((float)Math.toRadians(-amt));
		if (amt2 != 0){
		mMat.scale(5.0f, 5.0f, 5.0f);
		}else{
		mMat.scale(12.0f, 12.0f, 12.0f);
		}
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		sLoc = gl.glGetUniformLocation(renderingProgramShadows, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
		} else{
		// draw the deleted torus
		mMat.identity();
		mMat.translate(torusLoc.x(), torusLoc.y(), torusLoc.z());
		mMat.rotateX((float)Math.toRadians(amt));
		mMat.rotateZ((float)Math.toRadians(-amt));
		if (amt2 != 0){
		mMat.scale(5.0f, 5.0f, 5.0f);
		}else{
		mMat.scale(12.0f, 12.0f, 12.0f);
		}
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		sLoc = gl.glGetUniformLocation(renderingProgramShadows, "shadowMVP");
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
			
		}
		
		// draw the pyramid
		
		mMat.identity();
		mMat.translate(pyrLoc.x(), pyrLoc.y(), pyrLoc.z());
		mMat.scale(30.0f, 30.0f, 30.0f);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		//vbo vertex
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numPyramidVertices);
		
		//draw the sphere 
		mMat.identity();
		mMat.translate(sphereLoc.x(), sphereLoc.y(), sphereLoc.z());
		mMat.rotateX(-amt2);
		mMat.scale(10.0f, 10.0f, 10.0f);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		//vbo vertex
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
		
		
		//draw the sphere 2
		mMat.identity();
		mMat.translate(sphere2Loc.x(), sphere2Loc.y(), sphere2Loc.z());
		mMat.rotateX(amt2);
		mMat.scale(10.0f, 10.0f, 10.0f);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		//vbo vertex
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
		
		
		//draw the sphere 3
		mMat.identity();
		mMat.translate(sphere3Loc.x(), sphere3Loc.y(), sphere3Loc.z());
		mMat.rotateX(-amt2);
		mMat.scale(10.0f, 10.0f, 10.0f);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		//vbo vertex
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
		
		
		// draw the ground 
		mMat.identity();
		mMat.translate(terLocX, terLocY-35, terLocZ);
		mMat.scale(300.0f, 300.0f, 300.0f);

		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		// vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numGroundVertices);
		
		//draw the second ground
		mMat.identity();
		mMat.translate(terLocX, terLocY-35, terLocZ);
		mMat.scale(550.0f, 550.0f, 550.0f);
		
		shadowMVP1.identity();
		shadowMVP1.mul(lightPmat);
		shadowMVP1.mul(lightVmat);
		shadowMVP1.mul(mMat);
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP1.get(vals));
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);

		gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
		gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, 64*64);
		
		
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void skyboxPass()
	{	gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		vMat.identity().mul(camera.getMatrix());
	
		
		// draw cube map
		
		gl.glUseProgram(renderingProgramCubeMap);
		
		vLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "v_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "proj_matrix");
		
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
				
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE3);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
		
		
		
	}
	
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	gl = (GL4) GLContext.getCurrentGL();
		
		if(tCheck){
		// draw the torus
		gl.glUseProgram(renderingProgramEnvironment);
		
		mvLoc = gl.glGetUniformLocation(renderingProgramEnvironment, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramEnvironment, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramEnvironment, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgramEnvironment, "shadowMVP");

		
		thisAmb = BmatAmb; // the torus is silver
		thisDif = BmatDif;
		thisSpe = BmatSpe;
		thisShi = BmatShi;
		
		if (lightCheck)
		{	currentLightPos.set(lightLoc);
		}
		installLights(renderingProgramEnvironment, vMat);

		mMat.identity();
		mMat.translate(torusLoc.x(), torusLoc.y(), torusLoc.z());
		mMat.rotateX((float)Math.toRadians(amt));
		mMat.rotateZ((float)Math.toRadians(-amt));
		if (amt2 != 0){
		mMat.scale(5.0f, 5.0f, 5.0f);
		}else{
		mMat.scale(12.0f, 12.0f, 12.0f);
		}
		
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);
        gl.glActiveTexture(GL_TEXTURE4);
        gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
		}
		else{
		// draw the deleted torus
		gl.glUseProgram(renderingProgramGeo);

		mvLoc = gl.glGetUniformLocation(renderingProgramGeo, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramGeo, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramGeo, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgramEnvironment, "shadowMVP");
		
		thisAmb = BmatAmb; // the torus is silver
		thisDif = BmatDif;
		thisSpe = BmatSpe;
		thisShi = BmatShi;
		
		
		mMat.identity();
		mMat.translate(torusLoc.x(), torusLoc.y(), torusLoc.z());
		mMat.rotateX((float)Math.toRadians(amt));
		mMat.rotateZ((float)Math.toRadians(-amt));
		if (amt2 != 0){
		mMat.scale(5.0f, 5.0f, 5.0f);
		}else{
		mMat.scale(12.0f, 12.0f, 12.0f);
		}
		
		currentLightPos.set(lightLoc);
		installLights(renderingProgramGeo, vMat);
		
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
        gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);
		
		// texture
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, teal);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[21]);
		gl.glDrawElements(GL_TRIANGLES, numTorusIndices, GL_UNSIGNED_INT, 0);
			
		}
		
		
		// draw the pyramid
		gl.glUseProgram(renderingProgram);
		
		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgram, "shadowMVP");
		
		thisAmb = GmatAmb; // the pyramid is gold
		thisDif = GmatDif;
		thisSpe = GmatSpe;
		thisShi = GmatShi;
		
		mMat.identity();
		mMat.translate(pyrLoc.x(), pyrLoc.y(), pyrLoc.z());
		mMat.scale(30.0f, 30.0f, 30.0f);
		
		currentLightPos.set(lightLoc);
		installLights(renderingProgram, vMat);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
        gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(2);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// texture
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, TileTexture);


		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numPyramidVertices);
		
		//draw ground
		if(spaceCheck){
		gl.glUseProgram(renderingProgramBump);
		
		mvLoc = gl.glGetUniformLocation(renderingProgramBump, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramBump, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramBump, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgramBump, "shadowMVP");
		
		 thisAmb = ZmatAmb; // the ground is nada
		 thisDif = ZmatDif;
		 thisSpe = ZmatSpe;
		 thisShi = ZmatShi;
		
		
		
		vMat.identity().mul(camera.getMatrix());

		mMat.identity();
		mMat.translate(terLocX, terLocY-35, terLocZ);
		mMat.scale(300.0f, 300.0f, 300.0f);
		
		if(lightCheck){
			currentLightPos.set(lightLoc);
		}
		installLights(renderingProgramBump, vMat);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
	
		// vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// texture coordinate buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		// normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		// texture
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, grassyTexture);

		// height map
		gl.glActiveTexture(GL_TEXTURE5);
		gl.glBindTexture(GL_TEXTURE_2D, grassyTextureHM);
		
		// normal map
		gl.glActiveTexture(GL_TEXTURE6);
		gl.glBindTexture(GL_TEXTURE_2D, grassyTextureNM);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numGroundVertices);
		}
		else{
		//draw second ground
		gl.glUseProgram(renderingProgramTess);
		mvpLoc = gl.glGetUniformLocation(renderingProgramTess, "mvp");
		mvLoc = gl.glGetUniformLocation(renderingProgramTess, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramTess, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramTess, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgramBump, "shadowMVP");
		
		vMat.identity().mul(camera.getMatrix());

		mMat.identity();
		mMat.translate(terLocX, terLocY-35, terLocZ);
		mMat.scale(550.0f, 550.0f, 550.0f);
		
		if(lightCheck){
			currentLightPos.set(lightLoc);
		}	
		installLights(renderingProgramTess, vMat);
		
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		mvpMat.identity();
		mvpMat.mul(pMat);
		mvpMat.mul(vMat);
		mvpMat.mul(mMat);
		
		mvMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);
		
		
		gl.glUniformMatrix4fv(mvpLoc, 1, false, mvpMat.get(vals));
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
		
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, sandTexture);
		gl.glActiveTexture(GL_TEXTURE5);
		gl.glBindTexture(GL_TEXTURE_2D, sandTextureHM);
		gl.glActiveTexture(GL_TEXTURE6);
		gl.glBindTexture(GL_TEXTURE_2D, sandTextureNM);
	
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);

		gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
		gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, 64*64);
		}
		
		// draw sphere
		gl.glUseProgram(renderingProgramBump);
		
		mvLoc = gl.glGetUniformLocation(renderingProgramBump, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramBump, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramBump, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgramBump, "shadowMVP");
		
		 thisAmb = BmatAmb; // the sphere is silver
		 thisDif = BmatDif;
		 thisSpe = BmatSpe;
		 thisShi = BmatShi;
		
		
		
		vMat.identity().mul(camera.getMatrix());

		mMat.identity();
		
		if(sphereLoc.x() >= 30.0f && sphereLoc.x() < 290.0f){
		sphereLoc = new Vector3f(sphereLoc.x()-0.5f, sphereLoc.y(), sphereLoc.z());
		amt2 += 0.02;
		}else{
			sphereLoc = new Vector3f(sphereLoc.x(), sphereLoc.y(), sphereLoc.z());
			amt2 = 0.0f;
		}
		mMat.translate(sphereLoc.x(), sphereLoc.y(), sphereLoc.z());
		mMat.rotateZ(amt2);
		mMat.scale(10.0f, 10.0f, 10.0f);
		
		
		if(lightCheck){
			currentLightPos.set(lightLoc);
		}
		installLights(renderingProgramBump, vMat);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);
		
		// texture
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, rockyTexture);

		// height map
		gl.glActiveTexture(GL_TEXTURE5);
		gl.glBindTexture(GL_TEXTURE_2D, rockyTextureHM);
		
		// normal map
		gl.glActiveTexture(GL_TEXTURE6);
		gl.glBindTexture(GL_TEXTURE_2D, rockyTextureNM);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
		// draw sphere 2
		gl.glUseProgram(renderingProgramBump);
		
		mvLoc = gl.glGetUniformLocation(renderingProgramBump, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgramBump, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramBump, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgramBump, "shadowMVP");
		
		 thisAmb = BmatAmb; // the sphere is silver
		 thisDif = BmatDif;
		 thisSpe = BmatSpe;
		 thisShi = BmatShi;
		
		
		
		vMat.identity().mul(camera.getMatrix());

		mMat.identity();
		
		if(sphere2Loc.x() <= -30.0f && sphere2Loc.x() > -290.0f){
		sphere2Loc = new Vector3f(sphere2Loc.x()+0.5f, sphere2Loc.y(), sphere2Loc.z());
		amt2 += 0.02;
		}else{
			sphere2Loc = new Vector3f(sphere2Loc.x(), sphere2Loc.y(), sphere2Loc.z());
			amt2 = 0.0f;
		}
		mMat.translate(sphere2Loc.x(), sphere2Loc.y(), sphere2Loc.z());
		mMat.rotateZ(-amt2);
		mMat.scale(10.0f, 10.0f, 10.0f);
		
		
		if(lightCheck){
			currentLightPos.set(lightLoc);
		}
		installLights(renderingProgramBump, vMat);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[19]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		gl.glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(4);
		
		// texture
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, stoneTexture);

		// height map
		gl.glActiveTexture(GL_TEXTURE5);
		gl.glBindTexture(GL_TEXTURE_2D, stoneTextureHM);
		
		// normal map
		gl.glActiveTexture(GL_TEXTURE6);
		gl.glBindTexture(GL_TEXTURE_2D, stoneTextureNM);

		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);
		
		// draw sphere 3
		gl.glUseProgram(renderingProgram3D);
		
		mvLoc = gl.glGetUniformLocation(renderingProgram3D, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram3D, "proj_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram3D, "norm_matrix");
		sLoc = gl.glGetUniformLocation(renderingProgram3D, "shadowMVP");
		
		 thisAmb = ZmatAmb; // the sphere is nada
		 thisDif = ZmatDif;
		 thisSpe = ZmatSpe;
		 thisShi = ZmatShi;
		
		
		
		vMat.identity().mul(camera.getMatrix());

		mMat.identity();
		
		if(sphere3Loc.z() >= 30.0f && sphere3Loc.z() < 290.0f){
		sphere3Loc = new Vector3f(sphere3Loc.x(), sphere3Loc.y(), sphere3Loc.z()-0.5f);
		amt2 += 0.02;
		}else{
			sphere3Loc = new Vector3f(sphere3Loc.x(), sphere3Loc.y(), sphere3Loc.z());
			amt2 = 0.0f;
		}
		mMat.translate(sphere3Loc.x(), sphere3Loc.y(), sphere3Loc.z());
		mMat.rotateX(-amt2);
		mMat.scale(10.0f, 10.0f, 10.0f);
		
		
		if(lightCheck){
			currentLightPos.set(lightLoc);
		}
		installLights(renderingProgram3D, vMat);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);
		invTrMat.transpose(invTrMat);
		
		shadowMVP2.identity();
		shadowMVP2.mul(b);
		shadowMVP2.mul(lightPmat);
		shadowMVP2.mul(lightVmat);
		shadowMVP2.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		gl.glUniformMatrix4fv(sLoc, 1, false, shadowMVP2.get(vals));
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// texture
		gl.glActiveTexture(GL_TEXTURE7);
		gl.glBindTexture(GL_TEXTURE_3D, noiseTexture);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVertices);

		
		// Draw the light
        gl.glPointSize(12);
        gl.glUseProgram(renderingProgramLight);
		changeColor();
		
        mvLoc = gl.glGetUniformLocation(renderingProgramLight, "mv_matrix");
        projLoc = gl.glGetUniformLocation(renderingProgramLight, "proj_matrix");
		
        mMat.identity();
        mMat.translate(lightLoc.x(), lightLoc.y(), lightLoc.z());
		
		
        mvMat.identity();
        mvMat.mul(vMat);
        mvMat.mul(mMat);

        gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
        gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));

        gl.glDrawArrays(GL_POINTS, 0, 1);
        gl.glPointSize(12);
		
	}

	public void init(GLAutoDrawable drawable)
	{	gl = (GL4) GLContext.getCurrentGL();
		renderingProgramShadows = Utils.createShaderProgram("shaders/vertShadowShader.glsl", "shaders/fragShadowShader.glsl");
		renderingProgram = Utils.createShaderProgram("shaders/vertShader.glsl", "shaders/fragShader.glsl");
		renderingProgramLight = Utils.createShaderProgram("shaders/vertLShader.glsl", "shaders/fragLShader.glsl");
		renderingProgramCubeMap = Utils.createShaderProgram("shaders/vertCShader.glsl", "shaders/fragCShader.glsl");
		renderingProgramEnvironment = Utils.createShaderProgram("shaders/vertEShader.glsl", "shaders/fragEShader.glsl");
		renderingProgramTess = Utils.createShaderProgram("shaders/vertTesShader.glsl", "shaders/tessCShader.glsl", "shaders/tessEShader.glsl", "shaders/fragTesShader.glsl");
		renderingProgramBump = Utils.createShaderProgram("shaders/vertBumpShader.glsl", "shaders/fragBumpShader.glsl");
		renderingProgramGeo = Utils.createShaderProgram("shaders/vertGShader.glsl", "shaders/geomShader.glsl", "shaders/fragGShader.glsl");
		renderingProgram3D = Utils.createShaderProgram("shaders/vert3DShader.glsl", "shaders/frag3DShader.glsl");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		ground = new ImportedModel("grid.obj");
		pyramid = new ImportedModel("pyr.obj");

		setupVertices();
		skyboxTexture = Utils.loadCubeMap("cubeMap");
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		setupShadowBuffers();
				
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
			
		generateNoise();
			
		teal = Utils.loadTexture("textures/teal.png");
		TileTexture = Utils.loadTexture("textures/TileTexture.png");
		rockyTexture = Utils.loadTexture("textures/rockyTexture.png");
		rockyTextureNM = Utils.loadTexture("textures/rockyTextureNM.png");
		rockyTextureHM = Utils.loadTexture("textures/rockyTextureHM.png");
		grassyTexture = Utils.loadTexture("textures/grassyTexture.png");
		grassyTextureNM = Utils.loadTexture("textures/grassyTextureNM.png");
		grassyTextureHM = Utils.loadTexture("textures/grassyTextureHM.png");
		stoneTexture = Utils.loadTexture("textures/stoneTexture.png");
		stoneTextureNM = Utils.loadTexture("textures/stoneTextureNM.png");
		stoneTextureHM = Utils.loadTexture("textures/stoneTextureHM.png");
		sandTexture = Utils.loadTexture("textures/sandTexture.png");
		sandTextureNM = Utils.loadTexture("textures/sandTextureNM.png");
		sandTextureHM = Utils.loadTexture("textures/sandTextureHM.png");	
		noiseTexture = buildNoiseTexture();
	
		
	}
	
	private void setupShadowBuffers()
	{	gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadowBuffer, 0);
	
		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	private void setupVertices()
	{	gl = (GL4) GLContext.getCurrentGL();
		//sphere
		mySphere = new Sphere(500);
		numSphereVertices = mySphere.getIndices().length;
		
		int[] indices = mySphere.getIndices();
		Vector3f[] vertices = mySphere.getVertices();
		Vector2f[] texCoords = mySphere.getTexCoords();
		Vector3f[] normals = mySphere.getNormals();
		Vector3f[] tangents = mySphere.getTangents();
		
		float[] spherePvalues = new float[indices.length*3];
		float[] sphereTvalues = new float[indices.length*2];
		float[] sphereNvalues = new float[indices.length*3];
		float[] sphereTanvalues = new float[indices.length*3];

		for (int i=0; i<indices.length; i++)
		{	spherePvalues[i*3]   = (float) (vertices[indices[i]]).x();
			spherePvalues[i*3+1] = (float) (vertices[indices[i]]).y();
			spherePvalues[i*3+2] = (float) (vertices[indices[i]]).z();
			sphereTvalues[i*2]   = (float) (texCoords[indices[i]]).x();
			sphereTvalues[i*2+1] = (float) (texCoords[indices[i]]).y();
			sphereNvalues[i*3]   = (float) (normals[indices[i]]).x();
			sphereNvalues[i*3+1] = (float) (normals[indices[i]]).y();
			sphereNvalues[i*3+2] = (float) (normals[indices[i]]).z();
			sphereTanvalues[i*3] = (float) (tangents[indices[i]]).x();
			sphereTanvalues[i*3+1] = (float) (tangents[indices[i]]).y();
			sphereTanvalues[i*3+2] = (float) (tangents[indices[i]]).z();
		}
	
		//grid
		numGroundVertices = ground.getNumVertices();
		Vector3f[] gridVertices = ground.getVertices();
		Vector2f[] gridTexCoords = ground.getTexCoords();
		Vector3f[] gridNormals = ground.getNormals();

		float[] gridPvalues = new float[numGroundVertices*3];
		float[] gridTvalues = new float[numGroundVertices*2];
		float[] gridNvalues = new float[numGroundVertices*3];
		
		for (int i=0; i<numGroundVertices; i++)
		{	gridPvalues[i*3]   = (float) (gridVertices[i]).x();
			gridPvalues[i*3+1] = (float) (gridVertices[i]).y();
			gridPvalues[i*3+2] = (float) (gridVertices[i]).z();
			gridTvalues[i*2]   = (float) (gridTexCoords[i]).x();
			gridTvalues[i*2+1] = (float) (gridTexCoords[i]).y();
			gridNvalues[i*3]   = (float) (gridNormals[i]).x();
			gridNvalues[i*3+1] = (float) (gridNormals[i]).y();
			gridNvalues[i*3+2] = (float) (gridNormals[i]).z();
		}	
	
	
		// cube
		float[] cubeVertexPositions =
		{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};
		float[] cubeTextureCoordinates = 
		{	0.0f, 1.0f, 0.0f, 0.0f, 1.0f,  0.0f,
			1.0f, 0.0f, 1.0f, 1.0f, 0.0f,  1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  1.0f,
			1.0f, 0.0f, 1.0f, 1.0f, 0.0f,  1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  1.0f,
			1.0f, 0.0f, 1.0f, 1.0f, 0.0f,  1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 0.0f,  1.0f,
			1.0f, 0.0f, 1.0f, 1.0f, 0.0f,  1.0f,
			0.0f, 0.0f, 1.0f, 0.0f, 1.0f,  1.0f,
			1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  0.0f,
			0.0f, 1.0f, 0.0f, 0.0f, 1.0f,  1.0f,
			1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  0.0f
			
		};
	
		// pyramid definition
		numPyramidVertices = pyramid.getNumVertices();
		vertices = pyramid.getVertices();
		normals = pyramid.getNormals();
		texCoords = pyramid.getTexCoords();
		
		float[] pyramidPvalues = new float[numPyramidVertices*3];
		float[] pyramidNvalues = new float[numPyramidVertices*3];
		float[] pyramidTvalues = new float[numPyramidVertices*2];
		
		for (int i=0; i<numPyramidVertices; i++)
		{	pyramidPvalues[i*3]   = (float) (vertices[i]).x();
			pyramidPvalues[i*3+1] = (float) (vertices[i]).y();
			pyramidPvalues[i*3+2] = (float) (vertices[i]).z();
			pyramidNvalues[i*3]   = (float) (normals[i]).x();
			pyramidNvalues[i*3+1] = (float) (normals[i]).y();
			pyramidNvalues[i*3+2] = (float) (normals[i]).z();
			pyramidTvalues[i*2]   = (float) (texCoords[i]).x();
			pyramidTvalues[i*2+1] = (float) (texCoords[i]).y();
		}
		// axis lines
		float[] xLine = {
			0.0f, 0.0f, 0.0f, 
			100.0f, 0.0f, 0.0f, 
			0.0f, 0.05f, 0.0f	
		};
		
		float[] xTextures =	{
			0.0f, 0.0f, 1.0f, 
			0.0f, 1.0f, 1.0f	
		};
		
		float[] yLine = {
			0.0f, 0.0f, 0.0f, 
			0.05f, 0.0f, 0.0f, 
			0.0f, 100.0f, 0.0f
		};
		
		float[] yTextures = {
			0.0f, 0.0f, 1.0f, 
			0.0f, 1.0f, 1.0f	
		};
		
		float[] zLine = {
			0.0f, 0.0f, 0.0f, 
			0.0f, 0.0f, 100.0f, 
			0.0f, -0.05f, 0.0f
		};
		
		float[] zTextures =	{
			0.0f, 0.0f, 1.0f, 
			0.0f, 1.0f, 1.0f	
		};

		// torus definition
		myTorus = new Torus(0.5f, 0.2f, 36);
		numTorusVertices = myTorus.getNumVertices();
		numTorusIndices = myTorus.getNumIndices();
		vertices = myTorus.getVertices();
		normals = myTorus.getNormals();
		texCoords = myTorus.getTexCoords();
		indices = myTorus.getIndices();
		
		float[] torusPvalues = new float[vertices.length*3];
		float[] torusNvalues = new float[normals.length*3];
		float[] torusTvalues = new float[vertices.length*2];

		for (int i=0; i<numTorusVertices; i++)
		{	torusPvalues[i*3]   = (float) vertices[i].x();
			torusPvalues[i*3+1] = (float) vertices[i].y();
			torusPvalues[i*3+2] = (float) vertices[i].z();
			torusNvalues[i*3]   = (float) normals[i].x();
			torusNvalues[i*3+1] = (float) normals[i].y();
			torusNvalues[i*3+2] = (float) normals[i].z();
			torusTvalues[i*2]   = (float) (texCoords[i]).x();
			torusTvalues[i*2+1] = (float) (texCoords[i]).y();
		}

		// buffers definition
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(vbo.length, vbo, 0);

		//  put the Torus vertices into the first buffer,
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(torusPvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		//  load the pyramid vertices into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer pyrVertBuf = Buffers.newDirectFloatBuffer(pyramidPvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrVertBuf.limit()*4, pyrVertBuf, GL_STATIC_DRAW);
		
		// load the torus normal coordinates into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer torusNorBuf = Buffers.newDirectFloatBuffer(torusNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torusNorBuf.limit()*4, torusNorBuf, GL_STATIC_DRAW);
		
		//load the sphere vertices into the fourth buffer
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[4]);
		FloatBuffer sphereVertBuf = Buffers.newDirectFloatBuffer(spherePvalues);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, sphereVertBuf.limit()*4, sphereVertBuf, GL_STATIC_DRAW);
		
		// load the pyramid normal coordinates into the fourth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer pyrNorBuf = Buffers.newDirectFloatBuffer(pyramidNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrNorBuf.limit()*4, pyrNorBuf, GL_STATIC_DRAW);
		
		// load the sphere normal coordinates into the seventeenth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[17]);
		FloatBuffer sphereNorBuf = Buffers.newDirectFloatBuffer(sphereNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereNorBuf.limit()*4, sphereNorBuf, GL_STATIC_DRAW);
		
		// load the axis lines coordinates into the sixth seventh and eigth buffer
		//axis lines
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer xLineBuf = Buffers.newDirectFloatBuffer(xLine);
        gl.glBufferData(GL_ARRAY_BUFFER, xLineBuf.limit()*4,xLineBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer yLineBuf = Buffers.newDirectFloatBuffer(yLine);
        gl.glBufferData(GL_ARRAY_BUFFER, yLineBuf.limit()*4,yLineBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer zLineBuf = Buffers.newDirectFloatBuffer(zLine);
        gl.glBufferData(GL_ARRAY_BUFFER, zLineBuf.limit()*4,zLineBuf, GL_STATIC_DRAW);
		//load the axis lines textures into the ninth tenth and eleventh buffer
		
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer xtexBuf = Buffers.newDirectFloatBuffer(xTextures);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, xtexBuf.limit()*4, xtexBuf, gl.GL_STATIC_DRAW);
		
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer ytexBuf = Buffers.newDirectFloatBuffer(yTextures);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, ytexBuf.limit()*4, ytexBuf, gl.GL_STATIC_DRAW);
		
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer ztexBuf = Buffers.newDirectFloatBuffer(zTextures);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, ztexBuf.limit()*4, ztexBuf, gl.GL_STATIC_DRAW);
		//  load the pyramid textures into the twelvth buffer
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer pyrTexBuf = Buffers.newDirectFloatBuffer(pyramidTvalues);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, pyrTexBuf.limit()*4, pyrTexBuf, gl.GL_STATIC_DRAW);
		//  load the torus textures into the thirteenth buffer
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer torTexBuf = Buffers.newDirectFloatBuffer(torusTvalues);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, torTexBuf.limit()*4, torTexBuf, gl.GL_STATIC_DRAW);
		// load the sky box cube into the fourteetnth
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer cvertBuf = Buffers.newDirectFloatBuffer(cubeVertexPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cvertBuf.limit()*4, cvertBuf, GL_STATIC_DRAW);
		
		//  load the sphere textures into the nineteenth buffer
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo[19]);
		FloatBuffer sphereTexBuf = Buffers.newDirectFloatBuffer(sphereTvalues);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, sphereTexBuf.limit()*4, sphereTexBuf, gl.GL_STATIC_DRAW);
		
		// indices for torus
		
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[21]);
		IntBuffer idxBuf = Buffers.newDirectIntBuffer(indices);
		gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuf.limit()*4, idxBuf, GL_STATIC_DRAW);
		
		//grid stuff 
		//  ground vertices
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer groundVertBuf = Buffers.newDirectFloatBuffer(gridPvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, groundVertBuf.limit()*4, groundVertBuf, GL_STATIC_DRAW);
		
		//  ground texture coordinates
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[15]);
		FloatBuffer groundTexBuf = Buffers.newDirectFloatBuffer(gridTvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, groundTexBuf.limit()*4, groundTexBuf, GL_STATIC_DRAW);

		// ground normals
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[16]);
		FloatBuffer groundNorBuf = Buffers.newDirectFloatBuffer(gridNvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, groundNorBuf.limit()*4, groundNorBuf, GL_STATIC_DRAW);
		
		//sphere tan 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[20]);
		FloatBuffer sphereTanBuf = Buffers.newDirectFloatBuffer(sphereTanvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sphereTanBuf.limit()*4, sphereTanBuf, GL_STATIC_DRAW);
		
		//cube stuff
		
		//cube texture		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[18]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(cubeTextureCoordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		
	}
	// 3D Texture section
	
	private void fillDataArray(byte data[])
	{ double xyPeriod = 30.0;
	  double turbPower = 0.15;
	  double turbSize =  40.0;
	  
	  for (int i=0; i<noiseWidth; i++)
	  { for (int j=0; j<noiseHeight; j++)
	    { for (int k=0; k<noiseDepth; k++)
	      {	double xValue = (i - (double)noiseWidth/2.0) / (double)noiseWidth;
		double yValue = (j - (double)noiseHeight/2.0) / (double)noiseHeight;
		double distValue = Math.sqrt(xValue * xValue + yValue * yValue)
						+ turbPower * turbulence(i, j, k, turbSize) / 256.0;
		double sineValue = 128.0 * Math.abs(Math.sin(2.0 * xyPeriod * distValue * Math.PI));

		Color c = new Color((int)(60+(int)sineValue), (int)(10+(int)sineValue), 0);

	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte) c.getRed();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte) c.getGreen();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte) c.getBlue();
	        data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte) 255;
	} } } }

	private int buildNoiseTexture()
	{	gl = (GL4) GLContext.getCurrentGL();

		byte[] data = new byte[noiseHeight*noiseWidth*noiseDepth*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				noiseWidth, noiseHeight, noiseDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

	void generateNoise()
	{	for (int x=0; x<noiseHeight; x++)
		{	for (int y=0; y<noiseWidth; y++)
			{	for (int z=0; z<noiseDepth; z++)
				{	noise[x][y][z] = random.nextDouble();
	}	}	}	}
	
	double smoothNoise(double x1, double y1, double z1)
	{	//get fractional part of x, y, and z
		double fractX = x1 - (int) x1;
		double fractY = y1 - (int) y1;
		double fractZ = z1 - (int) z1;

		//neighbor values
		int x2 = ((int)x1 + noiseWidth + 1) % noiseWidth;
		int y2 = ((int)y1 + noiseHeight+ 1) % noiseHeight;
		int z2 = ((int)z1 + noiseDepth + 1) % noiseDepth;

		//smooth the noise by interpolating
		double value = 0.0;
		value += (1-fractX) * (1-fractY) * (1-fractZ) * noise[(int)x1][(int)y1][(int)z1];
		value += (1-fractX) * fractY     * (1-fractZ) * noise[(int)x1][(int)y2][(int)z1];
		value += fractX     * (1-fractY) * (1-fractZ) * noise[(int)x2][(int)y1][(int)z1];
		value += fractX     * fractY     * (1-fractZ) * noise[(int)x2][(int)y2][(int)z1];

		value += (1-fractX) * (1-fractY) * fractZ     * noise[(int)x1][(int)y1][(int)z2];
		value += (1-fractX) * fractY     * fractZ     * noise[(int)x1][(int)y2][(int)z2];
		value += fractX     * (1-fractY) * fractZ     * noise[(int)x2][(int)y1][(int)z2];
		value += fractX     * fractY     * fractZ     * noise[(int)x2][(int)y2][(int)z2];
		
		return value;
	}

	private double turbulence(double x, double y, double z, double size)
	{	double value = 0.0, initialSize = size;
		while(size >= 0.9)
		{	value = value + smoothNoise(x/size, y/size, z/size) * size;
			size = size / 2.0;
		}
		value = 128.0 * value / initialSize;
		return value;
	}
	private double logistic(double x)
	{	double k = 3.0;
		return (1.0/(1.0+Math.pow(2.718,-k*x)));
	}
	
	private void installLights(int renderingProgram, Matrix4f vMatrix)
	{	gl = (GL4) GLContext.getCurrentGL();
	
		currentLightPos.mulPosition(vMatrix);
		lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();
		
		// set current material values
		matAmb = thisAmb;
		matDif = thisDif;
		matSpe = thisSpe;
		matShi = thisShi;
		
		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
		mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
	}
	//space check
	public void turnOnTess(){
		spaceCheck = (spaceCheck) ? false : true;
		if (colorFlag == 1){
			colorFlag = 0;
		}else{
			colorFlag = 1;
		}
		
	}
	//t check
	public void turnOnGeo(){
		tCheck = (tCheck) ? false : true;
		
	}
	// change color of light source
	//color change function
	public void changeColor()
	{
		int offsetLoc = gl.glGetUniformLocation(renderingProgram, "colorFlag");
		gl.glProgramUniform1i(renderingProgram, offsetLoc, colorFlag);
	}
	//light check
	public void lightsOn(){
		lightCheck = (lightCheck) ? false : true;
	}
	// move lights
	@Override
    public void mouseDragged(MouseEvent e) {

        if (e.getX() > this.getWidth() / 2 && e.getX() < this.getWidth())
            lightLoc.set(this.getWidth() / 2 + (e.getX() - this.getWidth()), lightLoc.y(), lightLoc.z());
        else if (e.getX() > this.getWidth()) lightLoc.set(this.getWidth(), lightLoc.y(), lightLoc.z());
        else if (e.getX() < this.getWidth() / 2) lightLoc.set(e.getX() - this.getWidth() / 2, lightLoc.y(), lightLoc.z());
        else lightLoc.set(0, lightLoc.y(), lightLoc.z());

        if (e.getY() > this.getHeight() / 2 && e.getY() < this.getHeight())
            lightLoc.set(lightLoc.x(), -(this.getHeight() / 2 + (e.getY() - this.getHeight())), lightLoc.z());
        else if (e.getY() > this.getHeight()) lightLoc.set(lightLoc.x(), this.getHeight(), lightLoc.z());
        else if (e.getY() < this.getHeight() / 2) lightLoc.set(lightLoc.x(), -(e.getY() - this.getHeight() / 2), lightLoc.z());
        else lightLoc.set(lightLoc.x(), 0, lightLoc.z());


        myCanvas.display();
    }
	public void goLeft(){
	}
	public void goDown(){
	}
	public void goUp(){
	}
	public void goRight(){
	}
	//overrides
	@Override
    public void mouseExited(MouseEvent e) {

    }
	@Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }
	@Override
    public void mouseMoved(MouseEvent e) {

    }
	@Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    }


	public static void main(String[] args) { new Starter(); }
	public void dispose(GLAutoDrawable drawable) {}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	gl = (GL4) GLContext.getCurrentGL();

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupShadowBuffers();
	}
}