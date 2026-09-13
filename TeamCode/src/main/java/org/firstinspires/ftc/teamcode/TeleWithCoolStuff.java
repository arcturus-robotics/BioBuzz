package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.robot.RobotOpMode;

@TeleOp(name = "CoolTeleOp", group = "Competition")
@Config
public class TeleWithCoolStuff extends RobotOpMode {

    @Override
    public void loop() {

        double forward = -gamepad1.left_stick_y;
        double strafe  =  gamepad1.left_stick_x;
        double turn    = -gamepad1.right_stick_x;
        robot.drivetrain.setInput(forward, strafe, turn);

        robot.drivetrain.setSpeedMode(gamepad1.left_bumper, gamepad1.right_bumper);
        robot.drivetrain.setBrakeRequested(gamepad1.right_trigger_pressed);
        robot.intakewithsweeper.setIntakeMode(gamepad2.a, gamepad2.y);

        robot.colorDetection.logTelemetry(robot.telemetry);
        robot.drivetrain.logTelemetry(robot.telemetry);

        super.loop();
    }
}
