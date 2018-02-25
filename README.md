# NetworkAndroidBattleships
A real-time network [Battleship](https://en.wikipedia.org/wiki/Battleship_(game)#Description) clone for Android built in Kotlin

This is my final project for a Mobile Application Development course I took at the University of Utah in the Fall of 2017


<h1>Application Description</h1>
 Players take turns launching missiles into individual grid locations with the goal of sinking their opponent’s ships. When a missile is launched, the player will see the board update at their chosen location to indicate whether the missile hit or missed. The game also indicates on a hit if the attacked ship was sunk, meaning that all of the locations the ship occupies have been hit. The game is won when all locations that the enemy’s ships cover have been hit. If a player's attack hits or sinks an enemy ship, they get another attack and may continue attacking until they miss. On their first miss, their turn ends and the game must not allow them to move again or change the state of the game board until their opponent finishes attacking (by missing on an attack). Once a player has won the game, the game view must prevent either player from moving and should display which player won the game. Neither player is able to see the locations of their opponent's ships, except for squares where the player knows they hit the opponent's ship with a missile.
 
 <h2>Some More Details</h2>
 
* Online data model built in [Firebase](https://firebase.google.com/) which represents the state of a game, as well as a collection model that organizes all games known to the program.
* The data model allows addition, removal, and updating of games. If a game is updated by a device other than the one the user is currently using (because the other player made a move or joined a game), then the UI represents that change nearly immediately with no user interaction.
* The application creates pseudorandom (but valid) ship positionings at the beginning of each game. 
* The application opens to a login screen, where users may either log in using their email and password or register for a new account.
* The user's email is validated before granting access to other parts of the application by sending a verification email and waiting for a response.
* Users who have gotten past the login process see a list of games, which contains summary views for each game that display information about the games.
* The application has a game screen in which games can be spectated. Spectators cannot interact with the game and view the board as the current player sees it.
