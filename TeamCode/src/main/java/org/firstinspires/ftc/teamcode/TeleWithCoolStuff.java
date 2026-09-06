package org.firstinspires.ftc.teamcode;

import static com.pedropathing.ivy.Scheduler.schedule;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.geometry.Pose;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.robot.RobotOpMode;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

@TeleOp(name = "CoolTeleOp", group = "Competition")
@Config
public class TeleWithCoolStuff extends RobotOpMode {

    @Override
    public void init() {
        super.init();

        Scheduler.reset();
        schedule(
                robot.drivetrain.periodic(),
                robot.colorDetection.periodic());
    }

    @Override
    public void start() {
    }

    @Override
    public void loop() {

        double forward = -gamepad1.left_stick_y;
        double strafe  =  gamepad1.left_stick_x;
        double turn    = -gamepad1.right_stick_x;
        robot.drivetrain.setInput(forward, strafe, turn);

        robot.drivetrain.setSpeedMode(gamepad1.left_bumper, gamepad1.right_bumper);
        robot.intake.setIntakeMode(gamepad2.a, gamepad2.y);

        Scheduler.execute();

        super.loop();
    }
}
