import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class VideoPlayer{
    static String input = "11 64 n 1 162 252 421 508 897 n 1081 1132 1179 1352 n 2332 2460 2583 2717 3148 3244 3263 n 3303 n 3547 3571 3621 3729 3737 3771 3848 3879 3991 4023 4053 4082 4129 4232 4347 4492 4724 n 4844 n 5600 5755 5952 6141 n 6303 6857 6969 n 7048 n 7458 7592 7669 7836 7840 7844 7877 7891 7900 7906 n 8081 8115 8178 8269 8290 8296 8302 8312 8369 8511";
    static String[] inputParts = input.split(" ");
    //System.out.println(inputParts[0]);
    //System.out.println("Size of inputParts length"+ inputParts.length);
    //input preprocessing
    static int nscenes=Integer.parseInt(inputParts[0]), ntotal=Integer.parseInt(inputParts[1]);
    
    // default state - video playing
    static int isPause = 1;
    
    public static void main(String[] args) {
        File file = new File("./InputVideo.rgb"); // name of the RGB video file
        int width = 480; // width of the video frames
        int height = 270; // height of the video frames

        int frameWidth = 860; // window width
        int frameHeight = 540; // window height

        int fps = 30; // frames per second of the video
        int numFrames = 6276; // number of frames in the video
        
//System.out.println(input);
//System.out.println(nscenes);
//System.out.println(ntotal);
//System.out.println("IP Length"+inputParts.length);
        // create the JFrame and JLabel to display the video
        JFrame frame = new JFrame("Video Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setSize(new Dimension(frameWidth, frameHeight));
        frame.setVisible(true);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        frame.add(label);

        // left container displaying video structure
        JPanel leftPanel = new JPanel();
        //leftPanel.setLayout(new FlowLayout());
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));


        JTextArea textArea = new JTextArea(20, 20);
        JScrollPane scrollableTextArea = new JScrollPane(textArea);

        scrollableTextArea.setBounds(0, 0, frameWidth / 3, frameHeight);
        scrollableTextArea.setPreferredSize(new Dimension(frameWidth / 3, frameHeight));

        scrollableTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

/*JTextField skipTimeField = new JTextField(5);
        JButton skipButton = new JButton("SKIP");
        leftPanel.add(skipTimeField);
        leftPanel.add(skipButton);*/
        
        // right container displaying video playing
        JPanel rightPanel = new JPanel();
        rightPanel.setBounds(0,0, 2 * frameWidth / 3, frameHeight);
        rightPanel.setPreferredSize(new Dimension(2 * frameWidth / 3, frameHeight));
        rightPanel.setLayout(new BorderLayout());

        JLabel labImage = new JLabel();
        labImage.setBounds(0,0, frameWidth / 3, 2 * frameHeight/3);
        labImage.setPreferredSize(new Dimension(frameWidth / 3, 2 * frameHeight/3));
        labImage.setOpaque(true);

        // bottom container displaying interactive buttons to play, pause, stop
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        

        JButton jb1 = new JButton("PLAY");
        JButton jb2 = new JButton("PAUSE");
        JButton jb3 = new JButton("STOP");

        // action listener for play event
        // anonymous action listener replaced with lambda
        jb1.addActionListener(e -> isPause = 1);

        // action listener for pause event
        jb2.addActionListener(e -> isPause = 0);

        // action listener for stop event
        jb3.addActionListener(e -> isPause = -1);

        bottomPanel.add(jb1);
        bottomPanel.add(jb2);
        bottomPanel.add(jb3);


        //skipButton = new JButton("Skip10");
        //skipButton.setActionCommand("10");
        //bottomPanel.add(skipButton);

JButton jbSkip[];
jbSkip = new JButton[ntotal];
JPanel buttonPanel = new JPanel();
buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
Color color = new Color(255, 255, 255); // blue color

//Border border = BorderFactory.createLineBorder(Color.WHITE, 2);
int scene_counter = 1, shot_counter=1, button_counter=0;
for(int i1=2;i1<inputParts.length;i1++)
{
    if(inputParts[i1].equals("n"))
    {
        shot_counter=1;
        scene_counter++;
        //System.out.println(inputParts[i1]+"scene start");
    }
    else
    {
        //System.out.println(((Object)inputParts[i1]).getClass().getSimpleName()+inputParts[i1]+"here"+i1);
        if(inputParts[i1-1].equals("n"))
        {
            //scene
            //scene_counter++;
            jbSkip[button_counter] = new JButton("Scene"+scene_counter+"\n");
            leftPanel.add(jbSkip[button_counter]);
            jbSkip[button_counter].setActionCommand(Float.toString(Float.parseFloat(inputParts[i1])/30));
            jbSkip[button_counter].setForeground(color);
            jbSkip[button_counter].setForeground(new Color(0, 0, 0));
            jbSkip[button_counter].setFont(new Font("Arial", Font.BOLD, 16));
            //jbSkip[button_counter].addActionListener(skipActionListener);
        }
        else
        {
            //shot
            jbSkip[button_counter] = new JButton("Scene"+scene_counter+" Shot"+shot_counter);
            leftPanel.add(jbSkip[button_counter]);
            jbSkip[button_counter].setActionCommand(Float.toString(Float.parseFloat(inputParts[i1])/30));
            jbSkip[button_counter].setForeground(color);
            jbSkip[button_counter].setForeground(new Color(0, 0, 0));
            jbSkip[button_counter].setFont(new Font("Arial", Font.BOLD, 16));
            //jbSkip[button_counter].addActionListener(skipActionListener);
            shot_counter++;
        }
        button_counter++;
    }
    //System.out.println(button_counter+"Bcounter");
    //jbSkip[(i1/5)-1].setPreferredSize(new Dimension(50, 100));
}

        // right container style
        rightPanel.add(labImage, BorderLayout.NORTH);
        rightPanel.add(bottomPanel, BorderLayout.CENTER);
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        rightPanel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        // left container style
        

        //leftPanel.add(scrollableTextArea, BorderLayout.WEST);
        JScrollPane scrollPane = new JScrollPane(leftPanel);
        frame.getContentPane().add(scrollPane, BorderLayout.WEST);

        //leftPanel.add(buttonPanel);
        
        //frame.getContentPane().add(scrollPane);
        //JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        //separator.setPreferredSize(new Dimension(100, 600));
        //leftPanel.add(separator);
        frame.add(rightPanel, BorderLayout.EAST);
    
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //frame.add(leftPanel, BorderLayout.WEST);
        //frame.add(separator);
                //frame.setSize(300, 300);

        frame.setVisible(true);

        

        // read the video file and display each frame
        int i = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(width * height * 3);
  
            do {

                try {
                    Thread.sleep(1000 / fps);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

// action listener for all buttons
ActionListener skipActionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        // get the button that was pressed
        JButton button = (JButton) e.getSource();
        String actionCommand = ((JButton) e.getSource()).getActionCommand();
        // check which button was pressed and skip to appropriate time
        //if (button == jbSkip5) {
            // skip 5 seconds
            try {
                double skipFrame = Float.parseFloat(actionCommand) * fps;
            long skipBytes = (long)(skipFrame * width * height * 3);
            channel.position(skipBytes);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        //}
    }
};
for(int k=0;k<button_counter;k++)
{
    jbSkip[k].addActionListener(skipActionListener);
}

                // if video is stopped repaint frame to empty and re-position video start
                if (isPause == -1) {

                    labImage.setIcon(null);
                    frame.validate();
                    frame.repaint();

                    try {
                        channel.position(0);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    continue;
                }

                // if video is paused repaint frame to empty and re-position video start
                if (isPause == 0)
                    continue;


                buffer.clear();
                try {
                    channel.read(buffer);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                buffer.rewind();
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int r = buffer.get() & 0xff;
                        int g = buffer.get() & 0xff;
                        int b = buffer.get() & 0xff;
                        int rgb = (r << 16) | (g << 8) | b;
                        image.setRGB(x, y, rgb);
                    }
                }
                labImage.setIcon(new ImageIcon(image));
                frame.validate();
                frame.repaint();

            } while (i < numFrames);
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
 
}
