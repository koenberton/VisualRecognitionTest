package visualRecognitionTest;


import generalpurpose.gpUtils;

import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class mainScreen {

	visualRecognitionController ctrl = null;
	gpUtils xU = null;
	
	private JFrame frame;
	private JTextField watsonStatus;
	private JLabel imageLabel = null;
	private JScrollPane scrollPaneLeft = null;
	private JButton btnLoad=null;
	private JButton btnWatson=null;
	private JButton btnCorrect=null;
	private JButton btnInCorrect=null;
	private JComboBox classifyCombo=null;
	private JTextArea textAreaLeft =null;

	
	private int leftMargin = 20;
	private int FRAMEBORDER = 35;
	private Rectangle frame_coord = new Rectangle(0,0,0,0);
	private int imageWidth = -1;
	private int imageHeigth = -1;
	private String currentFileName;
	private long startt=0L;
	
	private String ScriptName = "c:\\temp\\watsonTensorFlow.cmd";
	private String OutputName = "c:\\temp\\watsonTensorFlow.txt";
	private String ImageSourceFolder = "c:\\temp\\cmcProc";
	
	/**
	 * Launch the application.
	 */
	//---------------------------------------------------------------------------------
	public static void main(String[] args) 
	//---------------------------------------------------------------------------------
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainScreen window = new mainScreen();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	//---------------------------------------------------------------------------------
	public mainScreen() 
	//---------------------------------------------------------------------------------
	{
		xU = new gpUtils();
		//
		if( xU.ctSlash == '/') {
			ScriptName = "/tmp/watsonTensorFlow.cmd";
			OutputName = "/tmp/watsonTensorFlow.txt";
		    ImageSourceFolder = "/home/koen/beeldherkenning/fotoos" ;
	    }
		//
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	//---------------------------------------------------------------------------------
	private void initialize() 
	//---------------------------------------------------------------------------------
	{
		frame = new JFrame();
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				do_resize();
			}
		});
		frame.setTitle("VisualRecognitionTest V01 - 20Mar2017");
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		setFrameCoordinates( frame.getBounds() );
		//
		imageLabel = new JLabel("Choose Image");
		imageLabel.setBounds(getCoordinates("IMAGE"));
		frame.getContentPane().add(imageLabel);
		//
		scrollPaneLeft = new JScrollPane();
		scrollPaneLeft.setBounds( getCoordinates("LEFTTEXT"));
		frame.getContentPane().add(scrollPaneLeft);
	    //	
		textAreaLeft = new JTextArea();
		scrollPaneLeft.setViewportView(textAreaLeft);
		//
		watsonStatus = new JTextField("");
		watsonStatus.setBounds(getCoordinates("WATSONSTATUS"));
		frame.getContentPane().add(watsonStatus);
		watsonStatus.setEditable(false);
	    //
		classifyCombo = new JComboBox();
		classifyCombo.setModel(new DefaultComboBoxModel(new String[]{"Watson" , "Tensorflow"}));
		classifyCombo.setBounds(getCoordinates("COMBO"));
		classifyCombo.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              	//doClickImageFilterPrepareAndRun(); 
            }
        });
		frame.getContentPane().add(classifyCombo);
        //
		btnLoad = new JButton("Image");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				do_load();
			}
		});
		btnLoad.setBounds(getCoordinates("LOAD"));
		frame.getContentPane().add(btnLoad);
	    //	
		btnWatson = new JButton("Classify");
		btnWatson.setBounds(getCoordinates("CLASSIFY"));
		btnWatson.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				watsonStatus.setText("Classifying");
				ctrl = new visualRecognitionController(watsonStatus);
				ctrl.setOutputName(OutputName);
				ctrl.setScriptName(ScriptName);
				ctrl.setImageName(currentFileName);
				ctrl.setApplciation(classifyCombo.getSelectedItem().toString());
			    startt = System.nanoTime();
				ctrl.execute();
			}
		});
		frame.getContentPane().add(btnWatson);
		//
		btnCorrect = new JButton("Correct");
		btnCorrect.setBounds(getCoordinates("CORRECT"));
		btnCorrect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//tensorStatus.setText("Querying");
			}
		});
		frame.getContentPane().add(btnCorrect);
		//
		btnInCorrect = new JButton("Incorrect");
		btnInCorrect.setBounds(getCoordinates("Incorrect"));
		btnInCorrect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//tensorStatus.setText("Querying");
			}
		});
		frame.getContentPane().add(btnInCorrect);
		
		// Ticker
		ActionListener timerListener = new ActionListener(){
			  public void actionPerformed(ActionEvent event){
				  
				  if( watsonStatus != null ) {
					  String t = watsonStatus.getText();
					  if( t != null ) {
						  if( t.compareToIgnoreCase("Done") == 0 ) {
							  watsonStatus.setText("OK - now processing result");
							  show();
						  }
						  else
						  if( t.compareToIgnoreCase("Error") == 0 ) {
							  watsonStatus.setText("Oops an error occurred");
							  show();
						  }
					  }
				  }
			  }
		};
		Timer displayTimer = new Timer(200, timerListener);
		displayTimer.start();
	}
	
	//---------------------------------------------------------------------------------
	private void do_load()
	//---------------------------------------------------------------------------------
	{
		    currentFileName=null;
		    String FF = FileChooser(ImageSourceFolder,false);
		    if( FF == null ) return;
	        imageWidth  = -1;
	        imageHeigth = -1;
		    BufferedImage image = null;
	        try
	        {
	          image = ImageIO.read(new File(FF));
	          imageWidth = image.getWidth();
	          imageHeigth  = image.getHeight();
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	          System.err.println("oeps");
	        }
			ImageIcon imageIcon = new ImageIcon(image);
			imageLabel.setIcon(imageIcon);
			imageLabel.setText("");
			frame.setTitle(FF + " [" + imageWidth + "x" + imageHeigth + "]");
			do_resize();
			imageLabel.repaint();
			currentFileName=FF;
	}
	
	//---------------------------------------------------------------------------------
	private void do_resize()
	//---------------------------------------------------------------------------------
	{
		if( this.frame.isShowing() == false ) {
			System.out.println("Wait"); 
			return;
		}
		setFrameCoordinates(frame.getBounds());
		//
		imageLabel.setBounds(getCoordinates("IMAGE"));
		//
		watsonStatus.setBounds(getCoordinates("WATSONSTATUS"));
	    classifyCombo.setBounds(getCoordinates("COMBO"));
		//
		scrollPaneLeft.setBounds( getCoordinates("LEFTTEXT"));
		//
		btnLoad.setBounds(getCoordinates("LOAD"));
		btnWatson.setBounds(getCoordinates("CLASSIFY"));
		btnCorrect.setBounds(getCoordinates("CORRECT"));
		btnInCorrect.setBounds(getCoordinates("INCORRECT"));
	}
	
	//---------------------------------------------------------------------------------
	private void setFrameCoordinates(Rectangle v)
	//---------------------------------------------------------------------------------
	{
		frame_coord.setBounds( 0 , 0 , (int)(v.getWidth()) , (int)(v.getHeight()) );
	}
	
	//---------------------------------------------------------------------------------
	private Rectangle getCoordinates(String obj)
	//---------------------------------------------------------------------------------
	{
		Rectangle r = new Rectangle(10,10,10,10);
		int BUTTONSPACE = 3;
		int UNDERMARGIN = 10;
		int RAND = 10;
		int TINY = 4;
		
		if( obj.compareToIgnoreCase("COMBO") == 0 ) {
			r.x = leftMargin;
			r.height = 27;
			r.width = 130;
			r.y = (int)frame_coord.getHeight() - UNDERMARGIN - r.height - FRAMEBORDER;
			//System.out.println( obj + r.toString());
			return r;
		}
		if( obj.compareToIgnoreCase("LOAD") == 0 ) {
			Rectangle temp = getCoordinates("COMBO");
			r.x = temp.x + temp.width + BUTTONSPACE;
			r.height = temp.height;
			r.width = temp.width;
			r.y = temp.y;
			//System.out.println( obj + r.toString());
			return r;
		}
		if( obj.compareToIgnoreCase("CLASSIFY") == 0 ) {
			Rectangle temp = getCoordinates("LOAD");
			r.x = temp.x + temp.width + BUTTONSPACE;
			r.height = temp.height;
			r.width = temp.width;
			r.y = temp.y;
			//System.out.println( obj + r.toString());
			return r;
		}
		if( obj.compareToIgnoreCase("CORRECT") == 0 ) {
			Rectangle temp = getCoordinates("LOAD");
			r.x = temp.x + (temp.width + BUTTONSPACE)*2;
			r.height = temp.height;
			r.width = temp.width;
			r.y = temp.y;
			//System.out.println( obj + r.toString());
			return r;
		}
		if( obj.compareToIgnoreCase("INCORRECT") == 0 ) {
			Rectangle temp = getCoordinates("LOAD");
			r.x = temp.x + (temp.width + BUTTONSPACE)*3;
			r.height = temp.height;
			r.width = temp.width;
			r.y = temp.y;
			//System.out.println( obj + r.toString());
			return r;
		}
		if( obj.compareToIgnoreCase("WATSONSTATUS") == 0 ) {
			Rectangle temp = getCoordinates("COMBO");
			int freeWidth  = (int)(frame_coord.getWidth() - (2*leftMargin) - RAND);  // 10 = dikte rand
			r.x = temp.x;
			r.height = 25;
			r.width = freeWidth;
			r.y = temp.y - UNDERMARGIN - r.height;
			//System.out.println( obj + r.toString());
			return r;
		}
		if( obj.compareToIgnoreCase("LEFTTEXT") == 0 ) {
			Rectangle temp = getCoordinates("WATSONSTATUS");
			int freeHeight = (int)(frame_coord.getHeight() - FRAMEBORDER - UNDERMARGIN);
			//int freeWidth  = (int)(frame_coord.getWidth() - (3*leftMargin) - RAND);  // 10 = dikte rand
			r.x = temp.x;
			r.height = freeHeight / 4;
			r.y = temp.y - TINY - r.height;
			r.width = temp.width;
			//System.out.println( obj + r.toString());
			return r;
		}
		if( obj.compareToIgnoreCase("IMAGE") == 0 ) {
			Rectangle temp = getCoordinates("LEFTTEXT");
			Rectangle temp2 = getCoordinates("LOAD");
			Rectangle temp3 =getCoordinates("WATSONSTATUS");
			int remainingHeight = (int)(frame_coord.getHeight() - temp.height - (UNDERMARGIN*5) - FRAMEBORDER - temp2.height - temp3.height);
			r.width = (int)((frame_coord.getWidth() * 2 ) / 3);
			r.height = remainingHeight;
			r.y = UNDERMARGIN;
			r.x = (int)(frame_coord.getWidth() - r.width) / 2;
			if( imageWidth != -1 ) {
				if( imageWidth < r.width  ) {
					r.width = imageWidth;
					r.x = (int)((frame_coord.getWidth() - r.width)/2);
				}
				if( imageHeigth < r.height ) {
					r.height = imageHeigth;
					r.y = ((remainingHeight - r.height) / 2) + UNDERMARGIN;
				}
			}
			return r;
		}
		System.err.println("Unknow object" + obj);
		return r;
	}
	
	//---------------------------------------------------------------------------------
	private String FileChooser(String sDir , boolean FolderOnly)
	//---------------------------------------------------------------------------------
	{
     String FileName = null;
	 JFileChooser fc = new JFileChooser(sDir);
	 if( FolderOnly ) fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	 int result = fc.showOpenDialog(null);
     if( result == JFileChooser.APPROVE_OPTION ) {
    	 File f = fc.getSelectedFile();
    	 FileName = f.getAbsolutePath();
     }
     return FileName;
	}
	
	//---------------------------------------------------------------------------------
	private void show()
	//---------------------------------------------------------------------------------
	{
		String list = xU.ReadContentFromFile(OutputName, 1000, "ASCII");
		textAreaLeft.setText(list);
		String res = determineResponse(list);
		long tt = (System.nanoTime() - startt ) / 1000000L;
		if ( res ==  null ) res = "Error - unknown";
		if( res.trim().toUpperCase().startsWith("ERROR") == false ) res += "  [ " + tt + " msec ]";
		watsonStatus.setText(res);
	}

	//---------------------------------------------------------------------------------
	private String determineResponse(String sin)
	//---------------------------------------------------------------------------------
	{
		String tipe = classifyCombo.getSelectedItem().toString();
		if( tipe == null ) tipe = "Unknown";
		if( tipe.trim().compareToIgnoreCase("WATSON")==0) {
			// denug
			sin += "\n" + "\"class\" : \"televisie\" ";
			return getWatsonResult(sin);		
    	}
		else
		if( tipe.trim().compareToIgnoreCase("TENSORFLOW")==0) {
			    sin += "\n" + " roses (score = 0000";
			    return getTensorFlowResult(sin);
		}	
		else return "Error : Could not determine calssifier type";	
	}

	//---------------------------------------------------------------------------------
	private String getWatsonResult(String sin)
	//---------------------------------------------------------------------------------
	{   // look for first occurence of "class":"xxx"
		int naant=xU.TelDelims(sin,'\n');
		for(int i=0;i<=naant;i++)
		{
			String sLine = xU.GetVeld(sin,(i+1),'\n');
			if( sLine == null ) continue;
			sLine = xU.Remplaceer(sLine," ","");
			int j = sLine.indexOf("\"class\":");
			if( j < 0 ) continue;
			j = sLine.indexOf(":");
			sLine = sLine.substring(j);
			sLine = xU.Remplaceer(sLine,"\"","");
			sLine = xU.Remplaceer(sLine,",","");
			sLine = xU.Remplaceer(sLine,".","");
			sLine = xU.Remplaceer(sLine,":","");
			return sLine;
		}
		return "Error : Could not locate \"class\":\"xxx\"";
	}

	//---------------------------------------------------------------------------------
	private String getTensorFlowResult(String sin)
	//---------------------------------------------------------------------------------
	{   // look for first occurence of "(score"
		int naant=xU.TelDelims(sin,'\n');
		for(int i=0;i<=naant;i++)
		{
			String sLine = xU.GetVeld(sin,(i+1),'\n');
			if( sLine == null ) continue;
			sLine = xU.Remplaceer(sLine," ","");
			int j = sLine.indexOf("(score");
			if( j < 0 ) continue;
			return sLine.substring(0,j);
		}
		return "Error : Could not locate (score=";
	}
}
