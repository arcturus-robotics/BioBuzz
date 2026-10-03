package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

@Config
public class Drivetrain {
    private final DcMotor frontLeft;
    private final DcMotor frontRight;
    private final DcMotor backLeft;
    private final DcMotor backRight;
    private final Command drive;
    private double speedMultiplier = 1.0;

    private double forwardInput = 0;
    private double strafeInput = 0;
    private double turnInput = 0;

    private int targetFL, targetFR, targetBL, targetBR;
    private boolean holdingPosition = false;
    private boolean brakeRequested = false;

    private static final double STICK_DEADZONE = 0.05;

    public static double RETURN_KP = 0.025;
    public static double RETURN_MAX_POWER = 0.5;
    public static int MIN_ERROR_TO_CORRECT = 30;

    private final Limelight3A limelight;

    public static int[] TARGET_TAG_IDS = {31, 32, 35, 36};

    public static double TARGET_OFFSET_DEGREES = 0;

    public static double TURN_GAIN = 0.02;
    public static double MAX_AUTO_TURN = 0.4;
    public static double MIN_TURN_POWER = 0.08;
    public static double ALIGNMENT_TOLERANCE = 1.5;
    public static double ALIGNED_SETTLE_MS = 150;
    public static double MAX_STALENESS_MS = 100;
    public static double TURN_SIGN = 1.0;
    public static int LIMELIGHT_PIPELINE = 0;

    private boolean alignRequested = false;
    private boolean alignActive = false;
    private boolean targetVisible = false;
    private boolean aligned = false;
    private long alignedSinceMs = 0;
    private int detectedTagId = -1;
    private double tagTx = 0;
    private double alignError = 0;
    private double autoTurnPower = 0;


    public Drivetrain(Robot robot) {
        frontLeft = robot.hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = robot.hardwareMap.get(DcMotorEx.class, "frontRight");
        backLeft = robot.hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight = robot.hardwareMap.get(DcMotorEx.class, "backRight");

        limelight = robot.hardwareMap.get(Limelight3A.class, "Limelight");
        limelight.pipelineSwitch(LIMELIGHT_PIPELINE);
        limelight.start();

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        targetFL = frontLeft.getCurrentPosition();
        targetFR = frontRight.getCurrentPosition();
        targetBL = backLeft.getCurrentPosition();
        targetBR = backRight.getCurrentPosition();

        drive = Command.build()
                .setExecute(() -> {
                    boolean driverControlling =
                            Math.abs(forwardInput) > STICK_DEADZONE ||
                                    Math.abs(strafeInput)  > STICK_DEADZONE ||
                                    Math.abs(turnInput)    > STICK_DEADZONE;

                    alignActive = alignRequested && !driverControlling && !brakeRequested;


                    boolean shouldHold = brakeRequested || !driverControlling;

                    if (alignActive) {

                        holdingPosition = false;
                        runAutoAlign();

                    } else if (!shouldHold) {
                        resetAlignState();
                        holdingPosition = false;

                        double forward = forwardInput * speedMultiplier;
                        double strafe  = strafeInput * speedMultiplier;
                        double turn    = turnInput * speedMultiplier;

                        frontLeft.setPower(forward + strafe + turn);
                        frontRight.setPower(forward - strafe - turn);
                        backLeft.setPower(forward - strafe + turn);
                        backRight.setPower(forward + strafe - turn);

                    } else {
                        resetAlignState();
                        if (!holdingPosition) {
                            targetFL = frontLeft.getCurrentPosition();
                            targetFR = frontRight.getCurrentPosition();
                            targetBL = backLeft.getCurrentPosition();
                            targetBR = backRight.getCurrentPosition();
                            holdingPosition = true;
                        }

                        int errorFL = targetFL - frontLeft.getCurrentPosition();
                        int errorFR = targetFR - frontRight.getCurrentPosition();
                        int errorBL = targetBL - backLeft.getCurrentPosition();
                        int errorBR = targetBR - backRight.getCurrentPosition();

                        frontLeft.setPower(calculateReturnPower(errorFL));
                        frontRight.setPower(calculateReturnPower(errorFR));
                        backLeft.setPower(calculateReturnPower(errorBL));
                        backRight.setPower(calculateReturnPower(errorBR));
                    }
                })
                .setDone(() -> false)
                .setEnd(endCondition -> {
                    frontLeft.setPower(0);
                    frontRight.setPower(0);
                    backLeft.setPower(0);
                    backRight.setPower(0);
                })
                .requiring(frontLeft, frontRight, backLeft, backRight);
    }


    private void runAutoAlign() {
        targetVisible = false;
        detectedTagId = -1;
        autoTurnPower = 0;

        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid() && result.getStaleness() < MAX_STALENESS_MS) {
            List<LLResultTypes.FiducialResult> tags = result.getFiducialResults();


            LLResultTypes.FiducialResult best = null;
            for (LLResultTypes.FiducialResult fr : tags) {
                if (!isTargetTag((int) fr.getFiducialId())) continue;
                if (best == null || Math.abs(fr.getTargetXDegrees()) < Math.abs(best.getTargetXDegrees())) {
                    best = fr;
                }
            }

            if (best != null) {
                targetVisible = true;
                detectedTagId = (int) best.getFiducialId();
                tagTx = best.getTargetXDegrees();
                alignError = tagTx - TARGET_OFFSET_DEGREES;

                if (Math.abs(alignError) < ALIGNMENT_TOLERANCE) {
                    autoTurnPower = 0;
                } else {
                    double turn = TURN_SIGN * alignError * TURN_GAIN;
                    if (turn > 0 && turn < MIN_TURN_POWER) turn = MIN_TURN_POWER;
                    else if (turn < 0 && turn > -MIN_TURN_POWER) turn = -MIN_TURN_POWER;
                    autoTurnPower = clamp(turn, -MAX_AUTO_TURN, MAX_AUTO_TURN);
                }
            }
        }


        boolean insideTolerance = targetVisible && Math.abs(alignError) < ALIGNMENT_TOLERANCE;
        if (insideTolerance) {
            if (alignedSinceMs == 0) alignedSinceMs = System.currentTimeMillis();
            aligned = (System.currentTimeMillis() - alignedSinceMs) >= ALIGNED_SETTLE_MS;
        } else {
            alignedSinceMs = 0;
            aligned = false;
        }

        frontLeft.setPower(autoTurnPower);
        backLeft.setPower(autoTurnPower);
        frontRight.setPower(-autoTurnPower);
        backRight.setPower(-autoTurnPower);
    }

    private boolean isTargetTag(int id) {
        for (int t : TARGET_TAG_IDS) {
            if (t == id) return true;
        }
        return false;
    }

    private void resetAlignState() {
        targetVisible = false;
        aligned = false;
        alignedSinceMs = 0;
        autoTurnPower = 0;
    }

    public void setAlignRequested(boolean pressed) {
        alignRequested = pressed;
    }

    public boolean isReadyToShoot() {
        return alignActive && aligned;
    }

    public boolean isAligning() {
        return alignActive;
    }


    public void stopLimelight() {
        limelight.stop();
    }


    private double calculateReturnPower(int error) {
        if (Math.abs(error) < MIN_ERROR_TO_CORRECT) {
            return 0;
        }
        double power = error * RETURN_KP;
        return clamp(power, -RETURN_MAX_POWER, RETURN_MAX_POWER);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public void setInput(double forward, double strafe, double turn) {
        forwardInput = forward;
        strafeInput = strafe;
        turnInput = turn;
    }

    public void setBrakeRequested(boolean pressed) {
        brakeRequested = pressed;
    }

    public void setSpeedMode(boolean slow, boolean turbo) {
        if (slow) {
            speedMultiplier = 0.4;
        } else if (turbo) {
            speedMultiplier = 1.5;
        } else {
            speedMultiplier = 1.0;
        }
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public Command periodic() {
        return drive;
    }

    public void logTelemetry(Telemetry telemetry) {
        boolean driverControlling =
                Math.abs(forwardInput) > STICK_DEADZONE ||
                        Math.abs(strafeInput)  > STICK_DEADZONE ||
                        Math.abs(turnInput)    > STICK_DEADZONE;
        boolean shouldHold = brakeRequested || !driverControlling;

        telemetry.addLine("===== LIMELIGHT ALIGN =====");
        telemetry.addData("Align Requested", alignRequested);
        telemetry.addData("Align Active", alignActive);
        telemetry.addData("Tag Visible", targetVisible);
        telemetry.addData("Tag ID", detectedTagId);
        telemetry.addData("Tag tx (deg)", "%.2f", tagTx);
        telemetry.addData("Error (deg)", "%.2f", alignError);
        telemetry.addData("Turn Power", "%.2f", autoTurnPower);
        telemetry.addData("Ready To Shoot", isReadyToShoot());

        telemetry.addLine("===== BRAKE =====");
        telemetry.addData("Mode", alignActive ? "ALIGNING" : (shouldHold ? "HOLDING" : "DRIVING"));
        telemetry.addData("Brake Requested", brakeRequested);
        telemetry.addData("Holding Position Flag", holdingPosition);

        int errorFL = targetFL - frontLeft.getCurrentPosition();
        int errorFR = targetFR - frontRight.getCurrentPosition();
        int errorBL = targetBL - backLeft.getCurrentPosition();
        int errorBR = targetBR - backRight.getCurrentPosition();

        telemetry.addData("FL pos/target/err", "%d / %d / %d",
                frontLeft.getCurrentPosition(), targetFL, errorFL);
        telemetry.addData("FR pos/target/err", "%d / %d / %d",
                frontRight.getCurrentPosition(), targetFR, errorFR);
        telemetry.addData("BL pos/target/err", "%d / %d / %d",
                backLeft.getCurrentPosition(), targetBL, errorBL);
        telemetry.addData("BR pos/target/err", "%d / %d / %d",
                backRight.getCurrentPosition(), targetBR, errorBR);

        if (shouldHold && !alignActive) {
            telemetry.addData("FL return power", "%.2f", calculateReturnPower(errorFL));
            telemetry.addData("FR return power", "%.2f", calculateReturnPower(errorFR));
            telemetry.addData("BL return power", "%.2f", calculateReturnPower(errorBL));
            telemetry.addData("BR return power", "%.2f", calculateReturnPower(errorBR));
        }
    }
}