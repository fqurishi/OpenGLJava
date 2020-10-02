package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PanRightCommand extends AbstractAction {
	
	private static PanRightCommand instance = new PanRightCommand();
	private Camera c;
	
	public PanRightCommand(){
		super("Pan Right");
	}
	
	public static PanRightCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.panR(5f);
	}
}