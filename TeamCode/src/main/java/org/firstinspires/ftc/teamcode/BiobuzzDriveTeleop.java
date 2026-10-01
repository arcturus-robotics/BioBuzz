// SimpleDriveTeleop with color detection code

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import java.util.List;
import static com.pedropathing.ivy.Scheduler.schedule;

@TeleOp(name = "BiobuzzDriveTeleop", group = "Teleop")
public class BiobuzzDriveTeleop extends LinearOpMode {
    private DcMotor frontLeft, frontRight, backLeft, backRight, intakeMotor;
    private DcMotorEx leftFlywheel, rightFlywheel;
    private Limelight3A limelight;
    private double speedMultiplier = 1.0;
    private int intake_power = 1;
    private final double launch_velocity = 1000; //tune this later
    private static final int YELLOW_BALL_PIPELINE = 0;
    private boolean ballDetected = false;
    private double tx = 0;
    private double ty = 0;
    private double ta = 0;

    @Override
    public void runOpMode() {

        Scheduler.reset();

        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        leftFlywheel = hardwareMap.get(DcMotorEx.class, "leftLaunch");
        rightFlywheel = hardwareMap.get(DcMotorEx.class, "rightLaunch");

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

        Command shoot = Command.build()
                .setExecute(() -> {
                    leftFlywheel.setVelocity(launch_velocity);
                    rightFlywheel.setVelocity(launch_velocity);
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    leftFlywheel.setVelocity(0);
                    rightFlywheel.setVelocity(0);
                })
                .requiring(leftFlywheel, rightFlywheel);

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

            if (gamepad2.left_stick_y > 0.5) {
                schedule(shoot);
            } else {
                leftFlywheel.setVelocity(0);
                rightFlywheel.setVelocity(0);
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

            telemetry.addData("Speed Multiplier", speedMultiplier);
            telemetry.update();
        }

        limelight.stop();
    }
}
