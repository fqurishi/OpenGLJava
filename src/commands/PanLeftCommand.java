package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PanLeftCommand extends AbstractAction {
	
	private static PanLeftCommand instance = new PanLeftCommand();
	private Camera c;
	
	public PanLeftCommand(){
		super("Pan Left");
	}
	
	public static PanLeftCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.panL(5f);
	}
}