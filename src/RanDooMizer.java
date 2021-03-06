import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
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
	private int doomGame, difficulty;
	private boolean enemiesRandomized, weaponsRandomized, powerupRandomized, ammoRandomized;
	private Random randomNumberGenerator;
	private long seed;
	private static final long DOOM1_OFFSET = 112;
	private static final long DOOM_MAPOFFSET = 176;
	private static final long DOOM2_OFFSET = 96;
	private static final int DOOM_NUMMAPS = 36;
	private static final int DOOM2_NUMMAPS = 32;

	/**
	 * Constructor for the RanDooMizer.
	 * @param wadFile the WAD File to use as a base
	 * @param seed the seed used for the random generator
	 * @param enemiesRandomized true if the enemies will be randomized, false otherwise
	 * @param difficulty If 0, Easy was selected. If 1, Medium was selected. If 2, Hard was selected
	 * @param weaponsRandomized true if the weapons are randomized, false otherwise
	 * @param powerupRandomized true if the powerups are randomized, false otherwise
	 * @param ammoRandomized true if the ammunition is randomized, false otherwise
	 */
	public RanDooMizer(File wadFile, int doomGame, long seed, boolean enemiesRandomized, int difficulty, boolean weaponsRandomized, boolean powerupRandomized, boolean ammoRandomized) throws InvalidParameterException{
		File newWad = null;
		if(doomGame == 1)
			newWad = new File(wadFile.getParent() + File.separator + "Doom_" + seed + ".wad");
		else if(doomGame == 2)
			newWad = new File(wadFile.getParent() + File.separator + "Doom2_" + seed + ".wad");
		else
			throw new InvalidParameterException("Invalid Doom Game");

		try {
			copyFile(wadFile, newWad);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Set instance variables
		this.wadFile = newWad;
		this.doomGame = doomGame;
		this.seed = seed;
		this.enemiesRandomized = enemiesRandomized;
		this.difficulty  = difficulty;
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
	public void randomize(){
		//Let the randomizing begin!
		try{

			//Load the WAD File
			RandomAccessFile raf = new RandomAccessFile(wadFile, "rw");

			//Byte buffer used for data manipulation
			byte[] byteBuff = new byte[4];

			//Initialize data lists
			initializeDoomLists();

			if(doomGame == 2)
				initializeDoom2Lists();

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


			int numMaps = 0;
			if(doomGame == 1)
				numMaps = DOOM_NUMMAPS;
			else if(doomGame == 2)
				numMaps = DOOM2_NUMMAPS;

			for(int i = 1 ; i <= numMaps ; i++){
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
	private void initializeDoomLists(){
		enemiesTID = new ArrayList<Integer>();
		//enemiesTID.add(3003);	//Baron of Hell
		enemiesTID.add(3005); 	//Cacodemon
		enemiesTID.add(3002);	//Pinky
		enemiesTID.add(3004);	//Former Human (Pistol)
		enemiesTID.add(9);			//Former Human (Shotgun)
		enemiesTID.add(3001);	//Imp
		enemiesTID.add(3006);	//Lost Soul
		enemiesTID.add(58);			//Spectre

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
	 * Adds additional items and enemies exclusive to Doom 2 to the randomizer.
	 */
	private void initializeDoom2Lists(){
		enemiesTID.add(68);			//Arachnotron
		enemiesTID.add(64);			//Arch-Vile
		enemiesTID.add(3003);	//Baron of Hell
		enemiesTID.add(65);			//Chaingunner
		enemiesTID.add(72);			//Commander Keen
		enemiesTID.add(69);			//Hell Knight
		enemiesTID.add(67);			//Mancubus
		enemiesTID.add(71);			//Pain Elemental
		enemiesTID.add(66);			//Revenant
		enemiesTID.add(84);			//Wolfenstein SS

		weaponsTID.add(82); 		//Super Shotgun
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
					if((tid == 3003 && levelNumber == 8 || tid == 16 && levelNumber == 17 || tid == 7 && levelNumber == 26) && doomGame == 1)			//Baron of Hell in E1M8, Cyberdemon in E2M8 or Spider Mastermind in E3M8, these can't be changed (level will not end)
						return tid;

					int randomTID = randomNumberGenerator.nextInt(100);
					if(seed == 666)
						return 16;	//Hehe... 

					if(doomGame == 1){		//Doom
						if(difficulty == 0){	//Easy Mode
							if(randomTID >= 0 && randomTID <= 13)
								return 3005;		//Cacodemon
							else if(randomTID >= 14 && randomTID <= 27)
								return 3002;		//Demon
							else if(randomTID >= 28 && randomTID <= 41)
								return 3004;		//Former Human Trooper
							else if(randomTID >= 42 && randomTID <= 55)
								return 9;				//Former Human Sergeant
							else if(randomTID >= 56 && randomTID <= 69)
								return 3001;		//Imp
							else if(randomTID >= 70 && randomTID <= 83)
								return 3006;		//Lost Soul
							else if (randomTID >= 84 && randomTID <= 97)
								return 58;			//Spectre
							else
								return enemiesTID.get(randomTID % enemiesTID.size());
						}
						else if(difficulty == 1){		//Medium Mode
							if(randomTID >= 0 && randomTID <= 9)
								return 3005;		//Cacodemon
							else if(randomTID >= 10 && randomTID <= 19)
								return 3002;		//Demon
							else if(randomTID >= 20 && randomTID <= 29)
								return 3004;		//Former Human Trooper
							else if(randomTID >= 30 && randomTID <= 39)
								return 9;				//Former Human Sergeant
							else if(randomTID >= 40 && randomTID <= 49)
								return 3001;		//Imp
							else if(randomTID >= 50 && randomTID <= 59)
								return 3006;		//Lost Soul
							else if (randomTID >= 60 && randomTID <= 69)
								return 58;			//Spectre
							else if(randomTID >= 70 && randomTID <= 79)
								if(doomGame == 1 && levelNumber ==8)
									return tid;		//Doesn't add more Barons of Hell in E1M8
								else
									return 3003;		//Baron of Hell

							else
								return enemiesTID.get(randomTID % enemiesTID.size());
						}
						else{		//Hard Mode
							if(randomTID >= 0 && randomTID <= 2)
								if(doomGame == 1 && levelNumber == 26)
									return tid;	//Same reason as Baron of Hell
								else
									return 7;		//Spider Mastermind

							else if(randomTID >= 3 && randomTID <= 5)
								if(doomGame == 1 && levelNumber == 17)
									return tid;	//Same reason as Baron of Hell
								else
									return 16;		//Cyberdemon

							else if(randomTID >= 70 && randomTID <= 79)
								if(doomGame == 1 && levelNumber ==8)
									return tid;		//Doesn't add more Barons of Hell in E1M8
								else
									return 3003;		//Baron of Hell

							else
								return enemiesTID.get(randomTID % enemiesTID.size());
						}
					}
					else if(doomGame == 2){		//Doom 2
						if(difficulty == 0){		//Easy Mode
							//if(randomTID == 0)
							//	return 68;		//Arachnotron
							//else if(randomTID == 1)
							//	return 64;		//Arch-Vile
							//else if(randomTID == 2)
							//	return 3003;		//Baron of Hell
							if(randomTID >= 3 && randomTID <= 6)
								return 3005;		//Cacodemon
							else if(randomTID >= 7 && randomTID <= 10)
								return 65;		//Chaingunner
							else if(randomTID >= 11 && randomTID <= 20)
								return 72;		//Commander Keen
							//else if(randomTID == 21)
							//	return 16;		//Cyberdemon
							else if(randomTID >= 22 && randomTID <= 31)
								return 3002;		//Demon
							else if(randomTID >= 32 && randomTID <= 42)
								return 3004;		//Former Human Trooper
							else if(randomTID >= 43 && randomTID <= 53)
								return 9;				//Former Human Sergeant
							//else if(randomTID == 54)
							//	return 69;			//Hell Knight
							else if(randomTID >= 55 && randomTID <= 66)
								return 3001;		//Imp
							else if(randomTID >= 67 && randomTID <= 76)
								return 3006;		//Lost Soul
							//else if(randomTID == 77)
							//	return 67;		//Mancubus
							//else if(randomTID == 78)
							//	return 71;		//Pain Elemental
							//else if(randomTID == 79)
							//	return 66;		//Revenant
							else if(randomTID >= 80 && randomTID <= 89)
								return 58;		//Spectre
							//else if(randomTID == 90)
							//	return 7;		//Spider Mastermind
							else if(randomTID >= 91 && randomTID <= 99)
								return 84;		//Wolfenstein SS
							else
								return enemiesTID.get(randomTID % enemiesTID.size());
						}
						else if(difficulty == 1){		//Medium Mode
							//if(randomTID >= 0 && randomTID <= 2)
							//	return 68;		//Arachnotron
							//else if(randomTID >= 3 & randomTID <= 4)
							//	return 64;		//Arch-Vile
							//else if(randomTID >= 5 && randomTID <= 7)
							//	return 3003;		//Baron of Hell
							if(randomTID >= 8 && randomTID <= 14)
								return 3005;		//Cacodemon
							else if(randomTID >= 15 && randomTID <= 21)
								return 65;		//Chaingunner
							else if(randomTID >= 22 && randomTID <= 28)
								return 72;		//Commander Keen
							//else if(randomTID >= 29 && randomTID <= 30)
							//	return 16;		//Cyberdemon
							else if(randomTID >= 31 && randomTID <= 37)
								return 3002;		//Demon
							else if(randomTID >= 38 && randomTID <= 44)
								return 3004;		//Former Human Trooper
							else if(randomTID >= 45 && randomTID <= 51)
								return 9;				//Former Human Sergeant
							//else if(randomTID >= 52 && randomTID <= 54)
							//	return 69;			//Hell Knight
							else if(randomTID >= 55 && randomTID <= 61)
								return 3001;		//Imp
							else if(randomTID >= 62 && randomTID <= 68)
								return 3006;		//Lost Soul
							//else if(randomTID >= 69 && randomTID <= 70)
							//	return 67;		//Mancubus
							//else if(randomTID >= 71 && randomTID <= 73)
							//	return 71;		//Pain Elemental
							//else if(randomTID >= 74 && randomTID <= 75)
							//	return 66;		//Revenant
							else if(randomTID >= 76 && randomTID <= 82)
								return 58;		//Spectre
							//else if(randomTID >= 83 && randomTID <= 84)
							//	return 7;		//Spider Mastermind
							else if(randomTID >= 84 && randomTID <= 90)
								return 84;		//Wolfenstein SS
							else
								return enemiesTID.get(randomTID % enemiesTID.size());

						}
						else{			//Hard Mode
							enemiesTID.add(7);		//Spider Mastermind
							enemiesTID.add(16);		//Cyberdemon
							return enemiesTID.get(randomTID % enemiesTID.size());
						}
					}
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


