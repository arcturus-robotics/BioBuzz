package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.robot.RobotOpMode;


@TeleOp(name = "CoolTeleOp", group = "Competition")
@Config
public class TeleWithCoolStuff extends RobotOpMode {

    @Override
    public void loop() {

        // ---- Drive ----
        double forward = -gamepad1.left_stick_y;
        double strafe  =  gamepad1.left_stick_x;
        double turn    = -gamepad1.right_stick_x;
        robot.drivetrain.setInput(forward, strafe, turn);

        robot.drivetrain.setSpeedMode(gamepad1.left_trigger > 0.5, gamepad1.right_trigger > 0.5);
        robot.drivetrain.setBrakeRequested(gamepad1.right_bumper);
        robot.drivetrain.setAlignRequested(gamepad1.left_bumper);

        boolean autoShoot = gamepad1.left_bumper
                && robot.drivetrain.isReadyToShoot()
                && robot.flywheel.isAtSpeed();

        robot.setCombinedIntakeMode(
                gamepad2.dpad_up, gamepad2.dpad_down,
                gamepad2.a || autoShoot, gamepad2.x
        );

        robot.flywheel.setIntakeMode(gamepad2.y);

        robot.stopper.setOpen(gamepad2.b || autoShoot);

        robot.colorDetection.logTelemetry(robot.telemetry);
        robot.drivetrain.logTelemetry(robot.telemetry);

        super.loop();
    }
}