package org.firstinspires.ftc.teamcode.extras;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous (name = "cheeseAuto", group = "cheese")
public class cheeseAuto extends LinearOpMode {
    //drive motors
    private DcMotor leftFront, rightFront, leftBack, rightBack;

    //intake motor
    private DcMotor intake;
    //transfer motor
    private DcMotor transfer;
    private Servo door;
    private DcMotor slideOne;
    private DcMotor slideTwo;

    private int maxSlide=2300;
    private int minSlide=1;

    private double slidepower=1;

    private int slidePos=1;

    private double forwardPower = 1;

    private double turnPower = 0.7;





    @Override
    public void runOpMode() throws InterruptedException {


        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        door = hardwareMap.get(Servo.class, "doorServo");

        //define intake
        intake = hardwareMap.get(DcMotor.class,"intake");
        transfer = hardwareMap.get(DcMotor.class, "transfer");
        slideOne = hardwareMap.get(DcMotor.class, "slide1");
        slideTwo = hardwareMap.get(DcMotor.class, "slide2");

        slideOne.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slideTwo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        slideOne.setPower(slidepower);
        slideTwo.setPower(slidepower);

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);

        intake.setDirection(DcMotorSimple.Direction.REVERSE);
        slideOne.setTargetPosition(slidePos);
        slideTwo.setTargetPosition(-slidePos);

        slideOne.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideTwo.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        waitForStart();

        moveForward(forwardPower);
        sleep(500);

        turnRight(turnPower);
        sleep(490);

        moveForward(-forwardPower);
        sleep(300);



        stopDrive();

        slide (2300);

        sleep(5000);

        door.setPosition(0.3);
        sleep (2000);
        door.setPosition(0);
        sleep(500);
        slide (1);
        sleep(4000);



    }

    private void moveForward(double speed){
        leftFront.setPower(speed);
        rightFront.setPower(speed);
        leftBack.setPower(speed);
        rightBack.setPower(speed);
    }
    private void turnRight (double speed) {
        leftFront.setPower(speed);
        rightFront.setPower(-speed);
        leftBack.setPower(speed);
        rightBack.setPower(-speed);
    }

    private void stopDrive(){
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftBack.setPower(0);
        rightBack.setPower(0);
    }

    private void slide (int pos) {
        slideOne.setTargetPosition(pos);
        slideTwo.setTargetPosition(-pos);
    }

}
