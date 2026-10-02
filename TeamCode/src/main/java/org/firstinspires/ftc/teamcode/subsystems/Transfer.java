package org.firstinspires.ftc.teamcode.subsystems;

import static com.pedropathing.ivy.Scheduler.schedule;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.robot.Robot;

@Config

public class Transfer {
    private final DcMotor transferMotor;
    private final double transfer_power = 1.0;

    private final Command transferIn;
    private final Command transferOut;


    public Transfer(Robot robot) {

        transferMotor = robot.hardwareMap.get(DcMotorEx.class, "transfer");

        transferIn = Command.build()
                .setExecute(() -> {
                    transferMotor.setPower(transfer_power);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    transferMotor.setPower(0);
                })
                .requiring(transferMotor);

        transferOut = Command.build()
                .setExecute(() -> {
                    transferMotor.setPower(-transfer_power);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    transferMotor.setPower(0);
                })
                .requiring(transferMotor);
    }

    public void setTransferMode (boolean transferringIn, boolean transferringOut) {
        if (transferringIn) {
            schedule(transferIn);
        }
        else if (transferringOut) {
            schedule(transferOut);
        }
        else {
            Scheduler.cancel(transferIn);
            Scheduler.cancel(transferOut);
            transferMotor.setPower(0);
        }
    }

}