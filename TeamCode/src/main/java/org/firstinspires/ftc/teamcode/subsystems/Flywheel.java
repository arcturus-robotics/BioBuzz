package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
//import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;
import org.firstinspires.ftc.teamcode.robot.Robot;
import java.util.List;

@Config

public class Flywheel {
    private final DcMotorEx leftFlywheel;
    private final DcMotorEx rightFlywheel;
    private final double launch_velocity = 1000; //tune this later
    private final Command shoot;

    public Flywheel(Robot robot) {

        leftFlywheel = robot.hardwareMap.get(DcMotorEx.class, "leftLaunch");
        rightFlywheel = robot.hardwareMap.get(DcMotorEx.class, "rightLaunch");

        shoot = Command.build()
                .setExecute(() -> {
                    leftFlywheel.setVelocity(launch_velocity);
                    rightFlywheel.setVelocity(-launch_velocity);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    leftFlywheel.setVelocity(0);
                    rightFlywheel.setVelocity(0);
                })
                .requiring(leftFlywheel, rightFlywheel);
    }

    public void setIntakeMode (boolean shooting) {
        if (shooting) {
            schedule(shoot);
        } else {
            Scheduler.cancel(shoot);
            leftFlywheel.setVelocity(0);
            rightFlywheel.setVelocity(0);


        }
    }

}