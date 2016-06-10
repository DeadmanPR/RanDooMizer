import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class randomizes the Enemies and Weapons in the game DooM.
 * @author Jose Antonio Rodriguez Rivera
 *
 */
public class RanDooMizer {
	//Instance variables
	private ArrayList<Integer> enemiesTID;
	private ArrayList<Integer> weaponsTID;
	private ArrayList<Integer> powerupsTID;
	private ArrayList<Integer> ammunitionTID;
	private File wadFile;
	private boolean enemiesRandomized, weaponsRandomized, powerupRandomized, ammoRandomized;
	private Random randomNumberGenerator;
	private long seed;
	private static final long DOOM1_OFFSET = 112;
	private static final long DOOM_MAPOFFSET = 176;
	private static final long DOOM2_OFFSET = 96;

	/**
	 * Constructor for the RanDooMizer.
	 * @param wadFile the WAD File to use as a base
	 * @param seed the seed used for the random generator
	 * @param enemiesRandomized true if the enemies will be randomized, false otherwise
	 * @param weaponsRandomized true if the weapons are randomized, false otherwise
	 * @param powerupRandomized true if the powerups are randomized, false otherwise
	 * @param ammoRandomized true if the ammunition is randomized, false otherwise
	 */
	public RanDooMizer(File wadFile, long seed, boolean enemiesRandomized, boolean weaponsRandomized, boolean powerupRandomized, boolean ammoRandomized){
		File newWad = new File(wadFile.getParent() + File.separator + "DooM-" + seed + ".wad");
		try {
			copyFile(wadFile, newWad);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Set instance variables
		this.wadFile = newWad;
		this.seed = seed;
		this.enemiesRandomized = enemiesRandomized;
		this.weaponsRandomized = weaponsRandomized;
		this.powerupRandomized = powerupRandomized;
		this.ammoRandomized = ammoRandomized;

		//Initialize random number generator
		randomNumberGenerator = new Random();
		randomNumberGenerator.setSeed(seed);
	}


	/**
	 * The "main" class of the randomizer. Interprets the .wad file structure to modify game data.
	 * @param doomGame integer that represents what DooM game will the randomizer use as a base (1 for The Ultimate Doom, 2 for Doom II: Hell on Earth
	 */
	public void randomize(int doomGame){
		//Let the randomizing begin!
		try{

			//Load the WAD File
			RandomAccessFile raf = new RandomAccessFile(wadFile, "rw");

			//Byte buffer used for data manipulation
			byte[] byteBuff = new byte[4];

			//Initialize data lists
			initializeLists();

			//Header decoding
			raf.seek(4);
			raf.read(byteBuff, 0, 4);
			int numberOfEntries = getIntFromByteArray(byteBuff);

			
			raf.read(byteBuff, 0, 4);
			int directoryOffset = getIntFromByteArray(byteBuff);

			//Go to the Directory Structure of the WAD File
			long currentOffset = 0;
			if(doomGame == 1)
				currentOffset = (long)(directoryOffset + DOOM1_OFFSET);
			else if(doomGame == 2)
				currentOffset = (long)(directoryOffset + DOOM2_OFFSET);
			


			for(int i = 1 ; i <= 27 ; i++){
				//Go to Map Data Lump
				raf.seek(currentOffset+16);


				//Get the offset for the map's THINGS lump
				raf.read(byteBuff, 0, 4);		
				int thingsOffset = getIntFromByteArray(byteBuff);

				//Get the map's THINGS lump size
				raf.read(byteBuff, 0, 4);
				int lumpSize = getIntFromByteArray(byteBuff);

				//Go to the THINGS lump of the current map
				raf.seek(thingsOffset);

				//For each THING in the lump
				for(int j=0 ; j < lumpSize / 10 ; j++){
					raf.skipBytes(6);

					//Save the offset to write the TID to
					long offsetToWrite = raf.getFilePointer();

					//Get the TID of the THING being examined
					byte[] tid = new byte[4];
					tid[0] = raf.readByte();
					tid[1] = raf.readByte();
					tid[2] = (byte)0;
					tid[3] = (byte)0;

					//Get the TID in decimal format and randomize it
					int tidDec = getIntFromByteArray(tid);
					int newTID = randomizeThing(tidDec, i);
			
					//If the TID was changed, proceed to write the new TID to the WAD File
					if(newTID != tidDec){
						//Backup the offset
						long offsetBackup = raf.getFilePointer();

						//Write the new TID 
						raf.seek(offsetToWrite);
						byte[] byteArr = convertToByteArray(newTID);
						byte[] btw = {byteArr[0],byteArr[1]};
						raf.write(btw, 0, 2);

						//Restore the previous offset
						raf.seek(offsetBackup);
					}

					raf.skipBytes(2);
				}


				//Next Map
				currentOffset += DOOM_MAPOFFSET;

			}

			//Close the file
			raf.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Converts a byte array to an integer, using the Little Endian convention.
	 * @param arr the byte array to convert
	 * @return the integer vlue of the byte array
	 */
	private int getIntFromByteArray(byte[] arr){
		//Doom WADs use little endian convention
		return ByteBuffer.wrap(arr).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
	}

	/**
	 * Converts an integer to a byte array, using the Little Endian convention.
	 * @param value the integer to convert
	 * @return the resultant byte array
	 */
	private byte[] convertToByteArray(int value){
		byte[] btr = new byte[4];
		int mask = 0xFF;
		int shiftNumber = 0;

		for(int i = 0; i <3 ; i++){
			int byteValue = value & mask;

			btr[i] = (byte)(byteValue >> shiftNumber);
			shiftNumber+=8;
			mask = (int)((byte)mask << shiftNumber);
		}

		return btr;
	}
	
	/**
	 * Copies a file.
	 * @param source the source file
	 * @param dest the destination file
	 * @throws IOException if an error occurred
	 */
	private static void copyFile(File source, File dest) throws IOException {
	    InputStream input = null;
	    OutputStream output = null;
	    try {
	        input = new FileInputStream(source);
	        output = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = input.read(buffer)) > 0) {
	            output.write(buffer, 0, length);
	        }
	    } finally {
	        input.close();
	        output.close();
	    }
	}

	/**
	 * Initializes the TID lists
	 */
	private void initializeLists(){
		enemiesTID = new ArrayList<Integer>();
		enemiesTID.add(3003);	//Baron of Hell
		enemiesTID.add(3005); 	//Cacodemon
		//enemiesTID.add(16); 		//Cyberdemon
		enemiesTID.add(3002);	//Pinky
		enemiesTID.add(3004);	//Former Human (Pistol)
		enemiesTID.add(9);			//Former Human (Shotgun)
		enemiesTID.add(3001);	//Imp
		enemiesTID.add(3006);	//Lost Soul
		enemiesTID.add(58);			//Spectre
		//enemiesTID.add(7);			//Spider Mastermind

		weaponsTID = new ArrayList<Integer>();
		weaponsTID.add(2006);	//BFG9000
		weaponsTID.add(2002);	//Chaingun
		weaponsTID.add(2005);	//Chainsaw
		weaponsTID.add(2004);	//Plasma Rifle
		weaponsTID.add(2003);	//Rocket Launcher
		weaponsTID.add(2001);	//Shotgun

		powerupsTID = new ArrayList<Integer>();
		//powerupsTID.add(8);		//Backpack
		powerupsTID.add(2019);	//Blue Armor
		powerupsTID.add(2018);	//Green Armor
		powerupsTID.add(2012);	//Medikit
		//powerupsTID.add(2025);	//Radiation Suit
		powerupsTID.add(2011);	//Stimpack
		//powerupsTID.add(2023);	//Berserk
		powerupsTID.add(2014);	//Health Potion
		//powerupsTID.add(2024); 	//Invisibility
		//powerupsTID.add(2045);	//Light Amplification Visor
		//powerupsTID.add(83);	//Megasphere
		//powerupsTID.add(2013);	//Soulsphere
		powerupsTID.add(2015);	//Spiritual Armor

		ammunitionTID = new ArrayList<Integer>();
		ammunitionTID.add(2007);	//Ammo Clip
		ammunitionTID.add(2048);	//Box of Ammo
		ammunitionTID.add(2046);	//Box of Rockets
		ammunitionTID.add(2049);	//Box of Shells
		ammunitionTID.add(2047); 	//Cell Charge
		ammunitionTID.add(17);		//Cell Charge Pack
		ammunitionTID.add(2010);	//Rocket
		ammunitionTID.add(2008);	//Shotgun Shells
	}

	/**
	 * Randomizes a THING contained inside the DooM WAD.
	 * @param tid the THING id found in the DooM WAD
	 * @param levelNumber the number of the level being randomized (with E1M1 being 1, E1M2 being 2 and so on)
	 * @return a new THING id if tid was of a randomized type
	 * @return tid if the THING is not of a randomized type (stays the same)
	 */
	private int randomizeThing(int tid, int levelNumber){
		//Searching if tid is an enemy...
		for(Integer e : enemiesTID){
			if(tid == e){
				if(!enemiesRandomized){
					randomNumberGenerator.nextInt(100);
					return tid;
				}
				
				else{
					if(tid == 3003 && levelNumber == 8 || tid == 16 && levelNumber == 17 || tid == 7 && levelNumber == 26 )			//Baron of Hell in E1M8, Cyberdemon in E2M8 or Spider Mastermind in E3M8, these can't be changed (level will not end)
						return tid;
				
					int randomTID = randomNumberGenerator.nextInt(100);
					if(seed == 666)
						return 16;	//Hehe... 
					
					if(randomTID == 0)
						return 7;	//Spider Mastermind (1/100 chance)
					else if (randomTID == 1)
						return 16; //Cyberdemon (1/100 chance)
					else
						//Get any enemy
						return enemiesTID.get(randomTID % enemiesTID.size());
				}
			}
		}

		//Searching if tid is a weapon...
		for(Integer w : weaponsTID){
			if(tid == w){
				if(!weaponsRandomized){
					randomNumberGenerator.nextInt(weaponsTID.size());
					return tid;
				}
				else{
					//If it is, generate a random number and return a random weapon tid
					return weaponsTID.get(randomNumberGenerator.nextInt(weaponsTID.size()));
				}
			}
		}

		//Searching if tid is a powerup...
		for(Integer p : powerupsTID){
			if(tid == p){
				if(!powerupRandomized){
					randomNumberGenerator.nextInt(100);
					return tid;
				}
				else{
					
					int randomTID = randomNumberGenerator.nextInt(100);
					if(randomTID >= 0 && randomTID <= 90)
						return tid;
					else
						return weaponsTID.get(randomTID % weaponsTID.size());	//10% chance of getting a weapon in place of a powerup, done for balancing purposes
				}
			}
		}

		//Searching if tid corresponds to ammunition...
		for(Integer a : ammunitionTID){
			if(tid == a){
				if(!ammoRandomized){
					randomNumberGenerator.nextInt(ammunitionTID.size());
					return tid;
				}
				//If it is, generate a random number and return a random ammo tid
				return ammunitionTID.get(randomNumberGenerator.nextInt(ammunitionTID.size()));
			}
		}

		//The tid does not correspond to a randomized type, therefore it is returned as is
		return tid;
	}

}
