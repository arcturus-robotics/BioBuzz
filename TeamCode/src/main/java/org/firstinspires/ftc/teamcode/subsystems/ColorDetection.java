package org.firstinspires.ftc.teamcode.subsystems;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.pedropathing.ivy.Command;

import org.firstinspires.ftc.teamcode.robot.Robot;

import java.util.List;
public class ColorDetection {
    private Follower follower;
    private final Limelight3A limelight;
    private static final int YELLOW_BALL_PIPELINE = 0;
    private boolean ballDetected = false;
    private double tx = 0;
    private double ty = 0;
    private double ta = 0;
    private final Command yellowBallDetection;

    public ColorDetection(Robot robot) {

        limelight = robot.hardwareMap.get(Limelight3A.class, "Limelight");
        limelight.pipelineSwitch(YELLOW_BALL_PIPELINE);
        limelight.start();

        yellowBallDetection = Command.build()
                .setExecute(() -> {
                    LLResult result = limelight.getLatestResult();

                    ballDetected = false;
                    tx = 0;
                    ty = 0;
                    ta = 0;

                    if (result != null && result.isValid()) {
                        List<LLResultTypes.ColorResult> colorTargets = result.getColorResults();
                        if (colorTargets != null && !colorTargets.isEmpty()) {
                            LLResultTypes.ColorResult ball = colorTargets.get(0);
                            ballDetected = true;
                            tx = ball.getTargetXDegrees();
                            ty = ball.getTargetYDegrees();
                            ta = ball.getTargetArea();
                        }
                    }
                })
                .setDone(() -> false)
                .requiring(limelight);


    }
    public boolean isBallDetected() {
        return ballDetected;
    }
    public double getTx() {
        return tx;
    }
    public double getTy() {
        return ty;
    }
    public double getTa() {
        return ta;
    }
    public Command periodic() {
        return yellowBallDetection;
    }
}