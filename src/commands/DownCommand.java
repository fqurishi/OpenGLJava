package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DownCommand extends AbstractAction {
	
	private static DownCommand instance = new DownCommand();
	private Camera c;
	
	public DownCommand(){
		super("Down");
	}
	
	public static DownCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.moveCameraYDown(3.5f);
	}
}