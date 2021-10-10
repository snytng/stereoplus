package snytng.astah.plugin.stereoplus;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;

class StereotypeView {

	Map<String, JButton> buttonsMap = new HashMap<>();
	
	public StereotypeView(String name, String colorString, String[] names, String[] colors){
		this(name, Color.decode(colorString), names, colors);
	}
	
	public StereotypeView(String name, Color color, String[] names, String[] colors){
		this.name = name;
		this.color = color;
		
		this.names = names;
		this.colors = colors;

		buttonTop = new JButton("<" + name + ">");
		buttons = new JButton[names.length];
		buttons[0] = new JButton("-----");
		for(int i = 1; i < buttons.length; i++){
			buttons[i] = new JButton(names[i]);
			buttonsMap.put(names[i], buttons[i]);
		}
	}

	private String name = null;
	public String getName(){
		return this.name;
	}

	private Color color = null;
	public Color getColor(){
		return this.color;
	}

	private JButton buttonTop = null;
	public JButton getButtonTop(){
		return this.buttonTop;
	}
	
	private JButton[] buttons = null;

	public JButton[] getButtons(){
		return this.buttons;
	}

	private String[] names = null;
	public String[] getNames(){
		return this.names;
	}

	private String[] colors = null;
	public String[] getColors(){
		return this.colors;
	}

	public Box getBox(){
		Box box = Box.createHorizontalBox();
		box.add(buttonTop);
		for(int i = 0; i < buttons.length; i++){
			box.add(buttons[i]);
		}
		return box;
	}

}
