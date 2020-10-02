package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RightCommand extends AbstractAction {
	
	private static RightCommand instance = new RightCommand();
	private Camera c;
	
	public RightCommand(){
		super("Right");
	}
	
	public static RightCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.moveCameraXRight(3.5f);
	}
}