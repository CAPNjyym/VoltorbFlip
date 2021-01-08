/* VoltorbFlip
	Version 2.2
	Finds the most likely solution to Voltorb Flip minigame
	Jyym Culpepper
*/

// add in "simplify + ?"
//	simplify also checks if any cards are known and, if so, includes/excludes the card in its count
// add in "check by row/column" (would be added into simplify)
// 	basically, find combinations that work for row 1, and do the same for every row and column
// 	combine these combinations to find possibilities
//	form:
//		rowCheck[i][j][k] / colCheck[i][j][k]
//		where i is row number, j is possibility index, k is column number
//		NOTE: At most, there will be 90 possibility indeces (for 8 points, 1 voltorb)
/*			Index count: (formula is close to a normal distribution)
			P	V	Count
			5	0	1
			6	0	5
			7	0	15
			8	0	30
			9	0	45
			10	0	51
			11	0	45
			12	0	30
			13	0	15
			14	0	5
			15	0	1
			4	1	5
			5	1	20
			6	1	50
			7	1	80
			8	1	95
			9	1	80
			10	1	50
			11	1	20
			12	1	5
			3	2	10
			4	2	30
			5	2	60
			6	2	70
			7	2	60
			8	2	30
			9	2	10
			2	3	10
			3	3	20
			4	3	30
			5	3	20
			6	3	10
			1	4	5
			2	4	5
			3	4	5
			0	5	1
*/
// add in checkboxes to disable checking certain numbers?
// add in an array (or arraylist) of previous possibilities--the number of results used to calculate chances


import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.StringTokenizer;
import java.util.Date;
import java.sql.Time;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class VoltorbFlip extends JFrame implements ActionListener{
	private static final String version = "2.2";
	private static final int overhead = 15, rate = 25900;
	private static final long serialVersionUID = 1L;

	private static final Color palette[] = {Color.red, Color.lightGray, Color.yellow, Color.green};
	private static final Font PointsVoltsFont = new Font("Arial", Font.BOLD, 18),
								ValFont = new Font("Arial", Font.BOLD, 13);
	
	private JPanel background;
	private JTextField ColumnPoints[], ColumnVolts[], RowPoints[], RowVolts[], Values[][];
	private JTextArea Recommend, Time;
	private JScrollPane RecommendScroll;
	private Button Calculate, Simplify, Reset, Timer;
	
	private int colPoints[], colVolts[], rowPoints[], rowVolts[], test[][], total;
	private double chances[][][];
	private long possibilities = 1, testCount;
	private boolean tested, possible[][][];
	
	public static void main(String[] args)
	{
		new VoltorbFlip();
	}
	
	public VoltorbFlip()
	{
		colPoints = new int[5];
		colVolts = new int[5];
		rowPoints = new int[5];
		rowVolts = new int[5];
		chances = new double[5][5][4];
		test = new int[5][5];
		possible = new boolean[5][5][4];
		
		background = new JPanel();
		background.setBounds(1024, 768, 200, 100);
		background.setLayout(null);
		background.setBackground(new Color(0, 64, 0));
		add(background);
		
		setTitle("Voltorb Flip Calculator v" + version);
		setSize(1024, 668);
		setBackground(Color.GREEN);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Clicking the "X" at the top-right corner closes the window
		
		placeBoxes();
		
		// Text Areas
		Recommend = new JTextArea("", 1, 1);
		Time = new JTextArea("", 1, 1);
		Recommend.setBounds(5, 350, 1000, 200);
		Time.setBounds(655, 600, 345, 50);
		Recommend.setEditable(false);
		Time.setFocusable(false);
		Recommend.setFont(ValFont);
		Time.setFont(ValFont);
		
		// Make Recommend scrollable
		RecommendScroll = new JScrollPane(Recommend, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		RecommendScroll.setBounds(5, 350, 1000, 200);
		
		// Buttons
		Calculate = new Button("Calculate");
		Simplify = new Button("Simplify");
		Reset = new Button("Reset");
		Timer = new Button("Estimate Time to Solve");
		Calculate.setBounds(5, 600, 140, 50);
		Simplify.setBounds(150, 600, 140, 50);
		Reset.setBounds(295, 600, 140, 50);
		Timer.setBounds(445, 600, 205, 50);
		Calculate.setEnabled(false);
		
		// Add ActionListeners
		Calculate.addActionListener(this);
		Simplify.addActionListener(this);
		Reset.addActionListener(this);
		Timer.addActionListener(this);
		
		// Add Everything
		//background.add(Recommend);
		background.add(RecommendScroll);
		background.add(Calculate);
		background.add(Simplify);
		background.add(Reset);
		background.add(Timer);
		background.add(Time);

		resetFields();
		enableGrid(false);
		
		setVisible(true);
	}
	
	private void placeBoxes()
	{
		int i, j;

		ColumnPoints = new JTextField[5];
		ColumnVolts = new JTextField[5];
		RowPoints = new JTextField[5];
		RowVolts = new JTextField[5];
		Values = new JTextField[5][5];
		
		for (i=0;i<5;i++)
		{
			ColumnPoints[i] = new JTextField();
			ColumnPoints[i].setBounds(185*i+65, 288, 50, 25);
			ColumnPoints[i].setFont(PointsVoltsFont);
			background.add(ColumnPoints[i]);
			
			ColumnVolts[i] = new JTextField();
			ColumnVolts[i].setBounds(185*i+65, 313, 50, 25);
			ColumnVolts[i].setFont(PointsVoltsFont);
			background.add(ColumnVolts[i]);

			RowPoints[i] = new JTextField();
			RowPoints[i].setBounds(950, 55*i+5, 50, 25);
			RowPoints[i].setFont(PointsVoltsFont);
			background.add(RowPoints[i]);
			
			RowVolts[i] = new JTextField();
			RowVolts[i].setBounds(950, 55*i+30, 50, 25);
			RowVolts[i].setFont(PointsVoltsFont);
			background.add(RowVolts[i]);
			
			for (j=0;j<5;j++)
			{
				Values[i][j] = new JTextField();
				Values[i][j].setBounds(185*j+5, 55*i+18, 180, 25);
				Values[i][j].setFont(ValFont);
				background.add(Values[i][j]);
			}
		}
	}
	
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("Calculate"))
		{
			long begin, end, time;
			Time now;
			
			try
			{
				begin = System.currentTimeMillis();
				System.out.println("Began: " + new Date(begin));
				
				readInput();
				estimate();
				calculate();
				printResults();
				
				end = System.currentTimeMillis();
				time = end - begin;
				now = new Time(18000000 + time); // 18000000 = 5 hours; 5 hours must be added because of EST offset from GMT
				System.out.println("Ended: " + new Date(end));
				System.out.println("Results: " + total + "/" + testCount + " in " + now + "(" + time + ")");
				//System.out.println(total + "\t" + testCount + "\t" + time);
				System.out.println();
			}
			catch (Exception f)
				{System.out.println("Calculation Aborted");}
		}
		else if (e.getActionCommand().equals("Simplify"))
		{
			try{
				readInput();
				simplify();
				readInput();
				estimate();
				enableGrid(true);
				Calculate.setEnabled(true);
			}
			catch (Exception f)
				{Time.setText("Improper input.\nCalculation impossible.");}
		}
		else if (e.getActionCommand().equals("Reset"))
		{
			clearText();
			enableGrid(false);
			Simplify.setEnabled(true);
			Calculate.setEnabled(false);
			RowPoints[0].grabFocus();
		}
		else if (e.getActionCommand().equals("Estimate Time to Solve"))
		{
			try
			{
				readInput();
				estimate();
			}
			catch (Exception f)
				{Time.setText("Improper input.\nCalculation impossible.");}
		}
	}
	
	
	private void readInput() throws Exception
	{
		int i, j = 0, k, avail, totalPoints = 0, totalVolts = 0;
		String text;
		StringTokenizer T;
		
		resetFields();
		
		for (i=0;i<5;i++)
		{
			for (j=0;j<5;j++)
			{
				avail = 0;
				text = Values[i][j].getText();
				try
				{
					if (text.indexOf('%') > -1)
					{

						T = new StringTokenizer(text, "%");
						while (T.hasMoreTokens())
						{
							k = Integer.parseInt(T.nextToken().trim().substring(0,1));
							possible[i][j][k] = true;
							avail++;
						}
					}
					else
					{
						T = new StringTokenizer(text, " ");
						if (!T.hasMoreTokens())
						{
							for (k=0;k<4;k++)
							{
								possible[i][j][k] = true;
								avail++;
							}
						}
						while (T.hasMoreTokens())
						{
							k = Integer.parseInt(T.nextToken());
							possible[i][j][k] = true;
							avail++;
						}
					}
				}
				catch (Exception e)
				{
					for (k=0;k<4;k++)
					{
						possible[i][j][k] = true;
						avail++;
					}
					System.out.println("Error " + e.getMessage() + "at  [" + i + "][" + j + "].");
					System.out.println("Resetting possible values for [" + i + "][" + j + "].");
				}
				possibilities *= avail;
			}
		}
		
		try
		{
			for (i=0;i<5;i++)
			{
				j = 0;
				colPoints[i] = Integer.parseInt(ColumnPoints[i].getText());
				j++;
				colVolts[i] = Integer.parseInt(ColumnVolts[i].getText());
				j++;
				rowPoints[i] = Integer.parseInt(RowPoints[i].getText());
				j++;
				rowVolts[i] = Integer.parseInt(RowVolts[i].getText());

				j++;
				// check that points/volts combo works out
				if (colPoints[i] + colVolts[i] < 5 || colPoints[i] + 3 * colVolts[i] > 15)
				{
					i = Integer.parseInt("fail");
				}
				else if (rowPoints[i] + rowVolts[i] < 5 || rowPoints[i] + 3 * rowVolts[i] > 15)
				{
					j++;
					i = Integer.parseInt("fail");
				}
				
				totalPoints += colPoints[i] - rowPoints[i];
				totalVolts += colVolts[i] - rowVolts[i];
			}
			
			j+=2;
			if (totalPoints != 0)
			{
				i = Integer.parseInt("fail");
			}
			else if (totalVolts != 0)
			{
				j++;
				i = Integer.parseInt("fail");
			}
		}
		catch (Exception e)
		{
			if (j == 0)
				System.out.println("Error " + e.getMessage() + " for column " + (i + 1) + " point value.");
			else if (j == 1)
				System.out.println("Error " + e.getMessage() + " for column " + (i + 1) + " voltorb value.");
			else if (j == 2)
				System.out.println("Error " + e.getMessage() + " for row " + (i + 1) + " point value.");
			else if (j == 3)
				System.out.println("Error " + e.getMessage() + " for row " + (i + 1) + " voltorb value.");
			
			else if (j == 4)
				System.out.println("Error for column " + (i + 1) + ": impossible total of points and voltorbs.");
			else if (j == 5)
				System.out.println("Error for row " + (i + 1) + ": impossible total of points and voltorbs.");

			else if (j == 6)
				System.out.println("Error: Total column points does not match total row points.");
			else if (j == 7)
				System.out.println("Error: Total column voltorbs does not match total row voltorbs.");
			
			else
				System.out.println("Unknown Error.");
				
			System.out.println("Please make sure that all row and column values are properly input.");
			System.out.println();
			
			throw e;
		}
		
	}
	
	private void calculate()
	{
		initialize();
		
		while (!tested)
		{
			test();
			increment();
			testCount++;
		}
	}
	
	private void simplify()
	{
		int i, j, k;
		String text;
		
		for (i=0;i<5;i++)
		{
			if (rowVolts[i] != 5)
			{
				if (rowVolts[i] == 0)
				{
					for (j=0;j<5;j++)
						possible[i][j][0] = false;
				}
				
				if (rowPoints[i] + rowVolts[i] < 7)
				{
					for (j=0;j<5;j++)
						possible[i][j][3] = false;
					
					if (rowPoints[i] + rowVolts[i] < 6)
					{
						for (j=0;j<5;j++)
							possible[i][j][2] = false;
					}
				}
				if (rowPoints[i] + 3 * rowVolts[i] > 13)
				{
					for (j=0;j<5;j++)
						possible[i][j][1] = false;
	
					if (rowPoints[i] + 3 * rowVolts[i] > 14)
					{
						for (j=0;j<5;j++)
							possible[i][j][2] = false;
					}
				}
			}
			else
			{
				for (j=0;j<5;j++)
					for (k=1;k<4;k++)
						possible[i][j][k] = false;
			}
			
			if (colVolts[i] != 5)
			{
				if (colVolts[i] == 0)
				{
					for (j=0;j<5;j++)
						possible[j][i][0] = false;
				}
				
				if (colPoints[i] + colVolts[i] < 7)
				{
					for (j=0;j<5;j++)
						possible[j][i][3] = false;
					
					if (colPoints[i] + colVolts[i] < 6)
					{
						for (j=0;j<5;j++)
							possible[j][i][2] = false;
					}
				}
				if (colPoints[i] + 3 * colVolts[i] > 13)
				{
					for (j=0;j<5;j++)
						possible[j][i][1] = false;
	
					if (colPoints[i] + 3 * colVolts[i] > 14)
					{
						for (j=0;j<5;j++)
							possible[j][i][2] = false;
					}
				}
			}
			else
			{
				for (j=0;j<5;j++)
					for (k=1;k<4;k++)
						possible[j][i][k] = false;
			}
		}
		
		for (i=0;i<5;i++)
		{
			for (j=0;j<5;j++)
			{
				text = "";
				
				for (k=0;k<4;k++)
				{
					if (possible[i][j][k])
					{
						if (!text.equals(""))
							text += " " + k;
						else
							text = "" + k;
					}
				}
				
				Values[i][j].setText(text);
				if (text.equals("1"))
				{
					Values[i][j].setBackground(palette[2]);
				}
				else if (text.equals("0 1"))
				{
					Values[i][j].setBackground(palette[1]);
				}
				else if (text.charAt(0) != '0')
				{
					Values[i][j].setBackground(palette[3]);
				}
				else if (text.equals("0"))
				{
					Values[i][j].setBackground(palette[0]);
				}
			}
		}
	}
	
	private void resetFields()
	{
		int i, j, k;
		
		for (i=0;i<5;i++)
		{
			for (j=0;j<5;j++)
			{
				for (k=0;k<4;k++)
				{
					chances[i][j][k] = 0.0;
					possible[i][j][k] = false;
				}
				
				test[i][j] = 0;
			}
		}
		
		tested = false;
		total = 0;
		testCount = 0;
		possibilities = 1;
	}
	
	
	private void enableGrid(boolean status)
	{
		int i, j;
		
		for (i=0;i<5;i++)
		{
			ColumnPoints[i].setFocusable(!status);
			ColumnVolts[i].setFocusable(!status);
			RowPoints[i].setFocusable(!status);
			RowVolts[i].setFocusable(!status);
			
			for (j=0;j<5;j++)
			{
				Values[i][j].setFocusable(status);
			}
		}
		
		Recommend.setFocusable(status);
	}
	
	
	private void initialize()
	{
		int i, j, k;
		
		for (i=0;i<5;i++)
		{
			for (j=0;j<5;j++)
			{
				k = 0;
				while (!possible[i][j][k])
					k++;
				
				test[i][j] = k;
			}
		}
	}
	
	private void test()
	{
		int i, j, RP, RV, CP, CV;
		boolean fails = false;
		
		for (i=0;i<5;i++)
		{
			RP = RV = CP = CV = 0;
			for (j=0;j<5;j++)
			{
				RP += test[i][j];
				CP += test[j][i];
				if (test[i][j] == 0)
					RV++;
				if (test[j][i] == 0)
					CV++;
			}
			
			if (RP != rowPoints[i] || RV != rowVolts[i] || CP != colPoints[i] || CV != colVolts[i])
			{
				fails = true;
				break;
			}
		}
		
		if (!fails)
		{
			for (i=0;i<5;i++)
			{
				for(j=0;j<5;j++)
					chances[i][j][test[i][j]]++;
			}
			total++;
		}
	}
	
	private void increment()
	{
		int xPos = 0, yPos = 0;
		
		test[0][0]++;
		while (test[0][0] < 4 && !possible[0][0][test[0][0]])
			test[0][0]++;
		
		while (test[xPos][yPos] > 3)
		{
			test[xPos][yPos] = 0;
			while (!possible[xPos][yPos][test[xPos][yPos]])
				test[xPos][yPos]++;
			
			xPos++;
			if (xPos > 4)
			{
				xPos = 0;
				yPos++;
				
				if (yPos > 4)
				{
					tested = true;
					yPos = 0;
					break;
				}
			}
			
			test[xPos][yPos]++;
			while (test[xPos][yPos] < 4 && !possible[xPos][yPos][test[xPos][yPos]])
				test[xPos][yPos]++;
		}
	}
	
	
	private void printResults()
	{
		int i, j, k, noVolt, maxX = -1, maxY = -1;
		double max = -100, min0 = 100;
		String text = "", append;

		append = "Of " + total + " possibilities: \n";
		
		for (i=0;i<5;i++)
		{
			for (j=0;j<5;j++)
			{
				noVolt = 0;
				text = "";
				
				for (k=0;k<4;k++)
				{
					if (possible[i][j][k])
					{
						chances[i][j][k] = 100.0 * chances[i][j][k] / total;
						if (chances[i][j][k] > 99.9)
						{
							text = "" + k;
						}
						
						else if (chances[i][j][k] > 0.1)
						{
							if (text.length() > 0)
								text += "  ";
							text += k + ": " + (int)chances[i][j][k] + "%";
							
							if (noVolt > 0)
							{
								noVolt++;
								if (noVolt > 2)
								{
									max = 101;
								}
							}
						}
						
						else if (chances[i][j][k] < 0.1)
						{
							possible[i][j][k] = false;
							noVolt++;
							
							if (k == 0)
							{
								append += "Pick " + (j + 1) + "," + (i + 1) + "\n";
							}
						}
					}
				}

				Values[i][j].setText(text);
				
				if (possible[i][j][0])
				{
					if (max < (int) (chances[i][j][2] + chances[i][j][3] - chances[i][j][0]) ||
						(max == (int) (chances[i][j][2] + chances[i][j][3] - chances[i][j][0]) && min0 > chances[i][j][0]))
					{
						max = chances[i][j][2] + chances[i][j][3] - chances[i][j][0];
						min0 = chances[i][j][0];
						maxY = i + 1;
						maxX = j + 1;
					}
					
					if (!possible[i][j][2] && !possible[i][j][3])
					{
						if (possible[i][j][1])
							Values[i][j].setBackground(palette[1]);
						else
							Values[i][j].setBackground(palette[0]);
					}
				}
				else
				{
					if (!possible[i][j][2] && !possible[i][j][3])
						Values[i][j].setBackground(palette[2]);
					else
						Values[i][j].setBackground(palette[3]);
				}
			}
		}

		if (maxX < 0)
		{
			append += "Solved!\n";
		}
		else if (max < 101)
		{
			append += "Recommendation: " + maxX + "," + maxY + " = ";
			if (max > 0)
				append += "+";
			append += (int)max + "%\n";
		}
		
		Recommend.append(append + "\n");
	}
	
	private void clearText()
	{
		int i, j;
		
		Recommend.setText("");
		Time.setText("");
		for (i=0;i<5;i++)
		{
			ColumnPoints[i].setText("");
			ColumnVolts[i].setText("");
			RowPoints[i].setText("");
			RowVolts[i].setText("");
			
			for (j=0;j<5;j++)
			{
				Values[i][j].setText("");
				Values[i][j].setBackground(Color.white);
			}
		}
	}
	
	private void estimate()
	{
		long time = overhead + possibilities / rate;
		String text = possibilities + " estimated possibilites.\n";
		
		if (time < 86400000)
		{
			Time now = new Time(18000000 + time);
			text += now + " (" + time + ") estimated to solve.";
		}
		else
		{
			int days = (int) (time / 34560000);
			text += days + "+ days (" + time + ") estimated to solve.";
		}
			
		Time.setText(text);
	}
}