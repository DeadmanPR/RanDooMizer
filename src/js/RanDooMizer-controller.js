var wadFile = new Array();
var seed;
var doomVersion;

function fileSelected(){
    //File Reader instance
    var reader = new FileReader();

    //When the file is loaded, ask for seed and disable the "Choose File" button
    reader.onloadend = function(event){
        wadFile = (event.target.result);
        if(doomVersion !== undefined)
            $("#seedConfirmation").modal();
        document.getElementById("file-upload").disabled = true;
        document.getElementById("options").style.display = 'block';
    }

    //Get the file selected
    var wad = document.getElementById("file-upload").files[0];
    var wadFilename = document.getElementById("file-upload").files[0].name;
    if(wadFilename.toLowerCase() === "doom1.wad")
        doomVersion = 1;
    else if(wadFilename.toLowerCase() === "doom2.wad")
        doomVersion = 2;
    
    else{
        console.log('Doom version not detected');
         $('#doomSelector').modal();
    }
    
    
    //Read the file as binary
    reader.readAsArrayBuffer(wad);
}


function setSeed(){
    seed = document.getElementById("seed").value;

    var enteredSeed = parseInt(seed);

    if(seed === '' || seed === undefined){
        document.getElementById("blank-seed-placeholder").innerHTML = "<div class=\"alert alert-danger\">"+
        "<strong>Error!</strong> Blank seeds are not allowed!"+
        "</div>";
    }
    else if(isNaN(enteredSeed)){
         document.getElementById("blank-seed-placeholder").innerHTML = "<div class=\"alert alert-danger\">"+
        "<strong>Error!</strong> The seed has to be a number!"+
        "</div>";
    }
    else{
        document.getElementById("blank-seed-placeholder").innerHTML = "";
        $("#seedInput").modal("hide");
    }

   
}

function setDoomVersion(version){
    doomVersion = parseInt(version);   
}

function checkButtonStatus(){
    if((document.getElementById("enemies").checked || document.getElementById("weapons").checked ||
        document.getElementById("powerups").checked || document.getElementById("ammo").checked) &&
        (document.getElementById("easy").checked || document.getElementById("medium").checked ||
        document.getElementById("hard").checked))
            document.getElementById("randomize-button").disabled = false;

    else
            document.getElementById("randomize-button").disabled = true;
}

function startRandomize(){
    document.getElementById("page").style.opacity = 0.2;
    document.getElementById("loading-spinner").style.display = 'block';
    document.getElementById("loading-spinner-text").style.display = 'block';
    var enemiesRandomized = document.getElementById("enemies").checked;
    var weaponsRandomized = document.getElementById("weapons").checked;
    var powerupsRandomized = document.getElementById("powerups").checked;
    var ammoRandomized = document.getElementById("ammo").checked;

    var difficulty;
    if(document.getElementById("easy").checked)
        difficulty = 0;
    else if (document.getElementById("medium").checked)
        difficulty = 1;
    else
        difficulty = 2;

    if(seed === undefined){
        seed = Math.floor(Math.random() * (2147483647 - -2147483648)) + 2147483647;
    }

   
    randomize(wadFile,doomVersion,difficulty,enemiesRandomized,weaponsRandomized,powerupsRandomized,ammoRandomized,seed);
}
