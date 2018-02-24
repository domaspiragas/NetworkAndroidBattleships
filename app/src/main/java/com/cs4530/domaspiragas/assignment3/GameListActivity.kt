package com.cs4530.domaspiragas.assignment3

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_game_list.*

class GameListActivity : AppCompatActivity() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseGames: DatabaseReference = FirebaseDatabase.getInstance().getReference("games")

    override fun onStart() {
        super.onStart()
        databaseGames.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.e("DB change cancelled", p0.toString())
            }
            override fun onDataChange(p0: DataSnapshot?) {
                NetworkState.relevantGames.clear() // Clear list of games
                for (dataSnapshot: DataSnapshot in p0!!.children) {
                    addGameIfRelevant(dataSnapshot.getValue(String::class.java)) // get only games relevant to player
                }
                setupViewAdapter()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_list)

        // the new button is used to create and load a new game
        newButton.setOnClickListener(View.OnClickListener {
            createNewGame()
            val intent = Intent(this, PlayScreenActivity::class.java)
            startActivity(intent)
        })

        // the join button is used to join a game selected from the list
        joinButton.setOnClickListener(View.OnClickListener {
            if (gameList.checkedItemPosition != -1) { // if a game is selected try to join the game
                if(joinGame(gameList.checkedItemPosition)) {
                    Utils.pushGameUpdateToDB(databaseGames)
                    val intent = Intent(this, PlayScreenActivity::class.java)
                    startActivity(intent)
                }
            } else {
                //if no game is selected in the list, display a message
                Toast.makeText(this, "Please select the game you want to load.", Toast.LENGTH_SHORT).show()
            }
        })

        // the delete button is used to delete a game selected from the list
        deleteButton.setOnClickListener(View.OnClickListener {
            if (gameList.checkedItemPosition != -1) { // if a game is selected try to delete it
                if (gameList.checkedItemPosition == gameList.count - 1) { // if the last game in the list is selected
                    deleteGame(gameList.checkedItemPosition)                         // we need to un-check the index it was in
                    gameList.setItemChecked(gameList.checkedItemPosition, false)//otherwise it's possible to attempt to remove something which is null
                } else {
                    deleteGame(gameList.checkedItemPosition) // if not the last game in the list, try to remove it
                }
            } else {
                //if no game is selected in the list, display a message
                Toast.makeText(this, "Please select the game you want to delete.", Toast.LENGTH_SHORT).show()
            }
        })

        // the logout button is used to log out the currently signed in user
        logoutButton.setOnClickListener(View.OnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        })

        // the watch button is used to join a game as a spectator
        watchButton.setOnClickListener(View.OnClickListener {
            watchGame(gameList.checkedItemPosition)
        })
    }

    /**Used to join a game as a spectator if the user meets the required criteria*/
    private fun watchGame(position : Int){
        val sergs: SerializableGameState = NetworkState.relevantGames[position]
        val username:String = firebaseAuth.currentUser!!.email.toString().trim()

        if(sergs.player1Name != username && sergs.player2Name != username){
            val intent = Intent(this, SpectateScreenActivity::class.java)
            intent.putExtra("Position", position.toString()) // Pass along the game we want to spectate
            startActivity(intent)
        } else {
            Toast.makeText(this, "You can't spectate your own games!", Toast.LENGTH_SHORT).show()
        }
    }

    /**Used to setup the list of games*/
    private fun setupViewAdapter() {
        // setup the list of games
        val lv: ListView = findViewById(R.id.gameList)
        val adapter = GameListAdapter(this)
        lv.adapter = adapter
    }

    /**Determines whether a game passed in as JSON is relevant to the player
     * Relevant meaning they can join, watch, or delete it
     * Irrelevant games are those which are complete and did not include the user as a player*/
    private fun addGameIfRelevant(sgsAsJson: String?) {
        val gson = Gson()
        val sgs: SerializableGameState = gson.fromJson(sgsAsJson, SerializableGameState::class.java) // game object from the DB
        val username: String = firebaseAuth.currentUser!!.email.toString().trim()
        if (sgs.player1Name != username && sgs.player2Name != username && sgs.status != "New Game" && sgs.status != "In Progress") {
            return
        } else {
            NetworkState.relevantGames.add(sgs) // Singleton managing relevant network games
        }
    }

    /**Sets up the state of a new game and pushes it to the DB*/
    private fun createNewGame() {
        //set up state of a new game
        GameState.player1 = GameBoard()
        GameState.player2 = GameBoard()
        GameState.selectedPos = -1
        GameState.status = "New Game"
        GameState.playerTurn = 1
        GameState.gameId = databaseGames.push().key
        GameState.player1Name = firebaseAuth.currentUser!!.email.toString().trim()
        GameState.player2Name = "Waiting For Opponent"
        //update player's games
        addGameToPlayer()
        Utils.pushGameUpdateToDB(databaseGames)
    }

    /**Pull the player's data, add a game to it, and push it back to the DB*/
    private fun addGameToPlayer(){
        val databasePlayer: DatabaseReference = FirebaseDatabase.getInstance().getReference("players")
        databasePlayer.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.e("DB change cancelled", p0.toString())
            }
            override fun onDataChange(p0: DataSnapshot?) {
                for (dataSnapshot: DataSnapshot in p0!!.children) {
                    val playerString = dataSnapshot.getValue(String::class.java)
                    val gson = Gson()
                    val player: Player = gson.fromJson(playerString, Player::class.java) // construct player form DB data
                    val gsonB: Gson = GsonBuilder().setPrettyPrinting().create()
                    if (player.getUsername() == Utils.formatPlayerNode(firebaseAuth.currentUser!!.email.toString().trim())) {
                        player.addGame(GameState.gameId) // update constructed player
                        val jsonified: String = gsonB.toJson(player)
                        databasePlayer.child(player.getUsername()).setValue(jsonified) // push new updated player data to DB
                    }
                }
            }
        })
    }
    /**Used to join a game as a player if an open spot is available, or if the user is already
     * involved in the game*/
    private fun joinGame(position: Int): Boolean {
        val sergs: SerializableGameState = NetworkState.relevantGames[position]
        val username: String = firebaseAuth.currentUser!!.email.toString().trim()
        if(sergs.player1Name != username && sergs.player2Name != username && sergs.player2Name != "Waiting For Opponent"){
            Toast.makeText(this, "Game is full, cannot join. Choose another game, or watch instead!", Toast.LENGTH_SHORT).show()
            return false
        }else if(sergs.player1Name != username && sergs.player2Name == "Waiting For Opponent") { // join empty spot
            GameState.player2Name = username
            addGameToPlayer()
        } else {
            GameState.player2Name = sergs.player2Name // open game user has already joined before
        }
        //read the values from the temp game state into our actual game state
        GameState.player1 = sergs.player1
        GameState.player2 = sergs.player2
        GameState.playerTurn = sergs.playerTurn
        GameState.status = sergs.status
        GameState.player1Name = sergs.player1Name
        GameState.gameId = sergs.gameId
        return true
    }

    /**Used to remove games a user has completed. Cannot remove games which are in progress, or
     * games which the user was not involved in*/
    private fun deleteGame(position: Int) {
        val sergs: SerializableGameState = NetworkState.relevantGames[position]
        val username: String = firebaseAuth.currentUser!!.email.toString().trim()
        if ((sergs.player1Name == username || sergs.player2Name == username) && sergs.status.endsWith("wins!")) {
            databaseGames.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    Log.e("DB change cancelled", p0.toString())
                }
                override fun onDataChange(p0: DataSnapshot?) {
                    if (sergs.status.endsWith("wins!") && (sergs.player1Name == username || sergs.player2Name == username)) {
                        p0!!.child(sergs.gameId).ref.removeValue()
                    }
                }
            })
            val databasePlayer: DatabaseReference = FirebaseDatabase.getInstance().getReference("players")
            databasePlayer.addListenerForSingleValueEvent(object : ValueEventListener { // Update the player's games after deletion
                override fun onCancelled(p0: DatabaseError?) {
                    Log.e("DB change cancelled", p0.toString())
                }
                override fun onDataChange(p0: DataSnapshot?) {
                    for (dataSnapshot: DataSnapshot in p0!!.children) {
                        val playerString = dataSnapshot.getValue(String::class.java)
                        val gson = Gson()
                        val player: Player = gson.fromJson(playerString, Player::class.java) // construct player from DB data
                        val gsonB: Gson = GsonBuilder().setPrettyPrinting().create()
                        if (player.getUsername() == Utils.formatPlayerNode(firebaseAuth.currentUser!!.email.toString().trim())) {
                            player.removeGame(GameState.gameId) // update constructed player
                            val jsonified: String = gsonB.toJson(player)
                            databasePlayer.child(player.getUsername()).setValue(jsonified) // push newly updated player data to DB
                        }
                    }
                }
            })
        } else {
            Toast.makeText(this, "Cannot delete unfinished games or games you are not a part of!", Toast.LENGTH_LONG).show()
        }
    }
}
