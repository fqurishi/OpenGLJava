package commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import main.Starter;

@SuppressWarnings("serial")
public class SpaceCommand extends AbstractAction{
	
	private Starter s;
	
	public SpaceCommand (Starter s) {
		super("space");
		this.s = s;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		s.turnOnTess();
	}
}