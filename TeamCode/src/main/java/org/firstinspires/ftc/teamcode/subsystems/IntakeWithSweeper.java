package org.firstinspires.ftc.teamcode.subsystems;

import static com.pedropathing.ivy.Scheduler.schedule;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.robot.Robot;

@Config

public class IntakeWithSweeper {
    private final DcMotor intakeMotor;
    private final double intake_power = 1.0;

    private final CRServo sweeperLeft;
    private final CRServo sweeperRight;

    private final double intake_servo_power = 1;
    private final Command intake;
    private final Command outtake;

    public IntakeWithSweeper(Robot robot) {

        intakeMotor = robot.hardwareMap.get(DcMotorEx.class, "intake");
        sweeperLeft = robot.hardwareMap.get(CRServo.class, "sweeperLeft");
        sweeperRight = robot.hardwareMap.get(CRServo.class, "sweeperRight");

        intake = Command.build()
                .setExecute(() -> {
                    //normal intaking + sweepers
                    intakeMotor.setPower(intake_power);
                    sweeperLeft.setPower(-intake_servo_power);
                    sweeperRight.setPower(-intake_servo_power);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    intakeMotor.setPower(0);
                    sweeperLeft.setPower(0);
                    sweeperRight.setPower(0);
                })
                .requiring(intakeMotor, sweeperLeft, sweeperRight);

        outtake = Command.build()
                .setExecute(() -> {
                    //reverse intake + sweeper
                    intakeMotor.setPower(-intake_power);
                    sweeperLeft.setPower(intake_servo_power);
                    sweeperRight.setPower(intake_servo_power);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    intakeMotor.setPower(0);
                    sweeperLeft.setPower(0);
                    sweeperRight.setPower(0);
                })
                .requiring(intakeMotor, sweeperLeft, sweeperRight);
    }

    public void setIntakeMode (boolean intaking, boolean outtaking) {
        if (intaking) {
            schedule(intake);
        }
        else if (outtaking) {
            schedule(outtake);
        }
        else {
            Scheduler.cancel(intake);
            Scheduler.cancel(outtake);
            intakeMotor.setPower(0);
            sweeperLeft.setPower(0);
            sweeperRight.setPower(0);

        }
    }

}