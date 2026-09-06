// this is saying "this file is in package org.firstinspires.ftc.teamcode"
package org.firstinspires.ftc.teamcode.extras;

//import statements, basically brings code from other files/libraries into this file.
//try deleting one of them and see what happens
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name="Pepperoni", group="Pre-Season")
public class pepperoniTeleop extends OpMode {

    //drive motors
    private DcMotor leftFrontDrive;
    private DcMotor rightFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightBackDrive;

    //intake motor
    private DcMotor intake;
    //slide motor
    private DcMotor slide;
    //bucket servo
    private Servo bucket;

    // intake power (set to 100%)
    final private double intakePower = 0.3;
    // slide positions, self explanatory btw pos means position
    private int maxSlidePos = -6710;
    private int minSlidePos =0;
    private int slidePos;

    // bucket positions
    private double bucketScorePos = 0.75;
    private double bucketRestPos = 0;

    @Override
    public void init() {
        // this is what we will refer to the motors/servos when configuring
        leftFrontDrive = hardwareMap.get(DcMotor.class, "lf");
        leftBackDrive = hardwareMap.get(DcMotor.class, "lb");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rf");
        rightBackDrive = hardwareMap.get(DcMotor.class, "rb");

        intake = hardwareMap.get(DcMotor.class, "intake");
        slide = hardwareMap.get(DcMotor.class, "slide");
        bucket = hardwareMap.get(Servo.class, "bucket");


        //setting the directions of the motors
        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        slide.setDirection(DcMotor.Direction.FORWARD);

        //this means that the drive motors do not have encoder wires
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //sets positions for the slides
        bucket.setPosition(0);

        //slide stuff
        slide.setPower(1);
        slide.setTargetPosition(slidePos);
        slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);





        //confirms that the robot is initialized
        telemetry.addLine("TELEOP INITIALIZED");
        telemetry.update();
    }

    @Override
    public void loop() {


        // drive code, just a bunch of math ignore this
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

        // turbo mode
        if (gamepad1.right_trigger > 0.5) { //if you press right trigger
            //the drive power will be doubled
            leftFrontPower *= 2;
            leftBackPower *= 2;
            rightFrontPower *= 2;
            rightBackPower *= 2;
        }

        // slow mode
        if (gamepad1.left_trigger > 0.5) { // if you press left trigger
            // the drive power will be 1/3 of the og power
            leftFrontPower /= 3;
            leftBackPower /= 3;
            rightFrontPower /= 3;
            rightBackPower /= 3;
        }

        // sets drive power
        leftFrontDrive.setPower(leftFrontPower);
        rightFrontDrive.setPower(rightFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightBackDrive.setPower(rightBackPower);


        if (gamepad2.dpad_down) { // if you press a
            intake.setPower(intakePower); // the intake will intake balls
        } else if (gamepad2.dpad_up) { // otherwise, if you press dpad up
            intake.setPower(-intakePower); // the intake will spin the opposite direction
        } else { // if no buttons are pressed
            intake.setPower(0); // don't move the intake
        }

        if (gamepad2.left_stick_y > 0.5) {
            slidePos = slidePos + 10;
            if (slidePos > -6710) slidePos = -6710;
            slide.setTargetPosition(slidePos);
        } else if (gamepad2.left_stick_y < -0.5) {
            slidePos = slidePos - 10;
            slide.setTargetPosition(slidePos);
        }

        if (gamepad2.left_bumper) {
            bucket.setPosition(bucketScorePos); // the bucket will tilt and score
        } else if (gamepad2.right_bumper) {
            bucket.setPosition(bucketRestPos);
        }

        telemetry.update();

    }
}