package org.firstinspires.ftc.teamcode.subsystems;

import static com.pedropathing.ivy.Scheduler.schedule;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.robot.Robot;

@Config

public class Intake {
    private final DcMotor intakeMotor;
 public static double intake_power = -1.0;

    private final Command intake;
    private final Command outtake;

    public Intake(Robot robot) {

        intakeMotor = robot.hardwareMap.get(DcMotorEx.class, "intake");

        intake = Command.build()
                .setExecute(() -> {
                    // normal intaking
                    intakeMotor.setPower(intake_power);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    intakeMotor.setPower(0);
                })
                .requiring(intakeMotor);

        outtake = Command.build()
                .setExecute(() -> {
                    // reverse intake
                    intakeMotor.setPower(-intake_power);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    intakeMotor.setPower(0);
                })
                .requiring(intakeMotor);
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
        }
    }

}