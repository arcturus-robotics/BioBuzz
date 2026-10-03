package org.firstinspires.ftc.teamcode.subsystems;

import static com.pedropathing.ivy.Scheduler.schedule;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.robot.Robot;

@Config
public class Outtake {
    private final DcMotorEx shooterMotor;
    public static double shooter_velocity = 1500;

    private final Command shooterOn;

    private boolean shooterActive = false;

    public Outtake(Robot robot) {

        shooterMotor = robot.hardwareMap.get(DcMotorEx.class, "shooter");
        shooterMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        shooterOn = Command.build()
                .setExecute(() -> {
                    shooterMotor.setVelocity(shooter_velocity);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    shooterMotor.setVelocity(0);
                })
                .requiring(shooterMotor);
    }

    public void setShooterMode(boolean shooterActive) {
        this.shooterActive = shooterActive;
        if (shooterActive) {
            schedule(shooterOn);
        } else {
            Scheduler.cancel(shooterOn);
            shooterMotor.setVelocity(0);
        }
    }

    public void toggleShooter() {
        setShooterMode(!shooterActive);
    }

    public boolean isShooterActive() {
        return shooterActive;
    }
}