package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

import org.firstinspires.ftc.teamcode.Commands.DriveCommand;
import org.firstinspires.ftc.teamcode.Subsytems.DriveSubsystem;

public class RobotContainer {
    public final DriveSubsystem drive;

    public RobotContainer(HardwareMap hardwareMap, GamepadEx driver, GamepadEx operator){
        drive = new DriveSubsystem(hardwareMap,
                "LeftFront",
                "RightFront",
                "LeftBack",
                "RightBack");

        drive.setDefaultCommand(
                new DriveCommand(drive, driver)
        );


    }
}
