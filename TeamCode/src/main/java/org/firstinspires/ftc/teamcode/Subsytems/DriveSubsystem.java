package org.firstinspires.ftc.teamcode.Subsytems;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.gamepad1;
import static org.firstinspires.ftc.teamcode.Constants.TeleopConstants.MotorMaxSpeed;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;
import com.seattlesolvers.solverslib.command.SubsystemBase;
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

import org.firstinspires.ftc.teamcode.Constants.TeleopConstants;

public class DriveSubsystem extends SubsystemBase {

    private final DcMotorEx leftFront;
    private final DcMotorEx rightFront;
    private final DcMotorEx leftBack;
    private final DcMotorEx rightBack;

    double leftFrontP, leftBackP, rightFrontP, rightBackP;

    public DriveSubsystem (final HardwareMap hMap, String lf,String rf, String lb, String rb){
        leftFront = hMap.get(DcMotorEx.class, lf);
        rightFront = hMap.get(DcMotorEx.class, rf);
        leftBack = hMap.get(DcMotorEx.class, lb);
        rightBack = hMap.get(DcMotorEx.class, rb);

    }

    public void NmDriveCode (double lx, double ly, double rx, double ry) {

            leftFrontP = -Range.clip(ly - lx, -MotorMaxSpeed, MotorMaxSpeed);
            leftBackP = Range.clip(ly + rx, -MotorMaxSpeed, MotorMaxSpeed);
            rightBackP = -Range.clip(ry - lx, -MotorMaxSpeed, MotorMaxSpeed);
            rightFrontP = Range.clip(ry + rx, -MotorMaxSpeed, MotorMaxSpeed);

        if (gamepad1.left_bumper) {
            leftFront.setPower(leftFrontP * 0.5);
            leftBack.setPower(leftBackP * 0.5);
            rightFront.setPower(rightFrontP * 0.5);
            rightBack.setPower(rightBackP * 0.5);
        } else {
            leftFront.setPower(leftFrontP);
            leftBack.setPower(leftBackP);
            rightFront.setPower(rightFrontP);
            rightBack.setPower(rightBackP);
        }
    }
    public void StrafeLeft (double trigger) {
        leftFrontP = -(MotorMaxSpeed * trigger);
        rightFrontP = (MotorMaxSpeed * trigger);
        leftBackP = (MotorMaxSpeed * trigger);
        rightBackP = -(MotorMaxSpeed * trigger);
    }
    public void StrafeRight (double trigger) {
        leftFrontP = (MotorMaxSpeed * trigger);
        rightFrontP = -(MotorMaxSpeed * trigger);
        leftBackP = -(MotorMaxSpeed * trigger);
        rightBackP = (MotorMaxSpeed * trigger);
    }


}
