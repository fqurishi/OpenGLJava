package commands;
import objects.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PitchUpCommand extends AbstractAction {
	
	private static PitchUpCommand instance = new PitchUpCommand();
	private Camera c;
	
	public PitchUpCommand(){
		super("Pitch Up");
	}
	
	public static PitchUpCommand getInstance(){
		return instance;
	}
	
	public void setCamera(Camera c){
		this.c = c;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		c.pitchU(5f);
	}
}