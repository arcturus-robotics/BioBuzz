package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.robotcore.external.Telemetry;

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

    // ---- Position hold state ----
    private int targetFL, targetFR, targetBL, targetBR;
    private boolean holdingPosition = false;
    private boolean brakeRequested = false; // set by right bumper

    private static final double STICK_DEADZONE = 0.05;

    // Tune these on FTC Dashboard
    public static double RETURN_KP = 0.025;
    public static double RETURN_MAX_POWER = 0.5;
    public static int MIN_ERROR_TO_CORRECT = 30;

    public Drivetrain(Robot robot) {
        frontLeft = robot.hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = robot.hardwareMap.get(DcMotorEx.class, "frontRight");
        backLeft = robot.hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight = robot.hardwareMap.get(DcMotorEx.class, "backRight");

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

                    // Right bumper forces a hold even if the driver bumps the stick;
                    // it takes priority over normal driving input.
                    boolean shouldHold = brakeRequested || !driverControlling;

                    if (!shouldHold) {
                        holdingPosition = false;

                        double forward = forwardInput * speedMultiplier;
                        double strafe  = strafeInput * speedMultiplier;
                        double turn    = turnInput * speedMultiplier;

                        frontLeft.setPower(forward + strafe + turn);
                        frontRight.setPower(forward - strafe - turn);
                        backLeft.setPower(forward - strafe + turn);
                        backRight.setPower(forward + strafe - turn);

                    } else {
                        // Capture the lock target ONCE, right when holding starts.
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

    /** Call this every loop from your OpMode with gamepad1.right_bumper. */
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

        telemetry.addLine("===== BRAKE =====");
        telemetry.addData("Mode", shouldHold ? "HOLDING" : "DRIVING");
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

        if (shouldHold) {
            telemetry.addData("FL return power", "%.2f", calculateReturnPower(errorFL));
            telemetry.addData("FR return power", "%.2f", calculateReturnPower(errorFR));
            telemetry.addData("BL return power", "%.2f", calculateReturnPower(errorBL));
            telemetry.addData("BR return power", "%.2f", calculateReturnPower(errorBR));
        }
    }


}