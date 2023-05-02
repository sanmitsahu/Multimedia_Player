import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

// open source VLCJ library
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;

public class VideoPlayer extends JFrame {
    static String input = "11 64 n 1 162 252 421 508 897 n 1081 1132 1179 1352 n 2332 2460 2583 2717 3148 3244 3263 n 3303 n 3547 3571 3621 3729 3737 3771 3848 3879 3991 4023 4053 4082 4129 4232 4347 4492 4724 n 4844 n 5600 5755 5952 6141 n 6303 6857 6969 n 7048 n 7458 7592 7669 7836 7840 7844 7877 7891 7900 7906 n 8081 8115 8178 8269 8290 8296 8302 113000 143000 153000";
    static String[] inputParts = input.split(" ");
    
    //input preprocessing
    static int nscenes=Integer.parseInt(inputParts[0]), ntotal=Integer.parseInt(inputParts[1]);

    static int currentFrameWidth = 0;
    static int currentFrameHeight = 0;
    private static final long serialVersionUID = 1L;
    private static final String VIDEO_PATH = "InputVideo.mp4";
    static final CallbackMediaPlayerComponent mediaPlayerComponent = new CallbackMediaPlayerComponent();
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    static JPanel contentPane = new JPanel();

    public static void main(String[] args) throws FileNotFoundException {
            new NativeDiscovery().discover();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println(e);
        }

        VideoPlayer myVideoPlayer = new VideoPlayer();

        // to initialize content pane and control pane
        myVideoPlayer.initialize();
        myVideoPlayer.setVisible(true);

        // load video file
        myVideoPlayer.loadVideo(VIDEO_PATH);
        mediaPlayerComponent.mediaPlayer().controls().play();

        int frameWidth = 860; // window width
        int frameHeight = 540; // window height
        currentFrameWidth = frameWidth;
        currentFrameHeight = frameHeight;
        int fps = 30; // frames per second of the video

        System.out.println("new input audio step1");

        // main frame window
        JFrame frame = new JFrame("Video Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setSize(new Dimension(frameWidth, frameHeight));
        frame.setVisible(true);

        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();

        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        leftPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        JTextArea textArea = new JTextArea(20, 20);

        // scrollable area on the left side
        JScrollPane scrollableTextArea = new JScrollPane(textArea);
        scrollableTextArea.setBounds(0, 0, 0, frameHeight);
        scrollableTextArea.setPreferredSize(new Dimension(0, 0));

        scrollableTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollableTextArea.setBackground(Color.RED);

        JPanel videoPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(leftPanel);

        // right container displaying video playing
        rightPanel.setBounds(0, 0, 50, currentFrameHeight);
        rightPanel.setPreferredSize(new Dimension(currentFrameWidth - 180, currentFrameHeight));
        rightPanel.setLayout(new BorderLayout());

        videoPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        videoPanel.setPreferredSize(new Dimension(currentFrameWidth / 3, 2 * currentFrameHeight / 3));

        // frame resize handler to make the app responsive
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                currentFrameWidth = frame.getBounds().width;
                currentFrameHeight = frame.getBounds().height;
                rightPanel.setPreferredSize(new Dimension(currentFrameWidth - 180, currentFrameHeight));

            }
        });

        // left container
        JButton jbSkip[];
        jbSkip = new JButton[ntotal];
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        Color color = new Color(255, 255, 255);

        int scene_counter = 0, shot_counter = 1, button_counter = 0;
        for (int i1 = 2; i1 < inputParts.length; i1++) {
            if (inputParts[i1].equals("n")) {
                shot_counter = 1;
                scene_counter++;
            } else {
            	JPanel shotPanel = new JPanel();
            	
                if (inputParts[i1 - 1].equals("n")) {
                    shotPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                	jbSkip[button_counter] = new JButton("Scene "+scene_counter + "\n");
                    shotPanel.add(jbSkip[button_counter]);
                   jbSkip[button_counter].setActionCommand(Float.toString(Float.parseFloat(inputParts[i1])/30*1000));
                    //jbSkip[button_counter].setActionCommand("98000.00");
                    jbSkip[button_counter].setForeground(color);
                    jbSkip[button_counter].setForeground(new Color(0, 0, 0));
                    jbSkip[button_counter].setFont(new Font("Arial", Font.BOLD, 16));
                } else {
                    // shot
                	shotPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
                	JLabel shotLabel = new JLabel("   ");
                    jbSkip[button_counter] = new JButton("Shot "+shot_counter);
                    shotPanel.add(shotLabel);
                    shotPanel.add(jbSkip[button_counter]);
                    jbSkip[button_counter].setActionCommand(Float.toString(Float.parseFloat(inputParts[i1])/30*1000));
                    jbSkip[button_counter].setForeground(color);
                    jbSkip[button_counter].setForeground(new Color(0, 0, 0));
                    jbSkip[button_counter].setFont(new Font("Arial", Font.BOLD, 16));
                    shot_counter++;
                }
                leftPanel.add(shotPanel);
                button_counter++;
            }
        }

        rightPanel.add(contentPane);
        rightPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        rightPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        frame.getContentPane().add(scrollPane, BorderLayout.WEST);

        frame.add(rightPanel, BorderLayout.EAST);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // action listener for all buttons
        ActionListener skipActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String actionCommand = ((JButton) e.getSource()).getActionCommand();
                System.out.println(actionCommand);
                // mediaplayer handles Video & Audio together
                long skipTime = (long) Float.parseFloat(actionCommand);
                //mediaPlayer.controls().setTime(skipTime);
                mediaPlayerComponent.mediaPlayer().controls().setTime(skipTime);
                //mediaPlayerComponent.getMediaPlayer().skip(skipTime);
                //mediaPlayerComponent.getMediaPlayer().setTime(seekTimeInMillis);
                mediaPlayerComponent.mediaPlayer().controls().play();
            }
        };

        for (int k = 0; k < button_counter; k++) {
            jbSkip[k].addActionListener(skipActionListener);
        }

    }

    //	initialize content pane and control pane
    public void initialize() {
        this.setBounds(100, 100, 600, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });

        contentPane.setLayout(new BorderLayout());
        contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);
        JPanel controlsPane = new JPanel();
        playButton = new JButton("Play");
        controlsPane.add(playButton);
        pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        stopButton = new JButton("Stop");
        controlsPane.add(stopButton);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        // play button handler
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().play();
            }
        });

        // pause button handler
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().pause();
            }
        });
        // stop button handler
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mediaPlayerComponent.mediaPlayer().controls().stop();

            }
        });
        this.setContentPane(contentPane);
        this.setVisible(true);
    }

    public void loadVideo(String path) {
        mediaPlayerComponent.mediaPlayer().media().startPaused(path);
    }

}
