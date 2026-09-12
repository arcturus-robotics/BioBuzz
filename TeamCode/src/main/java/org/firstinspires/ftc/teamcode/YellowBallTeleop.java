package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;

import java.util.List;

import static com.pedropathing.ivy.Scheduler.schedule;
@TeleOp(name = "TeleopWithYellowButton")
public class YellowBallTeleop extends LinearOpMode {
    private DcMotor frontLeft, frontRight, backLeft, backRight, intakeMotor;
    private Follower follower;
    private Limelight3A limelight;
    private double speedMultiplier = 1.0;
    private int intake_power = 1; //intake power

    private static final int YELLOW_BALL_PIPELINE = 0;

    private boolean ballDetected = false;
    private double tx = 0;
    private double ty = 0;
    private double ta = 0;

    // ---- Auto-align-to-ball tuning ----
    private static final double TURN_KP = 0.02;          // turn power per degree of tx error
    private static final double APPROACH_SPEED = 0.5;    // forward power while approaching
    private static final double TX_ALIGN_TOLERANCE = 2.5; // degrees - stop turning once aligned this well
    private static final double CLOSE_ENOUGH_AREA = 6.0;  // ta % - stop driving forward once this close

    private boolean ballAssistActive = false;

    @Override
    public void runOpMode() {

        Scheduler.reset();

        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        intakeMotor = hardwareMap.get(DcMotor.class, "intake");

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        limelight = hardwareMap.get(Limelight3A.class, "Limelight");
        limelight.pipelineSwitch(YELLOW_BALL_PIPELINE);
        limelight.start();

        Command drive = Command.build()
                .setExecute(() -> {
                    double forward = -gamepad1.left_stick_y * speedMultiplier;
                    double strafe  =  gamepad1.left_stick_x * speedMultiplier;
                    double turn    =  -gamepad1.right_stick_x * speedMultiplier;

                    frontLeft.setPower(forward + strafe + turn);
                    frontRight.setPower(forward - strafe - turn);
                    backLeft.setPower(forward - strafe + turn);
                    backRight.setPower(forward + strafe - turn);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    frontLeft.setPower(0);
                    frontRight.setPower(0);
                    backLeft.setPower(0);
                    backRight.setPower(0);
                })
                .requiring(frontLeft, frontRight, backLeft, backRight);

        Command slowMode = Command.build()
                .setStart(() -> speedMultiplier = 0.4)
                .setDone(() -> true);

        Command turboMode = Command.build()
                .setStart(() -> speedMultiplier = 1.5)
                .setDone(() -> true);

        Command intake = Command.build()
                .setExecute(() -> {
                    intakeMotor.setPower(intake_power); //normal intake
                })
                .setDone(() -> true)
                .setEnd(endCondition -> {
                    intakeMotor.setPower(0);
                })
                .requiring(intakeMotor);

        Command reverseIntake = Command.build()
                .setExecute(() -> {
                    intakeMotor.setPower(-intake_power); //reverse intake
                })
                .setDone(() -> true)
                .setEnd(endCondition -> {
                    intakeMotor.setPower(0);
                })
                .requiring(intakeMotor);


        Command yellow_ball_detection = Command.build()
                .setExecute(() -> {
                    LLResult result = limelight.getLatestResult();

                    ballDetected = false;
                    tx = 0;
                    ty = 0;
                    ta = 0;

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
                })
                .setDone(() -> false)
                .requiring(limelight);

        // Drives toward the nearest detected yellow ball: turns to null out tx,
        // drives forward while the ball still looks small (far away), and stops
        // driving forward once ta says we're close. Shares the drivetrain
        // requirement with `drive`, so scheduling this interrupts manual driving,
        // and rescheduling `drive` hands control back to the sticks.
        Command goToNearestBall = Command.build()
                .setExecute(() -> {
                    if (!ballDetected) {
                        frontLeft.setPower(0);
                        frontRight.setPower(0);
                        backLeft.setPower(0);
                        backRight.setPower(0);
                        return;
                    }

                    double turn = Math.abs(tx) > TX_ALIGN_TOLERANCE ? -tx * TURN_KP : 0;
                    double forward = ta < CLOSE_ENOUGH_AREA ? APPROACH_SPEED : 0;

                    frontLeft.setPower(forward + turn);
                    frontRight.setPower(forward - turn);
                    backLeft.setPower(forward + turn);
                    backRight.setPower(forward - turn);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    frontLeft.setPower(0);
                    frontRight.setPower(0);
                    backLeft.setPower(0);
                    backRight.setPower(0);
                })
                .requiring(frontLeft, frontRight, backLeft, backRight);


        waitForStart();

        schedule(drive);
        schedule(yellow_ball_detection);

        while (opModeIsActive()) {
            //speed & turbo mode
            if (gamepad1.left_bumper) {
                schedule(slowMode);
            } else if (gamepad1.right_bumper) {
                schedule(turboMode);
            } else {
                speedMultiplier = 1.0;
            }

            if (gamepad2.a) {
                schedule(intake);
            } else if (gamepad2.y) {
                schedule(reverseIntake);
            } else {
                intakeMotor.setPower(0);
            }

            // Hold gamepad1.dpad_up: auto-drive to the nearest yellow ball.
            // Release, or get close enough to it, and manual driving resumes.
            if (gamepad1.dpad_up) {
                if (!ballAssistActive) {
                    schedule(goToNearestBall);
                    ballAssistActive = true;
                }
                if (ballDetected && ta >= CLOSE_ENOUGH_AREA) {
                    schedule(drive);
                    ballAssistActive = false;
                }
            } else if (ballAssistActive) {
                schedule(drive);
                ballAssistActive = false;
            }

            Scheduler.execute();
            telemetry.addLine("===== YELLOW BALL =====");
            if (ballDetected) {
                telemetry.addData("Ball Detected", "YES");
                telemetry.addData("TX (left/right)", "%.2f°", tx);
                telemetry.addData("TY (up/down)", "%.2f°", ty);
                telemetry.addData("TA (size)", "%.2f%%", ta);
            } else {
                telemetry.addData("Ball Detected", "NO");
            }

            telemetry.addData("Ball Assist Active", ballAssistActive);
            telemetry.addData("Speed Multiplier", speedMultiplier);
            telemetry.update();
        }


        limelight.stop();
    }
}