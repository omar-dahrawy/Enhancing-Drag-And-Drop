package com.dahrawy.EnhancingDragAndDrop;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.jnativehook.NativeHookException;
import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

public class Recognizer extends JFrame {

	private static final long serialVersionUID = 1L;
	private File folderDirectory;
	private String folderName;
	private JFileChooser folderCh;
	private File[] folderContents;
	private JPanel panel;
	private JPanel pnl;
	private JProgressBar pb;
	private JFrame pbFrame;
	private int progress = 0;
	private ArrayList<File> images;
	private DropPane dp;

	private String modelpath;
	private byte[] graphDef;

	private DropPane dp1;
	private DropPane dp2;
	private DropPane dp3;
	private DropPane dp4;
	private String result = "Image contains:\n";
	private ArrayList<ArrayList<String>> classes = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> operations = new ArrayList<ArrayList<String>>();

	private JPanel container = new JPanel();
	private JPanel container2 = new JPanel();
	JFrame containerFrame = new JFrame("");
	JFrame containerFrame2 = new JFrame("");

	private InputStream inStream = null;
	private OutputStream outStream = null;


	private MouseInputListener mouseInputListener; 
	private KeyListener keyListener; 

	private List<String> labels;

	private MouseListener mouseListener1;
	private MouseListener mouseListener2;

	public Recognizer() throws IOException, InterruptedException, NativeHookException {
		createMouseListener1();
		createMouseListener2();

		mouseInputListener = new MouseInputListener();
		keyListener = new KeyListener();

		modelpath = "/Users/omar_aldahrawy/Desktop/GUC/Semester 8/Enhancing Drag & Drop/inception_dec_2015";
		graphDef = readAllBytesOrExit(Paths.get(modelpath, "graph.pb"));
		labels = readAllLinesOrExit(Paths.get(modelpath, "labels.txt"));
		
		containerFrame.add(new JScrollPane(container));
		containerFrame2.add(new JScrollPane(container2));

		setSize(200, 200);
		setLayout(new GridLayout(2, 2));
		setAlwaysOnTop(true);

		JLabel copy = new JLabel("Copy");
		JLabel cut = new JLabel("Cut");
		JLabel copyRec = new JLabel("<html>Copy &<br/>Recognize</html>");
		JLabel cutRec = new JLabel("<html>Cut &<br/>Recognize</html>");
		copy.setHorizontalAlignment(JLabel.CENTER);
		cut.setHorizontalAlignment(JLabel.CENTER);
		copyRec.setHorizontalAlignment(JLabel.CENTER);
		cutRec.setHorizontalAlignment(JLabel.CENTER);

		dp1 = new DropPane();
		dp2 = new DropPane();
		dp3 = new DropPane();
		dp4 = new DropPane();
		dp1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		dp2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		dp3.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		dp4.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		dp1.add(copy);
		dp2.add(cut);
		dp3.add(copyRec);
		dp4.add(cutRec);

		add(dp1);
		add(dp3);
		add(dp2);
		add(dp4);

		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		while(true) {
			if(keyListener.getShortcut()) {
				container2.removeAll();
				container2.updateUI();
				if(containerFrame2.isVisible()) {
					containerFrame2.setVisible(false);
				}
				if(!containerFrame.isVisible()) {
					showContainter();
				}
			}

			while(containerFrame.isVisible()){
				setVisible(false);
			}

			while(containerFrame2.isVisible()) {
				setVisible(false);
				if(keyListener.getShortcut()) {
					container2.removeAll();
					container2.updateUI();
					containerFrame2.setVisible(false);
					if(!containerFrame.isVisible()) {
						showContainter();
					}
				}
			}

			if((getLocation().x != (int)mouseInputListener.mouseX-100) || (getLocation().y != (int)mouseInputListener.mouseY-100)) {
				setLocation((int)mouseInputListener.mouseX-100, (int)mouseInputListener.mouseY-100);
			}

			if(!mouseInputListener.released && !isVisible()) {
				setVisible(true);
			}

			if(dp1.getFlag()) {
				setVisible(false);
				copy(dp1);
			}
			else if(dp2.getFlag()) {
				setVisible(false);
				cut(dp2);
			}
			else if(dp3.getFlag()) {
				setVisible(false);
				copyRec(dp3);
			}
			else if(dp4.getFlag()) {
				setVisible(false);
				cutRec(dp4);
			}
		}
	}

	public void checkArrays() {
		for(int i = 0 ; i < classes.size() ; i++) {
			for(int j = 0 ; j < classes.get(i).size() ; j++) {
				if(classes.get(i).get(j).equals("")) {
					classes.get(i).remove(j);
					j--;
				}
			}
			if(classes.get(i).size() == 1) {
				classes.remove(i);
				i--;
			}
		}
	}
	
	public void showContainter() throws IOException {
		container2.removeAll();
		container2.updateUI();
		checkArrays();
		if(classes.isEmpty()) {
			JOptionPane.showMessageDialog(null, "Container is empty. Drag files to the tray\nto view them in the container.", "Warning", JOptionPane.PLAIN_MESSAGE);
		}
		else {
			container.updateUI();
			container.setLayout(new GridLayout(1, classes.size()));
			for(int i = 0 ; i < classes.size() ; i++) {
				JPanel panel = new JPanel();
				panel.setLayout(new GridLayout(2, 3));
				panel.add(new JLabel(""));
				JLabel className = new JLabel(classes.get(i).get(0));
				className.setHorizontalAlignment(JLabel.CENTER);
				panel.add(className);
				panel.add(new JLabel(""));
				getClassImages(panel, i);
				panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
				panel.addMouseListener(mouseListener1);
				container.add(panel);
				panel.updateUI();
				container.updateUI();
			}
			containerFrame.setSize(700, 200);
			containerFrame.setLocationRelativeTo(null);
			containerFrame.setVisible(true);
		}
	}

	public void getClassImages(JPanel panel , int i) throws IOException {
		int j = 1;
		if(classes.get(i).size() == 2) {
			panel.add(new JLabel(""));
		}
		for( ; j <= 3 && j < classes.get(i).size() ; j++) {
			if(!classes.get(i).get(j).equals("")) {
				File file = new File(classes.get(i).get(j));
				Image img = ImageIO.read(file);
				JLabel imglbl = new JLabel();
				int length = 0;
				int width = 0;
				if(classes.size() >= 3) {
					length = 700/(3*3);
					width = 700/(3*3);
				}
				else if(classes.size() < 3) {
					if(700/(classes.size()*3) > 80) {
						length = 80;
						width = 80;
					}
					else {
						length = 700/(classes.size()*3);
						width = 700/(classes.size()*3);
					}
				}
				imglbl.setIcon(new ImageIcon(img.getScaledInstance(length,width,100)));
				imglbl.setHorizontalAlignment(JLabel.CENTER);
				panel.add(imglbl);
			}
		}
		if(classes.get(i).size() == 3 || classes.get(i).size() == 2) {
			panel.add(new JLabel(""));
		}
	}

	public void createContainer2(String className) throws IOException {
		container2.removeAll();
		container2.updateUI();
		for(int i = 0 ; i < classes.size() ; i++) {
			int rows = 0;
			if(classes.get(i).get(0).equals(className)) {
				if(classes.get(i).size() <= 9) {
					rows = 3;
				}
				else {
					if((classes.get(i).size()-1) % 3 == 0) {
						rows = ((int)(classes.get(i).size()-1)/3);
					}
					else{
						rows = (((int)(classes.get(i).size()-1)/3)+1);
					}
				}
				container2.setLayout(new GridLayout(rows, 3));
				int j = 1;
				for( ; j < classes.get(i).size() ; j++) {
					if(!classes.get(i).get(j).equals("")) {
						File file = new File(classes.get(i).get(j));
						Image img = ImageIO.read(file);
						JLabel imglbl = new JLabel();
						imglbl.setIcon(new ImageIcon(img.getScaledInstance(80,80,300)));
						imglbl.setHorizontalAlignment(JLabel.CENTER);
						imglbl.addMouseListener(mouseListener2);
						imglbl.setToolTipText(classes.get(i).get(j));
						imglbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
						container2.add(imglbl);
					}
				}
				for( ; j <= rows*3 ; j++) {
					JLabel tmp = new JLabel("");
					tmp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
					container2.add(tmp);
				}
				break;
			}
		}
		container2.updateUI();
		containerFrame2.setSize(new Dimension(300, 300));
		containerFrame2.setResizable(false);
		containerFrame2.setLocationRelativeTo(null);
		containerFrame.setVisible(false);
		containerFrame2.setVisible(true);
	}

	public void completeOperation(File file , String operation) throws IOException {
		inStream = new FileInputStream(file);
		outStream = new FileOutputStream(new File("/Users/omar_aldahrawy/Desktop/Test/" + file.getName()));
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inStream.read(buffer)) > 0){
			outStream.write(buffer, 0, length);
		}
		inStream.close();
		outStream.close();

		if(operation.equals("cut")) {
			file.delete();
		}

	}

	public void createMouseListener1() {
		mouseListener1 = new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				container.removeAll();
				container.updateUI();
				String className = ((JLabel)(((JPanel)arg0.getSource()).getComponent(1))).getText();
				try {
					createContainer2(className);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
	}

	public void createMouseListener2() {
		mouseListener2 = new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				String path = ((JLabel)arg0.getSource()).getToolTipText();
				int tmp = 0;
				for(int i = 0 ; i < classes.size() ; i++) {
					if(classes.get(i).contains(path)) {
						tmp = classes.get(i).indexOf(path)-1;
						break;
					}
				}
				((JLabel)container2.getComponent(tmp)).setIcon(null);;
				container2.updateUI();
				String operation = "";
				for(int i = 0 ; i < classes.size() ; i++) {
					if(classes.get(i).contains(path)) {
						operation = operations.get(i).get(tmp+1);
						classes.get(i).set(classes.get(i).indexOf(path), "");
						break;
					}
				}
				try {
					completeOperation(new File(path) , operation);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

	public void copy(DropPane dp) throws IOException {
		List<?> files = ((List<?>)(dp.getFiles()));
		for(int x = 0 ; x < files.size() ; x++) {
			File file = (File)(files.get(x));
			if(classes.isEmpty()) {
				classes.add(new ArrayList<String>());
				classes.get(0).add("Not Classified");
				classes.get(0).add(file.getAbsolutePath());
				operations.add(new ArrayList<String>());
				operations.get(0).add("Not Classified");
				operations.get(0).add("copy");
				
			}
			else {
				for(int j = 0 ; j < classes.size() ; j++) {
					if(classes.get(j).get(0).equals("Not Classified")) {
						classes.get(j).add(file.getAbsolutePath());
						operations.get(j).add("copy");
						break;
					}
					else if(j == classes.size()-1) {
						classes.add(new ArrayList<String>());
						classes.get(classes.size()-1).add("Not Classified");
						classes.get(classes.size()-1).add(file.getAbsolutePath());
						operations.add(new ArrayList<String>());
						operations.get(classes.size()-1).add("Not Classified");
						operations.get(classes.size()-1).add("copy");
						break;
					}
				}
			}
//			for(int i = 0 ; i < classes.size() ; i++) {
//				if(classes.get(i).contains("Not Classified")) {
//					if(classes.get(i).size() < 4) {
//						for(int c = 0 ; x < container.getComponentCount() ; x++) {
//							if(((JLabel)((JPanel)container.getComponent(c)).getComponent(1)).getText().equals("Not Classified")) {
//								System.out.println("Howa da");
//								System.out.println("Not Classified");
//							}
//						}
//					}
//				}
//			}
//			showContainter();
		}
	}

	public void cut(DropPane dp) throws IOException {
		List<?> files = ((List<?>)(dp.getFiles()));
		for(int x = 0 ; x < files.size() ; x++) {
			File file = (File)(files.get(x));
			if(classes.isEmpty()) {
				classes.add(new ArrayList<String>());
				classes.get(0).add("Not Classified");
				classes.get(0).add(file.getAbsolutePath());
				operations.add(new ArrayList<String>());
				operations.get(0).add("Not Classified");
				operations.get(0).add("cut");
			}
			else {
				for(int j = 0 ; j < classes.size() ; j++) {
					if(classes.get(j).get(0).equals("Not Classified")) {
						classes.get(j).add(file.getAbsolutePath());
						operations.get(j).add("cut");
						break;
					}
					else if(j == classes.size()-1) {
						classes.add(new ArrayList<String>());
						classes.get(classes.size()-1).add("Not Classified");
						classes.get(classes.size()-1).add(file.getAbsolutePath());
						operations.add(new ArrayList<String>());
						operations.get(classes.size()-1).add("Not Classified");
						operations.get(classes.size()-1).add("cut");
						break;
					}
				}
			}
		}
	}

	public void copyRec(DropPane dp) throws IOException {
		List<?> files = ((List<?>)(dp.getFiles()));
		for(int x = 0 ; x < files.size() ; x++) {
			File file = (File)(files.get(x));
			byte[] imageBytes = readAllBytesOrExit(Paths.get(file.getAbsolutePath()));
			try (Tensor<?> image = Tensor.create(imageBytes)) {
				float[] labelProbabilities = executeInceptionGraph(graphDef, image);
				for(int i = 0 ; i < 3 ; i++) {
					int bestLabelIdx = maxIndex(labelProbabilities);							
					//String order = "";
					switch(i){
					case 0:
						//order = "(most likely)";
						if(classes.isEmpty()) {
							classes.add(new ArrayList<String>());
							classes.get(0).add(labels.get(bestLabelIdx));
							classes.get(0).add(file.getAbsolutePath());
							operations.add(new ArrayList<String>());
							operations.get(0).add(labels.get(bestLabelIdx));
							operations.get(0).add("copy");
						}
						else {
							for(int j = 0 ; j < classes.size() ; j++) {
								if(classes.get(j).get(0).equals(labels.get(bestLabelIdx))) {
									classes.get(j).add(file.getAbsolutePath());
									operations.get(j).add("copy");
									break;
								}
								else if(j == classes.size()-1) {
									classes.add(new ArrayList<String>());
									classes.get(classes.size()-1).add(labels.get(bestLabelIdx));
									classes.get(classes.size()-1).add(file.getAbsolutePath());
									operations.add(new ArrayList<String>());
									operations.get(classes.size()-1).add(labels.get(bestLabelIdx));
									operations.get(classes.size()-1).add("copy");
									break;
								}
							}
						}
						break;
					case 1:
						//order = "2nd";
						break;
					case 2:
						//order = "3rd";
					}
					//result += String.format("%s "+order, labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f);
					//result += "\n";
					labelProbabilities[bestLabelIdx] = 0;
				}
				//JOptionPane.showMessageDialog(null, result, file.getName(), JOptionPane.PLAIN_MESSAGE);
				//result = "";
			}
		}
	}

	public void cutRec(DropPane dp) throws IOException {
		List<?> files = ((List<?>)(dp.getFiles()));
		for(int x = 0 ; x < files.size() ; x++) {
			File file = (File)(files.get(x));
			byte[] imageBytes = readAllBytesOrExit(Paths.get(file.getAbsolutePath()));
			try (Tensor<?> image = Tensor.create(imageBytes)) {
				float[] labelProbabilities = executeInceptionGraph(graphDef, image);
				for(int i = 0 ; i < 3 ; i++) {
					int bestLabelIdx = maxIndex(labelProbabilities);							
					//String order = "";
					switch(i){
					case 0:
						//order = "(most likely)";
						if(classes.isEmpty()) {
							classes.add(new ArrayList<String>());
							classes.get(0).add(labels.get(bestLabelIdx));
							classes.get(0).add(file.getAbsolutePath());
							operations.add(new ArrayList<String>());
							operations.get(0).add(labels.get(bestLabelIdx));
							operations.get(0).add("cut");
						}
						else {
							for(int j = 0 ; j < classes.size() ; j++) {
								if(classes.get(j).get(0).equals(labels.get(bestLabelIdx))) {
									classes.get(j).add(file.getAbsolutePath());
									operations.get(j).add("cut");
									break;
								}
								else if(j == classes.size()-1) {
									classes.add(new ArrayList<String>());
									classes.get(classes.size()-1).add(labels.get(bestLabelIdx));
									classes.get(classes.size()-1).add(file.getAbsolutePath());
									operations.add(new ArrayList<String>());
									operations.get(classes.size()-1).add(labels.get(bestLabelIdx));
									operations.get(classes.size()-1).add("cut");
									break;
								}
							}
						}
						break;
					case 1:
						//order = "2nd";
						break;
					case 2:
						//order = "3rd";
					}
					//result += String.format("%s "+order, labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f);
					//result += "\n";
					labelProbabilities[bestLabelIdx] = 0;
				}
				//JOptionPane.showMessageDialog(null, result, file.getName(), JOptionPane.PLAIN_MESSAGE);
				//result = "";
			}
		}
	}

	public void dp(DropPane dp) {
		List<?> files = ((List<?>)(dp.getFiles()));
		if(files.size() == 1) {
			File file = (File)(files.get(0));
			byte[] imageBytes = readAllBytesOrExit(Paths.get(file.getAbsolutePath()));
			try (Tensor<?> image = Tensor.create(imageBytes)) {
				float[] labelProbabilities = executeInceptionGraph(graphDef, image);
				for(int i = 0 ; i < 3 ; i++) {
					int bestLabelIdx = maxIndex(labelProbabilities);							
					String order = "";
					switch(i){
					case 0:
						order = "(most likely)";
						if(classes.isEmpty()) {
							classes.add(new ArrayList<String>());
							classes.get(0).add(labels.get(bestLabelIdx));
							classes.get(0).add(file.getName());
						}
						else {
							for(int j = 0 ; j < classes.size() ; j++) {
								if(classes.get(j).get(0).equals(labels.get(bestLabelIdx))) {
									classes.get(j).add(file.getName());
									break;
								}
								else if(j == classes.size()-1) {
									classes.add(new ArrayList<String>());
									classes.get(classes.size()-1).add(labels.get(bestLabelIdx));
									classes.get(classes.size()-1).add(file.getName());
								}
							}
						}
						break;
					case 1:
						order = "";
						break;
					case 2:
						order = "";
					}
					result += String.format("%s "+order, labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f);
					result += "\n";
					labelProbabilities[bestLabelIdx] = 0;
				}
				JOptionPane.showMessageDialog(null, result, file.getName(), JOptionPane.PLAIN_MESSAGE);
				result = "";
			}
		}
		else if(files.size() > 1) {
			int response = JOptionPane.showConfirmDialog(null, "You selected " + files.size() + " files.\nDo you want to classify them?", "" + "Files Select", JOptionPane.YES_NO_CANCEL_OPTION);
			if(response == JOptionPane.YES_OPTION) {
				for(int x = 0 ; x < files.size() ; x++) {
					File file = (File)(files.get(x));
					byte[] imageBytes = readAllBytesOrExit(Paths.get(file.getAbsolutePath()));
					try (Tensor<?> image = Tensor.create(imageBytes)) {
						float[] labelProbabilities = executeInceptionGraph(graphDef, image);
						for(int i = 0 ; i < 3 ; i++) {
							int bestLabelIdx = maxIndex(labelProbabilities);							
							String order = "";
							switch(i){
							case 0:
								order = "(most likely)";
								if(classes.isEmpty()) {
									classes.add(new ArrayList<String>());
									classes.get(0).add(labels.get(bestLabelIdx));
									classes.get(0).add(file.getName());
								}
								else {
									for(int j = 0 ; j < classes.size() ; j++) {
										if(classes.get(j).get(0).equals(labels.get(bestLabelIdx))) {
											classes.get(j).add(file.getName());
											break;
										}
										else if(j == classes.size()-1) {
											classes.add(new ArrayList<String>());
											classes.get(classes.size()-1).add(labels.get(bestLabelIdx));
											classes.get(classes.size()-1).add(file.getName());
											break;
										}
									}
								}
								break;
							case 1:
								order = "2nd";
								break;
							case 2:
								order = "3rd";
							}
							result += String.format("%s "+order, labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f);
							result += "\n";
							labelProbabilities[bestLabelIdx] = 0;
						}
						JOptionPane.showMessageDialog(null, result, file.getName(), JOptionPane.PLAIN_MESSAGE);
						result = "";
					}
				}
			}
			else if(response == JOptionPane.NO_OPTION) {
				for(int x = 0 ; x < files.size() ; x++) {
					File file = (File)(files.get(x));
					if(classes.isEmpty()) {
						classes.add(new ArrayList<String>());
						classes.get(0).add("Not Classified");
						classes.get(0).add(file.getName());
					}
					else {
						for(int j = 0 ; j < classes.size() ; j++) {
							if(classes.get(j).get(0).equals("Not Classified")) {
								classes.get(j).add(file.getName());
								break;
							}
							else if(j == classes.size()-1) {
								classes.add(new ArrayList<String>());
								classes.get(classes.size()-1).add("Not Classified");
								classes.get(classes.size()-1).add(file.getName());
								break;
							}
						}
					}			
				}
			}
			else if(response == JOptionPane.CANCEL_OPTION) {
				//
			}
		}
	}

	private static float[] executeInceptionGraph(byte[] graphDef, Tensor<?> image) {
		try (Graph g = new Graph()) {
			g.importGraphDef(graphDef);
			try (Session s = new Session(g);
					Tensor<?> result = s.runner().feed("DecodeJpeg/contents", image).fetch("softmax").run().get(0)) {
				final long[] rshape = result.shape();
				if (result.numDimensions() != 2 || rshape[0] != 1) {
					throw new RuntimeException(
							String.format(
									"Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
									Arrays.toString(rshape)));
				}
				int nlabels = (int) rshape[1];
				return result.copyTo(new float[1][nlabels])[0];
			}
		}
	}

	private static int maxIndex(float[] probabilities) {
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}

	private static byte[] readAllBytesOrExit(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			System.err.println("Failed to read [" + path + "]: " + e.getMessage());
			System.exit(1);
		}
		return null;
	}

	private static List<String> readAllLinesOrExit(Path path) {
		try {
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {
			System.err.println("Failed to read [" + path + "]: " + e.getMessage());
			System.exit(0);
		}
		return null;
	}

	static class GraphBuilder {

		GraphBuilder(Graph g) {
			this.g = g;
		}

		Output<?> div(Output<?> x, Output<?> y) {
			return binaryOp("Div", x, y);
		}

		Output<?> sub(Output<?> x, Output<?> y) {
			return binaryOp("Sub", x, y);
		}

		Output<?> resizeBilinear(Output<?> images, Output<?> size) {
			return binaryOp("ResizeBilinear", images, size);
		}

		Output<?> expandDims(Output<?> input, Output<?> dim) {
			return binaryOp("ExpandDims", input, dim);
		}

		Output<?>  cast(Output<?> value, DataType dtype) {
			return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
		}

		Output<?>  decodeJpeg(Output<?> contents, long channels) {
			return g.opBuilder("DecodeJpeg", "DecodeJpeg")
					.addInput(contents)
					.setAttr("channels", channels)
					.build()
					.output(0);
		}

		Output<?> constant(String name, Object value) {
			try (Tensor<?> t = Tensor.create(value)) {
				return g.opBuilder("Const", name)
						.setAttr("dtype", t.dataType())
						.setAttr("value", t)
						.build()
						.output(0);
			}
		}

		private Output<?> binaryOp(String type, Output<?> in1, Output<?> in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
		}

		private Graph g;
	}

	public static void main(String[] args) throws IOException, InterruptedException, NativeHookException {
		new Recognizer();
	}


}
