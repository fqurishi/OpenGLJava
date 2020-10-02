package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LeftCommand extends AbstractAction {
	
	private static LeftCommand instance = new LeftCommand();
	private Camera c;
	
	public LeftCommand(){
		super("Left");
	}
	
	public static LeftCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.moveCameraXLeft(3.5f);
	}
}