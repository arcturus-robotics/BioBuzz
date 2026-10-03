package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;

import java.util.List;

import org.firstinspires.ftc.teamcode.robot.Robot;
import com.qualcomm.robotcore.hardware.Gamepad;

@Config

public class Drivetrain{
    private final DcMotor frontLeft;
    private final DcMotor frontRight;
    private final DcMotor backLeft;
    private final DcMotor backRight;
    private final Command drive;
    private double speedMultiplier = 1.0;

    private double forwardInput = 0;
    private double strafeInput = 0;
    private double turnInput = 0;

    public Drivetrain(Robot robot) {
        frontLeft = robot.hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = robot.hardwareMap.get(DcMotorEx.class, "frontRight");
        backLeft = robot.hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight = robot.hardwareMap.get(DcMotorEx.class, "backRight");

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        drive = Command.build()
                .setExecute(() -> {
                    double forward = forwardInput * speedMultiplier;
                    double strafe  = strafeInput * speedMultiplier;
                    double turn    = turnInput * speedMultiplier;

                    frontLeft.setPower(forward + strafe + turn);
                    frontRight.setPower(forward - strafe - turn);
                    backLeft.setPower(forward - strafe + turn);
                    backRight.setPower(forward + strafe - turn);

                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    frontLeft.setPower(0);
                    frontRight.setPower(0);
                    backLeft.setPower(0);
                    backRight.setPower(0);
                })
                .requiring(frontLeft, frontRight, backLeft, backRight);

        Command slowMode = Command.build()
                .setStart(() -> speedMultiplier = 0.4)
                .setDone(() -> true);

        Command turboMode = Command.build()
                .setStart(() -> speedMultiplier = 1.5)
                .setDone(() -> true);


    }
    public void setInput(double forward, double strafe, double turn) {
        forwardInput = forward;
        strafeInput = strafe;
        turnInput = turn;
    }
    public void setSpeedMode(boolean slow, boolean turbo) {
        if (slow) {
            speedMultiplier = 0.4;
        } else if (turbo) {
            speedMultiplier = 1.5;
        } else {
            speedMultiplier = 1.0;
        }
    }
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }
    public Command periodic() {
        return drive;
    }

}

