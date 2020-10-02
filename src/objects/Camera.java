package objects;

import java.nio.*;
import javax.swing.*;
import java.lang.Math;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import org.joml.*;


public class Camera {
	private Vector4f u, v, n, c, location;
	private Matrix4f m;
	double pitch = 0;
	double pan = 0;
	
	public Camera(float x, float y, float z){
		init();
		location = new Vector4f(x, y, z, 1);
		updateView();
	}
	private void init(){
		u = new Vector4f(1, 0, 0, 0);
		v = new Vector4f(0, 1, 0, 0);
		n = new Vector4f(0, 0, 1, 0);
		m = new Matrix4f(u, v, n, new Vector4f(0, 0, 0, 1));
	}
	
	public void moveCameraXLeft(float x){
		c = this.u;
		Vector4f compute = c.normalize().mul(x);
		location = location.sub(compute);
		updateView();
		
	}
	public void moveCameraXRight(float x){
		c = this.u;
		Vector4f compute = c.normalize().mul(x);
		location = location.add(compute);
		updateView();
		
	}
	public void moveCameraYUp(float y){
		c = this.v;
		Vector4f compute = c.normalize().mul(y);
		location = location.add(compute);
		updateView();
		
	}
	public void moveCameraYDown(float y){
		c = this.v;
		Vector4f compute = c.normalize().mul(y);
		location = location.sub(compute);
		updateView();
		
	}
	public void moveCameraZForward(float z){
		c = this.n;
		Vector4f compute = c.normalize().mul(z);
		location = location.add(compute);
		updateView();
		
	}
	public void moveCameraZBack(float z){
		c = this.n;
		Vector4f compute = c.normalize().mul(z);
		location = location.sub(compute);
		updateView();
		
	}
	public void panR(float a){
		this.pan = pan - a;
		updateView();
	}
	public void panL(float a){
		this.pan = pan + a;
		updateView();
	}
	public void pitchU(float a) {
        this.pitch = pitch + a;
		updateView();
    }
	public void pitchD(float a) {
        this.pitch = pitch - a;
		updateView();
    }
	public float getX(){
		return location.x();
	}
	public float getY(){
		return location.y();
	}
	public float getZ(){
		return location.z();
	}
	public Matrix4f getMatrix(){
		return m;
	}
    public void updateView() {
        
        Vector4f newLocation = new Vector4f(location);
        float cosPitch = (float)Math.cos(Math.toRadians(pitch));
        float sinPitch = (float)Math.sin(Math.toRadians(pitch));
        float cosPan = (float)Math.cos(Math.toRadians(pan));
        float sinPan = (float)Math.sin(Math.toRadians(pan));
        
        u = new Vector4f(cosPan, 0, -sinPan, 0);
        v = new Vector4f(sinPan * sinPitch, cosPitch, cosPan * sinPitch, 0);
        n = new Vector4f(sinPan * cosPitch, -sinPitch, cosPitch * cosPan, 0);
        float[] matArray = new float[] {
                u.x(), v.x(), n.x(), 0,
                u.y(), v.y(), n.y(), 0, 
                u.z(), v.z(), n.z(), 0, 
                -(u.dot(newLocation)), -(v.dot(newLocation)), -(n.dot(newLocation)), 1};
        
        m.set(matArray);
    } 
	
	
}