package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.List;

@TeleOp(name = "BBSimpleTeleop", group = "ArtemisDecode")
public class BBSimpleTeleop extends OpMode {

    private Follower follower;

    private DcMotor intakeWheels;
    private DcMotor transferWheel;

    private Limelight3A limelight;

    private static final double INTAKE_POWER = 1.0;
    private static final double TRANSFER_POWER = 1.0;

    private static final int YELLOW_BALL_PIPELINE = 0;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.deactivateAllPIDFs();
        follower.startTeleopDrive();

        intakeWheels = hardwareMap.get(DcMotor.class, "intakeWheels");
        transferWheel = hardwareMap.get(DcMotor.class, "transferWheel");

        intakeWheels.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        transferWheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        limelight = hardwareMap.get(Limelight3A.class, "Limelight");
        limelight.pipelineSwitch(YELLOW_BALL_PIPELINE);
        limelight.start();

        telemetry.addLine("BBTeleop + Limelight Yellow Ball Ready");
        telemetry.update();
    }

    @Override
    public void loop() {
        follower.update();


        double axial = -gamepad1.left_stick_y;
        double lateral = gamepad1.left_stick_x;
        double yaw = gamepad1.right_stick_x;

        if (gamepad1.right_trigger > 0.5) {
            axial *= 2.0;
            lateral *= 2.0;
            yaw *= 2.0;
        } else if (gamepad1.left_trigger > 0.5) {
            axial /= 3.0;
            lateral /= 3.0;
            yaw /= 3.0;
        }

        follower.setTeleOpDrive(axial, lateral, yaw, true, 0);


        if (gamepad2.a) {
            intakeWheels.setPower(INTAKE_POWER);
            transferWheel.setPower(TRANSFER_POWER);
        } else if (gamepad2.dpad_up) {
            intakeWheels.setPower(-INTAKE_POWER);
            transferWheel.setPower(-TRANSFER_POWER);
        } else {
            intakeWheels.setPower(0);
            transferWheel.setPower(0);
        }

        if (gamepad2.dpad_down) intakeWheels.setPower(INTAKE_POWER);
        if (gamepad2.x) transferWheel.setPower(TRANSFER_POWER);
        else if (gamepad2.right_bumper) transferWheel.setPower(-TRANSFER_POWER);


        LLResult result = limelight.getLatestResult();

        boolean ballDetected = false;
        double tx = 0;
        double ty = 0;
        double ta = 0;

        if (result != null && result.isValid()) {
            List<LLResultTypes.ColorResult> colorTargets = result.getColorResults();
            if (colorTargets != null && !colorTargets.isEmpty()) {

                LLResultTypes.ColorResult ball = colorTargets.get(0);
                ballDetected = true;
                tx = ball.getTargetXDegrees();
                ty = ball.getTargetYDegrees();
                ta = ball.getTargetArea();
            }
        }


        telemetry.addLine("===== YELLOW BALL =====");
        if (ballDetected) {
            telemetry.addData("Ball Detected", "YES ✓");
            telemetry.addData("TX (left/right)", "%.2f°", tx);
            telemetry.addData("TY (up/down)", "%.2f°", ty);
            telemetry.addData("TA (size)", "%.2f%%", ta);
        } else {
            telemetry.addData("Ball Detected", "NO");
        }
        telemetry.addLine("Running");
        telemetry.update();
    }

    @Override
    public void stop() {
        intakeWheels.setPower(0);
        transferWheel.setPower(0);
        limelight.stop();
    }
}
