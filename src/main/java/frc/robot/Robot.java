/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
/*
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
*/
  WPI_TalonSRX frontLeftDrive;
  WPI_TalonSRX frontRightDrive;
  WPI_TalonSRX backLeftDrive;
  WPI_TalonSRX backRightDrive;
  WPI_TalonSRX bottomPitchClawControl;

  double RightStick_YAxis; //Throttle on Right Stick
  double LeftStick_XAxis; //Turning on Left Stick

  SpeedControllerGroup leftDrive;
  SpeedControllerGroup rightDrive;
  DifferentialDrive driveTrain;
  XboxController joystick;

  DoubleSolenoid claw;

  Timer clawTime;

  boolean orientation;
  boolean clawIsOpen;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    /*
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    */
    //create left speed drivers
    frontLeftDrive = new WPI_TalonSRX(4); //sticker says LF and is pink
    backLeftDrive = new WPI_TalonSRX(5); //sticker says LB and is orange
    leftDrive = new SpeedControllerGroup(frontLeftDrive, backLeftDrive);
    
    //create right speed drivers
    frontRightDrive = new WPI_TalonSRX(8); //sticker says RF and is yellow
    backRightDrive = new WPI_TalonSRX(9); //sticker says RB and is pink
    rightDrive = new SpeedControllerGroup(frontRightDrive, backRightDrive);

    //the whole boi in one
    driveTrain = new DifferentialDrive(leftDrive, rightDrive);

    //controller, first one plugged in
    joystick = new XboxController(0);

    //make the claw control exist
    bottomPitchClawControl = new WPI_TalonSRX(2);

    //true = up
    orientation = true;
    clawIsOpen = true;

    //claw timer, AKA claw time
    clawTime = new Timer();

    RightStick_YAxis = 0;
    LeftStick_XAxis = 0;

    //the claw gripper
    claw = new DoubleSolenoid(0, 7);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    /*
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    */
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    /*
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
    */
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {

    RightStick_YAxis = (-joystick.getY(Hand.kRight)); //left stick's Y Axis
    LeftStick_XAxis = (joystick.getX(Hand.kLeft)); //Right stick's X axis

    //square function for inputs on Y Axis
    if (RightStick_YAxis < 0){
      RightStick_YAxis = -(RightStick_YAxis * RightStick_YAxis);
    }else{
      RightStick_YAxis = (RightStick_YAxis * RightStick_YAxis);
    }

    //square function for inputs on X Axis
    if (LeftStick_XAxis < 0){
      LeftStick_XAxis = -(LeftStick_XAxis * LeftStick_XAxis);
    }else{
      LeftStick_XAxis = (LeftStick_XAxis * LeftStick_XAxis);
    }

    //stop the claw pitch timeout counter
    bottomPitchClawControl.feed();

    //check for x button press
    if (joystick.getXButtonPressed()){
      clawTime.start();//start the timer
      if(orientation){//if up top, then go down
        bottomPitchClawControl.set(-0.15);
        //System.out.println(clawTime.get() + " is starting from up");
        orientation = false;//now assumed to be down
      }else{
        bottomPitchClawControl.set(0.15);//go up if down
        //System.out.println(clawTime.get() + " is starting from down");
        orientation = true;//now assumed to be up
      }
    }

    if (joystick.getBButtonPressed()){
      claw.set(Value.kOff);
    }

    if(joystick.getAButtonPressed()){
      if (clawIsOpen){
        claw.set(Value.kForward);
        clawIsOpen = false;
      }else{
        claw.set(Value.kReverse);
        clawIsOpen = true;
      }
    }

    if(clawTime.get() > 5){//if 5 seconds have passed, then:
      bottomPitchClawControl.set(0);//stop the claw's pitch
      //System.out.println(clawTime.get() + " is toggled");
      clawTime.stop();//stop the counter
      clawTime.reset();//set counter to zero
    }

    driveTrain.arcadeDrive(RightStick_YAxis, LeftStick_XAxis);
    //stop the motor timeout timer
    driveTrain.feed();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
