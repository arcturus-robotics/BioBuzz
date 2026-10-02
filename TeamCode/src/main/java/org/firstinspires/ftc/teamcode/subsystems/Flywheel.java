package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import static com.pedropathing.ivy.Scheduler.schedule;
import org.firstinspires.ftc.teamcode.robot.Robot;

@Config

public class Flywheel {
    private final DcMotorEx flywheel;
    public static double launch_velocity = 1000;
    public static double SPEED_TOLERANCE = 50;
    private final Command shoot;

    public Flywheel(Robot robot) {

        flywheel = robot.hardwareMap.get(DcMotorEx.class, "leftLaunch");


        shoot = Command.build()
                .setExecute(() -> {
                    flywheel.setVelocity(launch_velocity);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    flywheel.setVelocity(0);
                })
                .requiring(flywheel);
    }

    public void setIntakeMode (boolean shooting) {
        if (shooting) {
            schedule(shoot);
        } else {
            Scheduler.cancel(shoot);
            flywheel.setVelocity(0);
        }
    }

    // true when both motors are within tolerance of the target speed
    public boolean isAtSpeed() {
        return Math.abs(flywheel.getVelocity() - launch_velocity) < SPEED_TOLERANCE;
    }

}