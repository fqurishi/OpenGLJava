package commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import main.Starter;

@SuppressWarnings("serial")
public class LightCommand extends AbstractAction{
	
	private Starter s;
	
	public LightCommand (Starter s) {
		super("lights");
		this.s = s;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		s.lightsOn();
	}
}