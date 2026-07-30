package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Slide Position", group = "Pre-Season")
public class slidePositionFinder extends OpMode {
    private DcMotor leftFront, rightFront, leftBack, rightBack;
    private DcMotor slideOne;
    private DcMotor slideTwo;
    private int slidePos;

    public void init() {

        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");

        slideOne = hardwareMap.get(DcMotor.class, "slide1");
        slideTwo = hardwareMap.get(DcMotor.class, "slide2");

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);

        slideOne.setTargetPosition(slidePos);
        slideTwo.setTargetPosition(slidePos);
    }

    public void loop() {

        double axial = -gamepad1.left_stick_y;
        double lateral = gamepad1.left_stick_x;
        double yaw = gamepad1.right_stick_x;

        double speedMult = 1.0;
        if (gamepad1.right_trigger > 0.5) speedMult = 2.0;
        if (gamepad1.left_trigger > 0.5) speedMult = 1.0 / 3.0;

        axial *= speedMult;
        lateral *= speedMult;
        yaw *= speedMult;

        double fl = axial + lateral + yaw;
        double fr = axial - lateral - yaw;
        double bl = axial - lateral + yaw;
        double br = axial + lateral - yaw;

        double max = Math.max(1.0, Math.max(Math.abs(fl),
                Math.max(Math.abs(fr), Math.max(Math.abs(bl), Math.abs(br)))));
        leftFront.setPower(fl / max);
        rightFront.setPower(fr / max);
        leftBack.setPower(bl / max);
        rightBack.setPower(br / max);


        if (gamepad2.left_stick_y > 0.5) {
            slidePos = slidePos + 1;
        } else if (gamepad2.left_stick_y < -0.5) {
            slidePos = slidePos + 1;
        }

        telemetry.addData("Slide Position", slidePos);
        telemetry.update();
    }


}
//