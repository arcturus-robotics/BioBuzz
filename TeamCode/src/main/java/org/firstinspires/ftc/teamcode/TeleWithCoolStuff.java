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
        robot.intake.setIntakeMode(gamepad2.a, gamepad2.y);

        robot.telemetry.addLine("===== YELLOW BALL =====");
            if (robot.colorDetection.isBallDetected()) {
                robot.telemetry.addData("Ball Detected", "YES");
                robot.telemetry.addData("TX (left/right)", "%.2f°", robot.colorDetection.getTx());
                robot.telemetry.addData("TY (up/down)", "%.2f°", robot.colorDetection.getTy());
                robot.telemetry.addData("TA (size)", "%.2f%%", robot.colorDetection.getTa());
            } else {
                robot.telemetry.addData("Ball Detected", "NO");
            }
                robot.telemetry.addData("Speed Multiplier", robot.drivetrain.getSpeedMultiplier());

        super.loop();
    }
}
