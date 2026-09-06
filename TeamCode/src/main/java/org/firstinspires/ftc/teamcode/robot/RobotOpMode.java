package org.firstinspires.ftc.teamcode.robot;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import static com.pedropathing.ivy.Scheduler.schedule;

public abstract class RobotOpMode extends OpMode {
    protected Robot robot;

    @Override
    public void init() {
        robot = new Robot(this);
        Scheduler.reset();
        schedule(robot.colorDetection.periodic());
    }

    @Override
    public void init_loop() {
        Scheduler.execute();
    }

    @Override
    public void start() {
        schedule(robot.drivetrain.periodic());
    }

    @Override
    public void loop() {
        Scheduler.execute();
        robot.telemetry.update();
    }
}