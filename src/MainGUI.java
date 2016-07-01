import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.Random;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main class of this randomizer.
 * @author Jose Antonio Rodriguez Rivera
 *
 */
public class MainGUI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//Frame
		JFrame window = new JFrame();

		//Greeting Message
		JOptionPane.showMessageDialog(window, "Please choose the WAD File to randomize!", "RanDooMizer", JOptionPane.INFORMATION_MESSAGE, null);


		//FIle Browser
		JFileChooser browse = new JFileChooser();

		//Set File Filter to only accept .wad files
		FileNameExtensionFilter filter = new FileNameExtensionFilter("WAD Files", "wad");
		browse.setFileFilter(filter);
		int returnVal = browse.showOpenDialog(window);
		if(returnVal == JFileChooser.APPROVE_OPTION){
			//Get the file
			File wadFile = browse.getSelectedFile();

			//Ask for a seed to be used for the randomization
			boolean seedGiven = false;
			long numSeed = 0;
			int answer = JOptionPane.showConfirmDialog(window, "Would you prefer to use a seed for the randomization?", "RanDooMizer", JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.YES_OPTION){
				boolean correct = false;
				while(!correct){
					String seed = JOptionPane.showInputDialog("Please enter the desired seed to use. (A seed is a number that can go from " + Long.MIN_VALUE + " to " + Long.MAX_VALUE + ")", null);
					try{
						numSeed = Long.parseLong(seed);
						correct = true;
						seedGiven = true;
					} catch( NumberFormatException e){
						JOptionPane.showMessageDialog(window, "There was an error in the seed given. Please try again.", "RanDooMizer", JOptionPane.ERROR_MESSAGE, null);
					}
				}
			}

			//Randomizer option selection
			boolean proceed = false;
			String[] items = {"Doom", "Doom 2"};
			JComboBox<String> baseGame = new JComboBox<>(items);
			JCheckBox enemies = new JCheckBox("Enemies");
			JCheckBox weapons = new JCheckBox("Weapons");
			JCheckBox powerups = new JCheckBox("Chance of getting weapons in place of powerups (10% chance that a powerup is changed to a weapon)");
			JCheckBox ammo = new JCheckBox("Ammunition");

			String[] diff = {"Easy", "Medium", "Hard"};
			JComboBox<String> difficulty = new JComboBox<>(diff);
			difficulty.setEnabled(false);
			
			ActionListener diffEnabler = new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					JCheckBox obj = (JCheckBox)e.getSource();
					if(obj.isSelected())
						difficulty.setEnabled(true);
					else
						difficulty.setEnabled(false);
				}
				
			};
			
			enemies.addActionListener(diffEnabler);
			
			Object[] params = {"Please select what things will be randomized", enemies, "Please select the difficulty", difficulty, "\n", weapons, powerups, ammo, "\nPlease select the base game", baseGame};

			do{
				int status = JOptionPane.showConfirmDialog(window, params, "RanDooMizer", JOptionPane.OK_CANCEL_OPTION);
				if(status == JOptionPane.CANCEL_OPTION){ 	//User pressed cancel, program ends
					window.dispose();
					System.exit(0);
				}

				if(enemies.isSelected() || weapons.isSelected() || powerups.isSelected() || ammo.isSelected())
					proceed = true;
				else
					JOptionPane.showMessageDialog(window, "At least select something, will you?", "RanDooMizer", JOptionPane.ERROR_MESSAGE, null);
			}while(!proceed);

			//If no seed was given, generate a random seed
			if(!seedGiven){
				Random rng = new Random();
				numSeed  = rng.nextLong();
			}

			//Initialize the randomizer, and execute it
			int doomGame = baseGame.getSelectedIndex() + 1;

			int difficultyInt = difficulty.getSelectedIndex();
			
			RanDooMizer randomizer = null;
			try{
				randomizer = new RanDooMizer(wadFile, doomGame, numSeed, enemies.isSelected(), difficultyInt, weapons.isSelected(), powerups.isSelected(), ammo.isSelected());
			}catch(InvalidParameterException e){
				e.printStackTrace();;
			}

			randomizer.randomize();

			//Lets the user know that the randomizer has finished
			JOptionPane.showMessageDialog(window, "Finished! Have Fun!", "RanDooMizer", JOptionPane.INFORMATION_MESSAGE);
			window.dispose();

		}
		else{			//User pressed cancel, program ends
			window.dispose();
			System.exit(0);
		}
	}
}
