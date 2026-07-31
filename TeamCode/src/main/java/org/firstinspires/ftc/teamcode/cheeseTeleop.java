// this is saying "this file is in package org.firstinspires.ftc.teamcode"
package org.firstinspires.ftc.teamcode;

//import statements, basically brings code from other files/libraries into this file.
//try deleting one of them and see what happens
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Cheese", group = "Pre-Season")
public class cheeseTeleop extends OpMode {

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

    public void init() {

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

        slideOne.setTargetPosition(slidePos);
        slideTwo.setTargetPosition(-slidePos);

        slideOne.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideTwo.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        slideOne.setPower(slidepower);
        slideTwo.setPower(slidepower);

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);

        intake.setDirection(DcMotorSimple.Direction.REVERSE);



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

        if (gamepad2.right_trigger_pressed){
            intake.setPower(1);
        } else if (gamepad2.square) {
            intake.setPower(-0.6);

        } else{
            intake.setPower(0);
        }

        if (gamepad2.left_trigger_pressed){
            transfer.setPower(1);
        }else if (gamepad2.triangle) {
            transfer.setPower(-0.6);
        }
        else{
            transfer.setPower(0);
        }

        if (gamepad2.right_bumper) {
            slidePos = maxSlide;
        } else if (gamepad2.left_bumper) {
            slidePos = minSlide;
        }
        if (gamepad2.left_stick_y >.5) {
            slidePos = slidePos+1 ;
        }
        if (gamepad2.left_stick_y <-.5) {
            slidePos = slidePos-1 ;
        }
        slideOne.setTargetPosition(slidePos);
        slideTwo.setTargetPosition(-slidePos);
        if(gamepad2.cross){
            door.setPosition(0);
        }
        if(gamepad2.circle){
            door.setPosition(0.5);
        }
        telemetry.addData("leftencoder",slideOne.getCurrentPosition());
        telemetry.addData("rightencoder",slideTwo.getCurrentPosition());
        telemetry.addData("slidetargetpos",slidePos);


    }
}
