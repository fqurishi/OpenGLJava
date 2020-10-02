package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ForwardCommand extends AbstractAction {
	
	private static ForwardCommand instance = new ForwardCommand();
	private Camera c;
	
	public ForwardCommand(){
		super("Forward");
	}
	
	public static ForwardCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.moveCameraZForward(3.5f);
	}
}