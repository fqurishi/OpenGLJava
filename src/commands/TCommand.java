package commands;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import main.Starter;

@SuppressWarnings("serial")
public class TCommand extends AbstractAction{
	
	private Starter s;
	
	public TCommand (Starter s) {
		super("T");
		this.s = s;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		s.turnOnGeo();
	}
}