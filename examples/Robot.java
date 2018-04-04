/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team3476.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	
	/*
	 * Instead of Joystick create a Controller
	 */
	Controller xbox = new Controller(1);

	@Override
	public void robotInit() {
	}

	@Override
	public void autonomousInit() {		
		/*
		 * Playback a previously saved auto file
		 */
		xbox.setPlay("autofile1");
	}

	@Override
	public void autonomousPeriodic() {
		/*
		 * Call update to get new values from file
		 */
		xbox.update();
		
		/*
		 * Paste teleop code here
		 * For example this prints out when the button A on the xbox is pressed 
		 */
		if(xbox.getRawButton(1)) {
			System.out.println("Button 1 played back");
		}
	}
	
	@Override
	public void teleopInit() {
		/*
		 * It stops playing back when it goes to the end of the file but just in case it doesn't stop emulating
		 */
		xbox.stopPlay();
	}
	
	@Override
	public void teleopPeriodic() {
		/*
		 * Update values of controller. This also allows certain functions such as getting a rising edge or falling edge
		 */
		xbox.update();
		/*
		 * Call setRecord to start recording
		 * This example uses the button A on the xbox
		 */
		if(xbox.getRisingEdge(Controller.Xbox.A)) {
			xbox.setRecord("some auto");
		}
		/*
		 * Call stopRecord to stop recording
		 * This example uses the button B on the xbox
		 */
		if(xbox.getRisingEdge(Controller.Xbox.B)) {
			xbox.stopRecord();
		}
		
		/*
		 * Normal teleop code here
		 * This example just prints something out when the button id 1 is pressed
		 */
		if(xbox.getRawButton(1)) {
			System.out.println("Button 1 pressed in teleop");
		}
		
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
