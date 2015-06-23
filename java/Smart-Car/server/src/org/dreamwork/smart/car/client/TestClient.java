package org.dreamwork.smart.car.client;

import org.dreamwork.smart.car.server.io.Command;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by seth.yang on 2015/6/9.
 */
public class TestClient extends JFrame implements ActionListener {
    private JButton btnForward;
    private JButton btnLeft;
    private JButton btnBackward;
    private JButton btnRight;
    private JButton btnStop;
    private JPanel root;
    private JLabel txtStatus;
    private JButton btnQuit;
    private JButton btnConnection;
    private JToggleButton btnSpeed1;
    private JToggleButton btnSpeed2;
    private JToggleButton btnSpeed3;
    private JToggleButton btnSpeed4;
    private JToggleButton btnSpeed5;

    private JToggleButton btnLED;
    private JToggleButton btnLeftLed;
    private JToggleButton btnRightLed;
    private JToggleButton btnCamera;
    private JButton btnServoLeft;
    private JButton btnServoUp;
    private JButton btnServoRight;
    private JButton btnServoDown;
    private JButton btnLF;
    private JButton btnLB;
    private JButton btnRF;
    private JButton btnRB;
    private JButton btnLP;
    private JButton btnRP;
    private JButton btnReset;
    private JButton btnShutdown;
    private VideoPanel video;

    private JFrame videoFrame;

    private RemoteCar car;

    private Socket socket;
    private OutputStream out;

    private Map<AbstractButton, Command> commandMap = new HashMap<AbstractButton, Command> ();

    public TestClient () throws IOException {
        super ("Remote Car Control");

        guiSetup ();
        pack ();
        setVisible (true);
        setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);
    }

    private void guiSetup () throws IOException {
        setContentPane (root);

        // control
        commandMap.put (btnForward, Command.FORWARD);
        commandMap.put (btnBackward, Command.BACKWARD);
        commandMap.put (btnLeft, Command.TURN_LEFT);
        commandMap.put (btnRight, Command.TURN_RIGHT);
        commandMap.put (btnStop, Command.STOP);

        // Servo && Camera
        commandMap.put (btnServoDown, Command.SERVO_DOWN);
        commandMap.put (btnServoLeft, Command.SERVO_LEFT);
        commandMap.put (btnServoRight, Command.SERVO_RIGHT);
        commandMap.put (btnServoUp, Command.SERVO_UP);
        commandMap.put (btnCamera, Command.TOGGLE_CAMERA);

        // LEDs
        commandMap.put (btnLeftLed, Command.TOGGLE_LEFT_BLINK);
        commandMap.put (btnRightLed, Command.TOGGLE_RIGHT_BLINK);
        commandMap.put (btnLED, Command.TOGGLE_LED);

        // speeds
        commandMap.put (btnSpeed1, Command.SPEED);
        commandMap.put (btnSpeed2, Command.SPEED);
        commandMap.put (btnSpeed3, Command.SPEED);
        commandMap.put (btnSpeed4, Command.SPEED);
        commandMap.put (btnSpeed5, Command.SPEED);

        // debugs
        commandMap.put (btnLF, Command.LEFT_FORWARD);
        commandMap.put (btnLB, Command.LEFT_BACKWARD);
        commandMap.put (btnLP, Command.LEFT_PAUSE);
        commandMap.put (btnRF, Command.RIGHT_FORWARD);
        commandMap.put (btnRB, Command.RIGHT_BACKWARD);
        commandMap.put (btnRP, Command.RIGHT_PAUSE);

        for (AbstractButton button : commandMap.keySet ()) {
            button.addActionListener (this);
        }
        btnQuit.addActionListener (this);
        btnConnection.addActionListener (this);
        btnReset.addActionListener (this);
        btnShutdown.addActionListener (this);

/*
        btnForward.addActionListener (this);
        btnLeft.addActionListener (this);
        btnBackward.addActionListener (this);
        btnRight.addActionListener (this);
        btnStop.addActionListener (this);

        btnSpeed1.addActionListener (this);
        btnSpeed2.addActionListener (this);
        btnSpeed3.addActionListener (this);
        btnSpeed4.addActionListener (this);
        btnSpeed5.addActionListener (this);
        btnLeftLed.addActionListener (this);
        btnRightLed.addActionListener (this);
        btnServoLeft.addActionListener (this);
        btnServoUp.addActionListener (this);
        btnServoRight.addActionListener (this);
        btnServoDown.addActionListener (this);
        btnLED.addActionListener (this);
        btnCamera.addActionListener (this);
        btnLF.addActionListener (this);
        btnLB.addActionListener (this);
        btnLP.addActionListener (this);
        btnRF.addActionListener (this);
        btnRB.addActionListener (this);
        btnRP.addActionListener (this);
*/

        ButtonGroup group1 = new ButtonGroup ();
        group1.add (btnSpeed1);
        group1.add (btnSpeed2);
        group1.add (btnSpeed3);
        group1.add (btnSpeed4);
        group1.add (btnSpeed5);
        btnSpeed3.setSelected (true);

        setButtonStatus (false);
    }

    private void connect () throws IOException {
        new Thread () {
            @Override
            public void run () {
                try {
                    btnConnection.setEnabled (false);
                    txtStatus.setText ("finding remote car ... ");
                    if (car == null) {
                        car = findCar ();
                        if (car == null) {
                            txtStatus.setText ("Can't find remote car.");
                            btnConnection.setEnabled (true);

                            return;
                        }
                    }
                    socket = new Socket (car.getIp (), 18000);
                    out = socket.getOutputStream ();
                    txtStatus.setText ("connected: " + car.getIp ().getHostAddress ());
                    setButtonStatus (true);
                } catch (Exception ex) {
                    ex.printStackTrace ();
                }
            }
        }.start ();
    }

    private RemoteCar findCar () throws IOException, InterruptedException {
        return RemoteCar.find (8001);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        AbstractButton button = (AbstractButton) e.getSource ();
        try {
            if (button == btnQuit) {
                sendCommand (Command.QUIT);
                socket.close ();
                socket = null;
                setButtonStatus (false);
            } else if (button == btnConnection) {
                if (socket == null)
                    connect ();
                else if (socket.isConnected ()) {
                    sendCommand (Command.QUIT);
                    Thread.sleep (1000);
                    socket.close ();

                    connect ();
                }
            } else if (button == btnReset) {
                sendCommand (Command.RESET);
            } else if (button == btnShutdown) {
                sendCommand (Command.DISPOSE);
            } else if (button == btnSpeed1) {
                sendSpeed (1);
            } else if (button == btnSpeed2) {
                sendSpeed (2);
            } else if (button == btnSpeed3) {
                sendSpeed (3);
            } else if (button == btnSpeed4) {
                sendSpeed (4);
            } else if (button == btnSpeed5) {
                sendSpeed (5);
            } else if (button == btnCamera) {
                if (videoFrame == null || !videoFrame.isVisible ()) {
                    showVideo ();
                } else {
                    hideVideo ();
                }
            } else if (commandMap.containsKey (button)) {
                sendCommand (commandMap.get (button));
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }

    private void sendCommand (Command command) throws IOException {
        byte[] buff = new byte[4];
        buff [0] = (byte) ((command.code >> 24) & 0xff);
        buff [1] = (byte) ((command.code >> 16) & 0xff);
        buff [2] = (byte) ((command.code >>  8) & 0xff);
        buff [3] = (byte) ((command.code) & 0xff);
        out.write (buff);
        out.flush ();
    }

    private void sendSpeed (int speed) throws IOException {
        sendCommand (Command.SPEED);
        out.write (speed);
        out.flush ();
    }

    private void setButtonStatus (boolean status) {
        for (AbstractButton button : commandMap.keySet ()) {
            button.setEnabled (status);
        }
        btnQuit.setEnabled (status);
        btnShutdown.setEnabled (status);
        btnReset.setEnabled (status);
        btnConnection.setEnabled (!status);
    }

    public static void main (String[] args) throws IOException {
        new TestClient ();
    }

    private void showVideo () throws IOException {
        if (videoFrame == null) {
            videoFrame = new JFrame ("Remote Car Video");
            video = new VideoPanel (car.getIp (), car.getCameraPort ());
            videoFrame.getContentPane ().add (video);
            videoFrame.setResizable (false);
            videoFrame.addWindowListener (new WindowAdapter () {
                @Override
                public void windowClosing (WindowEvent e) {
                    System.out.println ("closing");
                    super.windowClosed (e);
                    video.disconnect ();
                    btnCamera.setSelected (false);
                }

                @Override
                public void windowOpened (WindowEvent e) {
                    System.out.println ("opened");
                    super.windowOpened (e);
                    try {
                        video.connect ();
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace ();
                    }
                    video.play ();
                }

                @Override
                public void windowActivated (WindowEvent e) {
                    video.play ();
                }

                @Override
                public void windowIconified (WindowEvent e) {
                    video.pause ();
                }
            });
        }
        Point location = this.getLocation ();
        Dimension dim = this.getSize ();
        videoFrame.setLocation (location.x + dim.width + 2, location.y);
        videoFrame.setVisible (true);
        videoFrame.pack ();
        sendCommand (Command.TOGGLE_CAMERA);
    }

    private void hideVideo () throws IOException {
        videoFrame.setVisible (false);
        video.disconnect ();
        sendCommand (Command.TOGGLE_CAMERA);
    }
}