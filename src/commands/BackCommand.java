package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BackCommand extends AbstractAction {
	
	private static BackCommand instance = new BackCommand();
	private Camera c;
	
	public BackCommand(){
		super("Back");
	}
	
	public static BackCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.moveCameraZBack(3.5f);
	}
}