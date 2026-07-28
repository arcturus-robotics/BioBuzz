package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;

@TeleOp(name="JulyCampTeleop", group="ArtemisBioBuzz")
public class JulyCampTeleop extends OpMode {

    private DcMotor leftFrontDrive;
    private DcMotor rightFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightBackDrive;

    private DcMotor intakeWheel;
    private DcMotor transferWheel;


    final private int intakeWheelPower = 1;
    final private int transferWheelPower = 1;

    @Override
    public void init() {

        leftFrontDrive = hardwareMap.get(DcMotor.class, "leftFront");
        leftBackDrive = hardwareMap.get(DcMotor.class, "leftBack");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rightFront");
        rightBackDrive = hardwareMap.get(DcMotor.class, "rightBack");

        intakeWheel = hardwareMap.get(DcMotor.class, "intakeWheel");
        transferWheel = hardwareMap.get(DcMotor.class, "transferWheel");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        intakeWheel.setDirection(DcMotor.Direction.REVERSE);
        transferWheel.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addLine("SIMPLE TELEOP READY");
        telemetry.update();
    }

    @Override
    public void loop() {

        // ===== DRIVE CODE =====
        double axial   = -gamepad1.left_stick_y;
        double lateral =  gamepad1.left_stick_x;
        double yaw     =  gamepad1.right_stick_x;

        double leftFrontPower  = axial + lateral + yaw;
        double rightFrontPower = axial - lateral - yaw;
        double leftBackPower   = axial - lateral + yaw;
        double rightBackPower  = axial + lateral - yaw;

        double max = Math.max(Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower)),
                Math.max(Math.abs(leftBackPower), Math.abs(rightBackPower)));

        if (max > 1.0) {
            leftFrontPower  /= max;
            rightFrontPower /= max;
            leftBackPower   /= max;
            rightBackPower  /= max;
        }

        if (gamepad1.right_trigger > 0.5) {
            leftFrontPower *= 2;
            leftBackPower *= 2;
            rightFrontPower *= 2;
            rightBackPower *= 2;
        }

        if (gamepad1.left_trigger > 0.5) {
            leftFrontPower /= 3;
            leftBackPower /= 3;
            rightFrontPower /= 3;
            rightBackPower /= 3;
        }

        leftFrontDrive.setPower(leftFrontPower);
        rightFrontDrive.setPower(rightFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightBackDrive.setPower(rightBackPower);


        if (gamepad2.a) {
            intakeWheel.setPower(intakeWheelPower);
            transferWheel.setPower(transferWheelPower);
        } else if (gamepad2.dpad_up) {
            intakeWheel.setPower(-intakeWheelPower);
            transferWheel.setPower(-transferWheelPower);
        } else {
            intakeWheel.setPower(0);
            transferWheel.setPower(0);
        }


        if (gamepad2.dpad_down) {
            transferWheel.setPower(-transferWheelPower);
            intakeWheel.setPower(intakeWheelPower);
        }


        telemetry.update();
    }
}