package commands;
import objects.Camera;


import javax.swing.*;
import java.awt.event.ActionEvent;

public class UpCommand extends AbstractAction {
	
	private static UpCommand instance = new UpCommand();
	private Camera c;
	
	public UpCommand(){
		super("Up");
	}
	
	public static UpCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.moveCameraYUp(3.5f);
	}
}