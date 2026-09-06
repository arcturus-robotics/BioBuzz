package org.firstinspires.ftc.teamcode.extras;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
// --- GAMEPAD 1 ---
// Left Stick Y/X      : Drive (axial / lateral)
// Right Stick X       : Yaw (rotate)
// Right Trigger       : Turbo mode (2x speed)
// Left Trigger        : Slow mode (1/3 speed)
// B                   : Hold to ALIGN to AprilTag (Limelight fine alignment only)
// Left Bumper (LB)    : Toggle turret auto-tracking ON/OFF
// Y                   : Blocker UP
// X                   : Blocker DOWN
// (A, RB, RT, D-Pad are unused on GP1)
//
// --- GAMEPAD 2 ---
// Y                   : Toggle launchwheel ON/OFF
// A                   : Intake + Transfer (shoot/feed)
// D-Pad Up            : Outtake intake + transfer (reverse both)
// D-Pad Down          : Intake only (forward)
// Left Trigger (LT)   : Hood DOWN (decrements position)
// X                   : Transfer in only
// Left Bumper (LB)    : Hood UP (increments position)
// Right Bumper (RB)   : Transfer out only
// D-Pad Right         : Turret rotate RIGHT (manual mode only)
// D-Pad Left          : Turret rotate LEFT  (manual mode only)

@Config
@TeleOp(name = "V3ThirdCodeRed", group = "ArtemisDecode")
public class RoboGeorgia extends OpMode {

    // ── Drive ──────────────────────────────────────────────────────────────
    private DcMotor leftFront, rightFront, leftBack, rightBack;

    // ── Mechanisms ─────────────────────────────────────────────────────────
    private DcMotorEx launchWheel;
    private DcMotor   intakeWheels;
    private DcMotor   transferWheel;
    private DcMotorEx turret;
    private Servo     hood;
    private Servo     blocker;
    private boolean lastTogglePressed = false;

    private Limelight3A limelight;
    private double  distance  = -1;
    private boolean tagVisible = false;


    public static double LAUNCHWHEEL_VELOCITY_LOW = 1400;
    public static double LAUNCHWHEEL_VELOCITY_HIGH = 4000;
    public static double LAUNCHWHEEL_VELOCITY_MID = 1500;

    public static double TRANSFER_POWER       = 0.6;
    private static final double INTAKE_POWER  = 1.0;
    public static double TURRET_POWER         = 0.9;
    public static double TURRET_MIN           = -600;
    public static double TURRET_MAX           =  600;

    public static double HOOD_START    = 0.2;
    public static double HOOD_STEP     = 0.01;
    public static double HOOD_MIN      = 0.25;
    public static double HOOD_MAX      = 0.85;
    public static double BLOCKER_START = 0.3;
    public static double BLOCKER_STEP  = 0.01;
    public static double BLOCKER_MIN   = 0; //blocker is on/BLOCKING
    public static double BLOCKER_MAX   = 0.9; //blocker is off/NOT BLOCKING

    public static double TARGET_OFFSET_DEGREES = -2.0;
    public static double TURN_GAIN             = 0.02;
    public static double MAX_AUTO_TURN         = 0.4;
    public static double ALIGNMENT_TOLERANCE   = 1.0;
    public static double MIN_TURN_POWER        = 0.08;
    public static double DISTANCE_CALIBRATION_K = 150.0;

    public static int TARGET_TAG_ID = 24; // RED alliance tag

    private static final double STICK_DEADZONE = 0.08;


    private double  hoodPosition    = HOOD_START;
    private double  blockerPosition = BLOCKER_START;
    private boolean launchwheelOn   = false;
    private boolean previousYState  = false;
    private boolean previousBState = false;
    private boolean launchwheelMid    = false;
    private boolean previousLBState2  = false;
    private boolean launchwheelOn2 = false;
    private boolean manualOn = false;
    private double  lastLoopTime    = 0;
    private String blockerStatus;
    private ElapsedTime loopTimer = new ElapsedTime();
    public static double SHOOTER_P = 20;
    public static double SHOOTER_I = 0.0;
    public static double SHOOTER_D = 0.00007;
    public static double SHOOTER_F = 13.9;


    @Override
    public void init() {

        // Drive motors
        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Mechanisms
        launchWheel   = hardwareMap.get(DcMotorEx.class, "launchWheel");
        intakeWheels  = hardwareMap.get(DcMotor.class,   "intakeWheels");
        transferWheel = hardwareMap.get(DcMotor.class,   "transferWheel");
        turret        = hardwareMap.get(DcMotorEx.class, "turret");
        hood          = hardwareMap.get(Servo.class,     "hood");
        blocker       = hardwareMap.get(Servo.class,     "blocker");
        limelight     = hardwareMap.get(Limelight3A.class, "Limelight");

        launchWheel.setDirection(DcMotorSimple.Direction.FORWARD);
        launchWheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        intakeWheels.setDirection(DcMotor.Direction.REVERSE);
        intakeWheels.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        transferWheel.setDirection(DcMotor.Direction.REVERSE);
        transferWheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        hood.setPosition(hoodPosition);
        blocker.setPosition(blockerPosition);


        telemetry.setMsTransmissionInterval(11);
        limelight.pipelineSwitch(0);
        limelight.start();


        lastLoopTime = System.nanoTime();

        telemetry.addLine("===== Turret Tracker Teleop [RED] =====");
        telemetry.addLine("GP1 LB     = Toggle turret auto-tracking");
        telemetry.addLine("GP1 B      = Hold to align to AprilTag");
        telemetry.addLine("GP1 RT     = Turbo | GP1 LT = Slow");
        telemetry.addLine("GP1 Y/X    = Blocker up/down");
        telemetry.addLine("GP2 Y      = Launchwheel toggle");
        telemetry.addLine("GP2 A      = Intake + Transfer");
        telemetry.addLine("GP2 DPad RIGHT/LEFT = Turret manual (when auto OFF)");
        telemetry.addLine("GP2 LB     = Hood up | GP2 LT = Hood down");
        telemetry.update();
    }

    @Override
    public void loop() {
        double axial   = -gamepad1.left_stick_y;
        double lateral =  gamepad1.left_stick_x;
        double yaw     =  gamepad1.right_stick_x;

        double speedMult = 1.0;
        if (gamepad1.right_trigger > 0.5) speedMult = 2.0;
        if (gamepad1.left_trigger  > 0.5) speedMult = 1.0 / 3.0;

        axial   *= speedMult;
        lateral *= speedMult;
        yaw     *= speedMult;

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


        if (gamepad2.left_stick_y < -0.5) {
            hoodPosition = Math.min(hoodPosition + HOOD_STEP, HOOD_MAX);
            hood.setPosition(hoodPosition);
        } else if (gamepad2.left_stick_y > 0.5) {
            hoodPosition = Math.max(hoodPosition - HOOD_STEP, HOOD_MIN);
            hood.setPosition(hoodPosition);
        }

        int turretPos = turret.getCurrentPosition();
        if (gamepad2.right_stick_x > 0.5 && turretPos < TURRET_MAX) {
            turret.setPower(TURRET_POWER);
        } else if (gamepad2.right_stick_x < -0.5 && turretPos > TURRET_MIN) {
            turret.setPower(-TURRET_POWER);
        } else {
            turret.setPower(0);
        }



        if (gamepad2.yWasPressed()) {
            launchwheelOn  = !launchwheelOn;
            launchwheelOn2 = false;
            launchwheelMid = false;
        } else if (gamepad2.bWasPressed()) {
            launchwheelOn2 = !launchwheelOn2;
            launchwheelOn  = false;
            launchwheelMid = false;
        } else if (gamepad2.leftBumperWasPressed()) {
            launchwheelMid = !launchwheelMid;
            launchwheelOn  = false;
            launchwheelOn2 = false;
        }

        if (launchwheelOn2) {
            launchWheel.setVelocity(LAUNCHWHEEL_VELOCITY_HIGH);
            if (!manualOn) hood.setPosition(0.85);

        } else if (launchwheelMid) {
            launchWheel.setVelocity(LAUNCHWHEEL_VELOCITY_MID);
            if (!manualOn) hood.setPosition(0.4);

        } else if (launchwheelOn) {
            launchWheel.setVelocity(LAUNCHWHEEL_VELOCITY_LOW);
            if (!manualOn) hood.setPosition(0.3);
        }

        if (manualOn) {
            hood.setPosition(hoodPosition);
        }

        if (gamepad2.a) {
            intakeWheels.setPower(INTAKE_POWER);
            transferWheel.setPower(TRANSFER_POWER);
            blocker.setPosition(BLOCKER_MIN);
        } else if (gamepad2.dpad_up) {
            intakeWheels.setPower(-INTAKE_POWER);
            transferWheel.setPower(-TRANSFER_POWER);
            blocker.setPosition(BLOCKER_MAX);
        } else {
            intakeWheels.setPower(0);
            transferWheel.setPower(0);
            blocker.setPosition(BLOCKER_MAX);
        }

        if (gamepad2.dpad_down) {
            intakeWheels.setPower(INTAKE_POWER);
        }

        if (gamepad2.x) {
            transferWheel.setPower(TRANSFER_POWER);
        } else if (gamepad2.right_bumper) {
            transferWheel.setPower(-TRANSFER_POWER);
        }


       /* if (gamepad1.y) {
            blockerPosition = Math.min(blockerPosition + BLOCKER_STEP, BLOCKER_MAX);
            blockerStatus = "ON";
        } else if (gamepad1.x) {
            blockerPosition = Math.max(blockerPosition - BLOCKER_STEP, BLOCKER_MIN);
            blockerStatus = "OFF";

        }*/

        blocker.setPosition(blockerPosition);


        double loopMs = loopTimer.milliseconds();
        loopTimer.reset();

    }

    private void updateLimelight() {
        LLResult result = limelight.getLatestResult();
        tagVisible = false;
        distance   = -1;

        if (result != null && result.isValid()) {
            for (LLResultTypes.FiducialResult fr : result.getFiducialResults()) {
                if (fr.getFiducialId() == TARGET_TAG_ID) {
                    tagVisible = true;
                    double area = fr.getTargetArea();
                    if (area > 0) {
                        distance = (DISTANCE_CALIBRATION_K / Math.sqrt(area)) / 40.0;
                    }
                    break;
                }
            }
        }
    }

    private double limelightHeadingError() {
        if (!tagVisible) return 0;
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) return 0;
        for (LLResultTypes.FiducialResult fr : result.getFiducialResults()) {
            if (fr.getFiducialId() == TARGET_TAG_ID) {
                return fr.getTargetXDegrees() - TARGET_OFFSET_DEGREES;
            }
        }
        return 0;
    }

    @Override
    public void stop() {
        launchWheel.setVelocity(0);
        turret.setPower(0);
        limelight.stop();
    }
}
//If the turret doesn't reach its target / is too slow
//→ Increase P
//If the turret overshoots and oscillates back and forth
//→ Decrease P, then increase D
//If the turret oscillates forever and never settles
//→ Increase D to dampen it
//If the turret settles close but never exactly on target (small persistent error)
//→ Increase I slightly (start with 0.0001 increments — it's very sensitive)
//If the turret is jerky/twitchy
//→ Decrease P, increase D
//If the turret moves fine but drifts over time
//→ Increase I slightly