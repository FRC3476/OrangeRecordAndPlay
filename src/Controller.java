package org.usfirst.frc.team3476.robot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;

/**
 * This class stores the int sent back from the Driver Station and uses it to check for rising or falling edges
 */
public class Controller extends Joystick {

	public static class Xbox {
		public static int A = 1;
		public static int B = 2;
		public static int X = 3;
		public static int Y = 4;
		public static int LeftBumper = 5;
		public static int RightBumper = 6;
		public static int Back = 7;
		public static int Start = 8;
		public static int LeftClick = 9;
		public static int RightClick = 10;

		public static int LeftX = 0;
		public static int LeftY = 1;
		public static int LeftTrigger = 2;
		public static int RightTrigger = 3;
		public static int RightX = 4;
		public static int RightY = 5;
	}

	/*
	 * The Driver Station sends back an int(32 bits) for buttons
	 * Shifting 1 left (button - 1) times and ANDing it with
	 * int sent from the Driver Station will either give you
	 * 0 or a number not zero if it is true
	 */
	private int oldButtons;
	private int currentButtons;
	private int axisCount, povCount;
	private double[] oldAxis;
	private double[] currentAxis;
	private int[] oldPOV;
	private int[] currentPOV;
	private boolean record, play;
	private InputRecorder recorder;
	private InputPlayer player;

	public Controller(int port) {
		super(port);
		axisCount = DriverStation.getInstance().getStickAxisCount(port);
		povCount = DriverStation.getInstance().getStickPOVCount(port);
		oldAxis = new double[axisCount];
		currentAxis = new double[axisCount];
		oldPOV = new int[povCount];
		currentPOV = new int[povCount];
		record = false;
		play = false;
		recorder = null;
		player = null;
	}

	/**
	 * Only works if update() is called in each iteration
	 *
	 * @param button
	 *            Joystick button ID
	 * @return
	 * 		Falling edge state of the button
	 */
	public boolean getFallingEdge(int button) {
		boolean oldVal = ((0x1 << (button - 1)) & oldButtons) != 0;
		boolean currentVal = ((0x1 << (button - 1)) & currentButtons) != 0;
		if (oldVal == true && currentVal == false) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Only works if update() is called in each iteration
	 *
	 * @param button
	 *            Joystick button ID
	 * @return
	 * 		Rising edge state of the button
	 */
	public boolean getRisingEdge(int button) {
		boolean oldVal = ((0x1 << (button - 1)) & oldButtons) != 0;
		boolean currentVal = ((0x1 << (button - 1)) & currentButtons) != 0;
		if (oldVal == false && currentVal == true) {
			return true;
		} else {
			return false;
		}
	}

	public boolean getRisingEdge(int axis, double threshold) {
		if (axis <= axisCount) {
			boolean oldVal = oldAxis[axis] > threshold;
			boolean currentVal = currentAxis[axis] > threshold;
			if (oldVal == false && currentVal == true) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean getFallingEdge(int axis, double threshold) {
		if (axis <= axisCount) {
			boolean oldVal = oldAxis[axis] > threshold;
			boolean currentVal = currentAxis[axis] > threshold;
			if (oldVal == true && currentVal == false) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * This method needs to be called for each iteration of the teleop loop
	 */
	public void update() {
		if(play) {
			if(player.update() != 0) {
				stopPlay();
				return;
			}
			currentButtons = player.getButtons();
			currentAxis = player.getAxis();
			currentPOV = player.getPOV();			
		} else {
			oldButtons = currentButtons;
			currentButtons = DriverStation.getInstance().getStickButtons(getPort());
			if(axisCount != DriverStation.getInstance().getStickAxisCount(getPort())){
				axisCount = DriverStation.getInstance().getStickAxisCount(getPort());
				oldAxis = new double[axisCount];
				currentAxis = new double[axisCount];
			}
			if(povCount != DriverStation.getInstance().getStickPOVCount(getPort())){
				povCount = DriverStation.getInstance().getStickPOVCount(getPort());
				oldPOV = new int[povCount];
				currentPOV = new int[povCount];
			}
			oldAxis = currentAxis;
			for (int i = 0; i < axisCount; i++) {
				currentAxis[i] = super.getRawAxis(i);
			}
			
			oldPOV = currentPOV;
			for (int i = 0; i < povCount; i++) {
				currentPOV[i] = super.getPOV(i);
			}
			if(record) {
				recorder.record(currentButtons, currentAxis, currentPOV);
			}
		}
	}
	
	public void setRecord(String filename) {
		record = true;
		recorder = new InputRecorder(filename);
	}
	
	public void stopRecord() {
		record = false;		
		recorder.stop();
	}
	
	public void setPlay(String filename) {
		play = true;
		player = new InputPlayer(filename);
	}
	
	public void stopPlay() {
		play = false;
	}
	
	public static class InputPlayer {
		
		private Scanner scanner;
		private int currentButtons;
		private double[] currentAxis;
		private int[] currentPOV;
		
		public InputPlayer(String filename) {
			try {
				scanner = new Scanner(new File("/home/lvuser/" + filename + ".csv"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			scanner.useDelimiter(",|\\n");			
		}
		
		public int update() {
			if(scanner.hasNext()) {					
				currentButtons = scanner.nextInt();
				int axisLength = scanner.nextInt();
				for(int i = 0; i < axisLength; i++) {
					currentAxis[i] = scanner.nextDouble();
				}
				int povLength = scanner.nextInt();
				for(int i = 0; i < povLength; i++) {
					currentPOV[i] = scanner.nextInt();
				}
			} else {
				return -1;
			}
			return 0;
		}
		
		public int getButtons() {
			return currentButtons;
		}
		
		public double[] getAxis() {
			return currentAxis;
		}
		
		public int[] getPOV() {
			return currentPOV;
		}
		
	}
	
	public static class InputRecorder {

		private FileWriter writer;
		
		public InputRecorder(String filename) {
			try {
				writer = new FileWriter("/home/lvuser/" + filename + ".csv");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void record(int currentButtons, double[] currentAxis, int[] currentPOV) {
			try {
				String buttons = String.valueOf(currentButtons);
				writer.append(buttons);
				writer.append("," + String.valueOf(currentAxis.length));
				for(int i = 0; i < currentAxis.length; i++) {
					writer.append("," + String.valueOf(currentAxis[i]));
				}
				writer.append("," + String.valueOf(currentPOV.length));
				for(int i = 0; i < currentPOV.length; i++) {
						writer.append("," + String.valueOf(currentPOV[i]));				
				}
				writer.append("\n");
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void stop() {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
