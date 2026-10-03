package org.firstinspires.ftc.teamcode;

import static com.pedropathing.ivy.Scheduler.schedule;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.robot.RobotOpMode;

// --- GAMEPAD 1 ---
// Left Stick / Right Stick X : Drive / turn
// Left Bumper                : Slow
// Right Bumper               : Turbo
//
// --- GAMEPAD 2 ---
// D-Pad Down (hold)    : Intake in
// D-Pad Up (hold)      : Intake out
// A (hold)             : Intake + transfer together
// Y (press)            : Outtake / shooter TOGGLE (press on, press again off)
// Right Bumper (hold)  : Stopper open (closes on release)

@TeleOp(name = "CoolTeleOp", group = "Competition")
@Config
public class TeleWithCoolStuff extends RobotOpMode {

    private boolean previousY = false;   // remembers Y from the last loop

    @Override
    public void init() {
        super.init();

        Scheduler.reset();
        schedule(robot.drivetrain.periodic());
    }

    @Override
    public void start() {
    }

    @Override
    public void loop() {

        // ---- Drive ----
        double forward = -gamepad1.left_stick_y;
        double strafe  =  gamepad1.left_stick_x;
        double turn    = -gamepad1.right_stick_x;
        robot.drivetrain.setInput(forward, strafe, turn);

        robot.drivetrain.setSpeedMode(gamepad1.left_bumper, gamepad1.right_bumper);

        // ---- Intake + transfer ----
        // A = intake AND transfer together
        // D-Pad Down = intake in only, D-Pad Up = intake out only
        boolean combined = gamepad2.a;
        robot.intake.setIntakeMode(combined || gamepad2.dpad_down, gamepad2.dpad_up);
        robot.transfer.setTransferMode(combined, false);

        // ---- Outtake: press Y once to turn on, press again to turn off ----
        if (gamepad2.y && !previousY) {
            robot.outtake.toggleShooter();
        }
        previousY = gamepad2.y;

        // ---- Stopper: hold right bumper to open, release to close ----
        robot.stopper.setOpen(gamepad2.right_bumper);

        // ---- Telemetry ----
        robot.telemetry.addData("Speed Multiplier", robot.drivetrain.getSpeedMultiplier());
        robot.telemetry.addData("Shooter", robot.outtake.isShooterActive() ? "ON" : "OFF");
        robot.telemetry.addData("Stopper", gamepad2.right_bumper ? "OPEN" : "CLOSED");

        super.loop();
    }
}