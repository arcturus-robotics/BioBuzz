package org.firstinspires.ftc.teamcode.Commands;

import com.bylazar.gamepad.Gamepad;
import com.seattlesolvers.solverslib.command.CommandBase;
import com.seattlesolvers.solverslib.command.Subsystem;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

import org.firstinspires.ftc.teamcode.Subsytems.DriveSubsystem;



public class DriveCommand extends CommandBase {
    private final DriveSubsystem drive;
    private final GamepadEx gamepad;

    public DriveCommand(DriveSubsystem drive, GamepadEx gamepad) {
        this.drive = drive;
        this.gamepad = gamepad;

        addRequirements(drive);

    }

    @Override
    public void execute() {

        drive.NmDriveCode(
                gamepad.getLeftX(),
                gamepad.getLeftX(),
                gamepad.getRightX(),
                gamepad.getRightY()
        );

    }

}
