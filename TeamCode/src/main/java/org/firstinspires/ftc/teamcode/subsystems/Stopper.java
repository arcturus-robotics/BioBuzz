package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.robot.Robot;

@Config
public class Stopper {
    private final Servo stopper;


    public static double STOPPER_CLOSED = 0.0;
    public static double STOPPER_OPEN = 0.9;

    public Stopper(Robot robot) {
        stopper = robot.hardwareMap.get(Servo.class, "stopper");
        stopper.setPosition(STOPPER_CLOSED);
    }


    public void setOpen(boolean open) {
        stopper.setPosition(open ? STOPPER_OPEN : STOPPER_CLOSED);
    }
}