package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PitchDownCommand extends AbstractAction {
	
	private static PitchDownCommand instance = new PitchDownCommand();
	private Camera c;
	
	public PitchDownCommand(){
		super("Pitch Down");
	}
	
	public static PitchDownCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.pitchD(5f);
	}
}