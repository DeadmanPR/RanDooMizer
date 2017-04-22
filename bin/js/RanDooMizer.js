//Credit: http://indiegamr.com/generate-repeatable-random-numbers-in-js/

var DOOM1_OFFSET = 112;
var DOOM_MAPOFFSET = 176;
var DOOM2_OFFSET = 96;
var DOOM_NUMMAPS = 36;
var DOOM2_NUMMAPS = 32;
var enemiesTID = new Array();
var weaponsTID = new Array();
var powerupsTID = new Array();
var ammunitionTID = new Array();
var doomGame;
var difficulty;
var enemiesRandomized;
var weaponsRandomized;
var powerupRandomized;
var ammoRandomized;
var randomizerSeed;


 
Math.seededRandom = function(max, min) {
    max = max || 1;
    min = min || 0;
 
    Math.seed = (Math.seed * 9301 + 49297) % 233280;
    var rnd = Math.seed / 233280;
 
    return Math.floor(min + rnd * (max - min));
}

function randomize(wad, doomVersion, diff, enemiesRandom, weaponsRandom, powerupRandom, ammoRandom, seed){
    Math.seed = seed;
	randomizerSeed = seed;

    var positionInFile = 4;

	var wadFile = new Uint8Array(wad);// new Array();
	//for(var x = 0 ; x < wad.length; x++){
	//	wadFile.push(wad[x]);
	//}

	
    this.doomGame = doomVersion;
    this.difficulty = diff;
    this.enemiesRandomized = enemiesRandom;
    this.weaponsRandomized = weaponsRandom;
    this.powerupRandomized = powerupRandom;
    this.ammoRandomized = ammoRandom;
		

	//Byte buffer used for data manipulation
	var byteBuff;

    //Initialize data lists
	initializeDoomLists(); 

	if(doomGame == 2)
		initializeDoom2Lists();

	//Header decoding
    byteBuff = new Array();
	for(var a = 0; a < 4; a++){
        byteBuff.push(wadFile[positionInFile+a]);
    }
	positionInFile += 4;
	
	var numberOfEntries = getIntFromByteArray(byteBuff);

    byteBuff = new Array();
	for(var b = 0; b < 4; b++){
        byteBuff.push(wadFile[positionInFile+b]);
    }
	positionInFile += 4;
	

	var directoryOffset = getIntFromByteArray(byteBuff);

	//Go to the Directory Structure of the WAD File
	var currentOffset = 0;
		if(doomGame == 1)
			currentOffset = (directoryOffset + DOOM1_OFFSET);
		else if(doomGame == 2)
			currentOffset = (directoryOffset + DOOM2_OFFSET);


		var numMaps = 0;
		if(doomGame == 1)
			numMaps = DOOM_NUMMAPS;
		else if(doomGame == 2)
			numMaps = DOOM2_NUMMAPS;

			for(var i = 1 ; i <= numMaps ; i++){
				//Go to Map Data Lump
				positionInFile = (currentOffset+16);
	
				//Get the offset for the map's THINGS lump
				byteBuff = new Array();
	            for(var j = 0; j < 4; j++){
                       byteBuff.push(wadFile[positionInFile+j]);
                }	
				positionInFile += 4;	
			    var thingsOffset = getIntFromByteArray(byteBuff);

				//Get the map's THINGS lump size
				byteBuff = new Array();
	            for(var k = 0; k < 4; k++){
                    byteBuff.push(wadFile[positionInFile+k]);
                }	
				positionInFile += 4;
				var lumpSize = getIntFromByteArray(byteBuff);

				//Go to the THINGS lump of the current map
				positionInFile = thingsOffset;

				//For each THING in the lump
				for(var l=0 ; l < lumpSize / 10 ; l++){
					positionInFile += 6;

					//Save the offset to write the TID to
					var offsetToWrite = positionInFile;

					//Get the TID of the THING being examined
					var tid = new Array();
                    tid.push(wadFile[positionInFile++]);
					tid.push(wadFile[positionInFile++]);
					tid.push(0);
					tid.push(0);
			
        
					//Get the TID in decimal format and randomize it
					var tidDec = getIntFromByteArray(tid);
					var newTID = randomizeThing(tidDec, i);


					//If the TID was changed, proceed to write the new TID to the WAD File
					if(newTID != tidDec){
						//Backup the offset
						var offsetBackup = positionInFile;

						//Write the new TID
                        positionInFile = offsetToWrite;
						var byteArr = convertToByteArray(newTID);
					
						wadFile[positionInFile++] = byteArr[0];
		                wadFile[positionInFile++] = byteArr[1];

						//Restore the previous offset
						positionInFile = offsetBackup;
					}

					positionInFile += 2;
				}


				//Next Map
				currentOffset += DOOM_MAPOFFSET;

			}

        console.log('FINISHED');
		// for(var y = 0; y < wadFile.length; y++)
		// 	wadFile[y] = String.fromCharCode(wadFile[y]);
			
   		// wadFile = wadFile.join("");
	
    	var blob = new Blob([wad]);
    	var link = document.createElement('a');
    	link.href = window.URL.createObjectURL(blob);
		var fileName;
		if(doomGame == 1)
    		fileName = 'Doom_' + randomizerSeed + ".wad";
		else if(doomGame == 2)
			fileName = 'Doom2_' + randomizerSeed + ".wad";
    	link.download = fileName;
    	link.click();
		document.getElementById("page").style.opacity = 1;
   		document.getElementById("loading-spinner").style.display = 'none';
   		document.getElementById("loading-spinner-text").style.display = 'none';

}

function getIntFromByteArray(arr){
    var arrayToConvert = arr.reverse();
	var intToReturn = arrayToConvert[0];
    for(var i = 1; i < 4; i++){
    	intToReturn = intToReturn << 8; 
        intToReturn = intToReturn | arrayToConvert[i];
    }

    return intToReturn;
}

function convertToByteArray(value){
    var arrayToReturn = new Array();
    var mask = 0xFF;
    var shiftNumber = 0;

    for(var i = 0 ; i < 4; i++){
        var byteValue = value & mask;
        arrayToReturn.push(byteValue >> shiftNumber);
        shiftNumber += 8;
        mask = mask << 8;
        
    }

    return arrayToReturn;
}

/**
  * Initializes the TID lists
  */
function initializeDoomLists(){
	
	//enemiesTID.push(3003);	//Baron of Hell
	enemiesTID.push(3005); 	//Cacodemon
	enemiesTID.push(3002);	//Pinky
	enemiesTID.push(3004);	//Former Human (Pistol)
	enemiesTID.push(9);			//Former Human (Shotgun)
	enemiesTID.push(3001);	//Imp
	enemiesTID.push(3006);	//Lost Soul
	enemiesTID.push(58);			//Spectre

	
	weaponsTID.push(2006);	//BFG9000
	weaponsTID.push(2002);	//Chaingun
	weaponsTID.push(2005);	//Chainsaw
	weaponsTID.push(2004);	//Plasma Rifle
	weaponsTID.push(2003);	//Rocket Launcher
	weaponsTID.push(2001);	//Shotgun

	
	//powerupsTID.push(8);		//Backpack
	powerupsTID.push(2019);	//Blue Armor
	powerupsTID.push(2018);	//Green Armor
	powerupsTID.push(2012);	//Medikit
	//powerupsTID.push(2025);	//Radiation Suit
	powerupsTID.push(2011);	//Stimpack
	//powerupsTID.push(2023);	//Berserk
	powerupsTID.push(2014);	//Health Potion
	//powerupsTID.push(2024); 	//Invisibility
	//powerupsTID.push(2045);	//Light Amplification Visor
	//powerupsTID.push(83);	//Megasphere
	//powerupsTID.push(2013);	//Soulsphere
	powerupsTID.push(2015);	//Spiritual Armor

	
	ammunitionTID.push(2007);	//Ammo Clip
	ammunitionTID.push(2048);	//Box of Ammo
	ammunitionTID.push(2046);	//Box of Rockets
	ammunitionTID.push(2049);	//Box of Shells
	ammunitionTID.push(2047); 	//Cell Charge
	ammunitionTID.push(17);		//Cell Charge Pack
	ammunitionTID.push(2010);	//Rocket
	ammunitionTID.push(2008);	//Shotgun Shells
}

/**
  * Adds additional items and enemies exclusive to Doom 2 to the randomizer.
  */
function initializeDoom2Lists(){
	enemiesTID.push(68);			//Arachnotron
	enemiesTID.push(64);			//Arch-Vile
	enemiesTID.push(3003);	//Baron of Hell
	enemiesTID.push(65);			//Chaingunner
	enemiesTID.push(72);			//Commander Keen
	enemiesTID.push(69);			//Hell Knight
	enemiesTID.push(67);			//Mancubus
	enemiesTID.push(71);			//Pain Elemental
	enemiesTID.push(66);			//Revenant
	enemiesTID.push(84);			//Wolfenstein SS

	weaponsTID.push(82); 		//Super Shotgun
}

/**
  * Randomizes a THING contained inside the DooM WAD.
  * @param tid the THING id found in the DooM WAD
  * @param levelNumber the number of the level being randomized (with E1M1 being 1, E1M2 being 2 and so on)
  * @return a new THING id if tid was of a randomized type
  * @return tid if the THING is not of a randomized type (stays the same)
  */
function randomizeThing(tid, levelNumber){
	//Searching if tid is an enemy...
	for(var i = 0; i < enemiesTID.length; i++){
		if(tid == enemiesTID[i]){
			if(!enemiesRandomized){
				Math.seededRandom(0,99);
				return tid;
			}

			else{
				if((tid == 3003 && levelNumber == 8 || tid == 16 && levelNumber == 17 || tid == 7 && levelNumber == 26) && doomGame == 1)			//Baron of Hell in E1M8, Cyberdemon in E2M8 or Spider Mastermind in E3M8, these can't be changed (level will not end)
					return tid;

				var randomTID = Math.seededRandom(0,99);
				if(randomizerSeed == 666)
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
							return enemiesTID[(randomTID % enemiesTID.length)];
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
							return enemiesTID[(randomTID % enemiesTID.length)];
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
							return enemiesTID[(randomTID % enemiesTID.length)];
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
							return enemiesTID[(randomTID % enemiesTID.length)];
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
							return enemiesTID[(randomTID % enemiesTID.length)];
						}
					else{			//Hard Mode
						enemiesTID.push(7);		//Spider Mastermind
						enemiesTID.push(16);		//Cyberdemon
						return enemiesTID[(randomTID % enemiesTID.length)];
					}
    			}
			}
		}
	}

	//Searching if tid is a weapon...
	for(var j = 0; j < weaponsTID.length; j++){
		if(tid == weaponsTID[j]){
			if(!weaponsRandomized){
				Math.seededRandom(0,weaponsTID.length);
				return tid;
			}
			else{
				//If it is, generate a random number and return a random weapon tid
				return weaponsTID[Math.seededRandom(0,weaponsTID.length)];
			}
		}
	}

	//Searching if tid is a powerup...
	for(var k = 0; k < powerupsTID.length; k++){
		if(tid == powerupsTID[k]){
			if(!powerupRandomized){
				Math.seededRandom(0,99);
				return tid;
			}
			else{

				var randomTID = Math.seededRandom(0,99);
				if(randomTID >= 0 && randomTID <= 90)
					return tid;
				else
					return weaponsTID[(randomTID % weaponsTID.length)];	//10% chance of getting a weapon in place of a powerup, done for balancing purposes
			}
		}
	}

	//Searching if tid corresponds to ammunition...
	for(var l = 0; l < ammunitionTID.length; l++){
		if(tid == ammunitionTID[l]){
			if(!ammoRandomized){
				Math.seededRandom(0,ammunitionTID.length);
				return tid;
			}
			//If it is, generate a random number and return a random ammo tid
			return ammunitionTID[Math.seededRandom(0,ammunitionTID.length)];
		}
	}

	//The tid does not correspond to a randomized type, therefore it is returned as is
	return tid;
	}






