
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;

import static com.pedropathing.ivy.Scheduler.schedule;

@TeleOp(name = "Simple Drive (Ivy)")
public class SimpleDriveTeleop extends LinearOpMode {
    private DcMotor frontLeft, frontRight, backLeft, backRight;
    private double speedMultiplier = 1.0;


    @Override
    public void runOpMode() {

        Scheduler.reset();

        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        Command drive = Command.build()
                .setExecute(() -> {
                    double forward = -gamepad1.left_stick_y * speedMultiplier;
                    double strafe  =  gamepad1.left_stick_x * speedMultiplier;
                    double turn    =  gamepad1.right_stick_x * speedMultiplier;

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


        waitForStart();

        schedule(drive);

        while (opModeIsActive()) {

            if (gamepad1.left_bumper) {
                schedule(slowMode);
            } else if (gamepad1.right_bumper) {
                schedule(turboMode);
            } else {
                speedMultiplier = 1.0;
            }

            Scheduler.execute();

            telemetry.addData("Speed Multiplier", speedMultiplier);
            telemetry.update();
        }
    }
}

//

 