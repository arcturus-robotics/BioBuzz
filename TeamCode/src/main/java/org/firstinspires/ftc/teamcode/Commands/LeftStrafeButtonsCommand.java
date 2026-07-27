package org.firstinspires.ftc.teamcode.Commands;

import com.seattlesolvers.solverslib.command.CommandBase;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.teamcode.Subsytems.DriveSubsystem;

public class LeftStrafeButtonsCommand extends CommandBase {
    private final DriveSubsystem drive;
    private final GamepadEx driver;

    public LeftStrafeButtonsCommand(DriveSubsystem drive, GamepadEx driver){
        this.drive = drive;
        this.driver = driver;
    addRequirements(drive);

    }
    @Override
    public void initialize() {

        drive.StrafeLeft(driver.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER));

    }

}
