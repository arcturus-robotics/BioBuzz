package org.firstinspires.ftc.teamcode.extras;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;


@Autonomous(name = "PepporoniAuto", group = "ArtemisDecode")
public class AutoBumbleBall extends LinearOpMode {

    private DcMotor slide;
    private Servo bucket;


    private static final int SLIDE_MAX_POSITION = 3000;
    private static final double SLIDE_POWER = 0.8;
    private static final int SLIDE_TIMEOUT_MS = 4000;

    private static final double BUCKET_REST_POS = 0.0;
    private static final double BUCKET_SCORE_POS = 1.0;

    private static final long DUMP_WAIT_MS = 1000;
    private static final long BUCKET_MOVE_SETTLE_MS = 500;

    private final ElapsedTime timer = new ElapsedTime();

    @Override
    public void runOpMode() {

        slide = hardwareMap.get(DcMotor.class, "slide");
        bucket = hardwareMap.get(Servo.class, "bucket");

        // TODO: If the slide doesn't move (or drives into a hard stop and stalls),
        // this is very likely the wrong direction. Try REVERSE instead.
        slide.setDirection(DcMotorSimple.Direction.FORWARD);

        slide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        slide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        bucket.setPosition(BUCKET_REST_POS);

        telemetry.addData("Status", "Initialized - bucket loaded, ready to raise slide");
        telemetry.addData("Slide Start Pos", slide.getCurrentPosition());
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {

            raiseSlideToMax();

            bucket.setPosition(BUCKET_SCORE_POS);
            sleep(BUCKET_MOVE_SETTLE_MS);

            sleep(DUMP_WAIT_MS);

            bucket.setPosition(BUCKET_REST_POS);
            sleep(BUCKET_MOVE_SETTLE_MS);

            lowerSlideToZero();

            telemetry.addData("Status", "Auto complete");
            telemetry.addData("Final Slide Pos", slide.getCurrentPosition());
            telemetry.update();
        }
    }

    private void raiseSlideToMax() {
        slide.setTargetPosition(SLIDE_MAX_POSITION);
        slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slide.setPower(SLIDE_POWER);

        timer.reset();
        while (opModeIsActive()
                && slide.isBusy()
                && timer.milliseconds() < SLIDE_TIMEOUT_MS) {
            telemetry.addData("Slide Action", "Raising");
            telemetry.addData("Slide Target", SLIDE_MAX_POSITION);
            telemetry.addData("Slide Pos", slide.getCurrentPosition());
            telemetry.addData("Slide Power (actual)", slide.getPower());
            telemetry.addData("Slide Busy", slide.isBusy());
            telemetry.addData("Elapsed ms", timer.milliseconds());
            telemetry.update();
        }

        if (timer.milliseconds() >= SLIDE_TIMEOUT_MS) {
            telemetry.addData("WARNING", "Slide raise TIMED OUT before reaching target");
            telemetry.addData("Stopped at Pos", slide.getCurrentPosition());
            telemetry.update();
            sleep(1500); // pause so the warning is actually visible on the DS
        }

        // Don't re-apply power here; RUN_TO_POSITION + BRAKE already holds position.
    }

    private void lowerSlideToZero() {
        slide.setTargetPosition(0);
        slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slide.setPower(SLIDE_POWER);

        timer.reset();
        while (opModeIsActive()
                && slide.isBusy()
                && timer.milliseconds() < SLIDE_TIMEOUT_MS) {
            telemetry.addData("Slide Action", "Lowering");
            telemetry.addData("Slide Target", 0);
            telemetry.addData("Slide Pos", slide.getCurrentPosition());
            telemetry.addData("Slide Power (actual)", slide.getPower());
            telemetry.addData("Slide Busy", slide.isBusy());
            telemetry.addData("Elapsed ms", timer.milliseconds());
            telemetry.update();
        }

        if (timer.milliseconds() >= SLIDE_TIMEOUT_MS) {
            telemetry.addData("WARNING", "Slide lower TIMED OUT before reaching target");
            telemetry.addData("Stopped at Pos", slide.getCurrentPosition());
            telemetry.update();
            sleep(1500);
        }

        slide.setPower(0);
    }
}