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
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner;

// open source VLCJ library
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;

public class VideoPlayer extends JFrame {
    //static String input = "11 64 n 1 162 252 421 508 897 n 1081 1132 1179 1352 n 2332 2460 2583 2717 3148 3244 3263 n 3303 n 3547 3571 3621 3729 3737 3771 3848 3879 3991 4023 4053 4082 4129 4232 4347 4492 4724 n 4844 n 5600 5755 5952 6141 n 6303 6857 6969 n 7048 n 7458 7592 7669 7836 7840 7844 7877 7891 7900 7906 n 8081 8115 8178 8269 8290 8296 8302 113000 143000 153000";
    static int currentFrameWidth = 0;
    static int currentFrameHeight = 0;
    private static final long serialVersionUID = 1L;
    private static final String VIDEO_PATH = "/Users/sanmitsahu/IdeaProjects/First/src/InputVideo.mp4";
    static final CallbackMediaPlayerComponent mediaPlayerComponent = new CallbackMediaPlayerComponent();
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    static JPanel contentPane = new JPanel();

    public static void main(String[] args) throws FileNotFoundException {
        String file_input = "";
        try {
            File myObj = new File("/Users/sanmitsahu/IdeaProjects/First/src/output.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                file_input+=data;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println("File Input"+file_input);
        //file_input = "17 64 7 n 1 162 252 421 n 508 897 n 1081 1132 1179 1352 -2011 n 2332 2460 2583 2717 -2896 3148 n 3244 3263 n 3303 -3390 3547 3571 3621 n 3729 3737 3771 n 3848 n 3879 3991 4023 4053 4082 4129 4232 n 4347 4492 -4571 4724 n 4844 -5124 n 5600 5755 5952 6141 n 6303 -6630 6857 n 6969 n 7048 -7144 7458 7592 n 7669 7836 7840 7844 7877 7891 7900 7906 8081 8115 8178 8269 8290 8296 8302 8312 n 8369 8511";
        //System.out.println("File Input"+file_input);
        String[] file_inputParts = file_input.split(" ");
        System.out.println("Length "+file_inputParts.length);
        String[] inputParts = new String[file_inputParts.length + Integer.parseInt(file_inputParts[0])];
        for(int k=0,l=0;k<file_inputParts.length;k++,l++)
        {
            if(file_inputParts[k].equals("n"))
            {
                inputParts[l] = file_inputParts[k];
                l++;
                inputParts[l] = file_inputParts[k+1];
            }
            else
            {
                inputParts[l] = file_inputParts[k];
            }
        }


        int nscenes=Integer.parseInt(inputParts[0]), ntotal=Integer.parseInt(inputParts[1])+Integer.parseInt(inputParts[2])+Integer.parseInt(inputParts[0]);


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
        //leftPanel.setSize(new Dimension(10, 10));
        //leftPanel.setPreferredSize(new Dimension(200, 400));
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

        int scene_counter = 0, shot_counter = 1, button_counter = 0, subshot_counter = 1;
        for (int i1 = 3; i1 < inputParts.length; i1++) {
            if (inputParts[i1].equals("n")) {
                //changed
                shot_counter = 1;
                subshot_counter = 1;
                scene_counter++;
            } else {
                //changed
                JPanel shotPanel = new JPanel();
                JLabel shotLabel = new JLabel("   ");
                JLabel subshotLabel = new JLabel("    ");

                if (inputParts[i1 - 1].equals("n")) {
                    shotPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                    jbSkip[button_counter] = new JButton("Scene "+scene_counter + "\n");
                    jbSkip[button_counter].setOpaque(true);
                    jbSkip[button_counter].setBackground(Color.BLACK);
                    jbSkip[button_counter].setForeground(Color.BLACK);

                    shotPanel.add(jbSkip[button_counter]);
                    jbSkip[button_counter].setActionCommand(Float.toString(Float.parseFloat(inputParts[i1])/30*1000));
                    //jbSkip[button_counter].setActionCommand("98000.00");
                    //jbSkip[button_counter].setForeground(color);
                    //jbSkip[button_counter].setForeground(new Color(64, 68, 74));
                    //jbSkip[button_counter].setBackground(Color.BLUE);
                    jbSkip[button_counter].setFont(new Font("Arial", Font.BOLD, 18));

                } else {
                    // shotif
                    //subshot
                    if(Float.parseFloat(inputParts[i1])<0.0)
                    {
                        shotPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
                        jbSkip[button_counter] = new JButton("SS" + subshot_counter);
                        shotPanel.add(subshotLabel);
                        shotPanel.add(subshotLabel);
                        shotPanel.add(jbSkip[button_counter]);

                        jbSkip[button_counter].setActionCommand(Float.toString(Float.parseFloat(inputParts[i1]) / 30 * 1000 *(-1)));
                        jbSkip[button_counter].setForeground(color);
                        jbSkip[button_counter].setForeground(new Color(0, 0, 0));
                        jbSkip[button_counter].setFont(new Font("Arial", Font.ITALIC, 14));
                        subshot_counter++;
                    }
                    else
                    {
                        subshot_counter = 1;
                        shotPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
                        jbSkip[button_counter] = new JButton("Shot" + shot_counter);

                        jbSkip[button_counter].setOpaque(true);
                        jbSkip[button_counter].setBackground(Color.LIGHT_GRAY);

                        shotPanel.add(shotLabel);
                        shotPanel.add(jbSkip[button_counter]);

                        jbSkip[button_counter].setActionCommand(Float.toString(Float.parseFloat(inputParts[i1]) / 30 * 1000));
                        jbSkip[button_counter].setForeground(color);
                        jbSkip[button_counter].setForeground(new Color(0, 0, 0));
                        jbSkip[button_counter].setFont(new Font("Arial", Font.BOLD, 16));
                        shot_counter++;
                    }

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
