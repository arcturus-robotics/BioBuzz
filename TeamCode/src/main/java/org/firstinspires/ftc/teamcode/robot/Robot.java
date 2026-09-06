package org.firstinspires.ftc.teamcode.robot;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.*;

public class Robot {
    public final HardwareMap hardwareMap;
    public final Telemetry telemetry;
    public final Drivetrain drivetrain;
    public final Intake intake;
    public ColorDetection colorDetection;



    public Robot(OpMode opMode) {
        hardwareMap = opMode.hardwareMap;
        telemetry = new MultipleTelemetry(
                opMode.telemetry,
                FtcDashboard.getInstance().getTelemetry(),
                PanelsTelemetry.INSTANCE.getFtcTelemetry()
        );

        drivetrain = new Drivetrain(this);
        intake = new Intake(this);
        colorDetection = new ColorDetection(this);

        telemetry.addLine("===== YELLOW BALL =====");
        if (colorDetection.isBallDetected()) {
            telemetry.addData("Ball Detected", "YES");
            telemetry.addData("TX (left/right)", "%.2f°", colorDetection.getTx());
            telemetry.addData("TY (up/down)", "%.2f°", colorDetection.getTy());
            telemetry.addData("TA (size)", "%.2f%%", colorDetection.getTa());
        } else {
            telemetry.addData("Ball Detected", "NO");
        }

        telemetry.addData("Speed Multiplier", drivetrain.getSpeedMultiplier());
        telemetry.update();

    }
}