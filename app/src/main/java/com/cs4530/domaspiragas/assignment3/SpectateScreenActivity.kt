package com.cs4530.domaspiragas.assignment3

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.GridView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_spectate_screen.*

/**Screen used by a user who is spectating a match. */
class SpectateScreenActivity : AppCompatActivity() {
    private val databaseGames: DatabaseReference = FirebaseDatabase.getInstance().getReference("games")
    override fun onStart() {
        super.onStart()
        // Get the gameId for the game selected on the previous screen
        databaseGames.child(NetworkState.relevantGames[intent.extras.getString("Position").toInt()].gameId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.e("DB change cancelled", p0.toString())
            }
            override fun onDataChange(p0: DataSnapshot?) {
                Utils.updateGameStateFromDB(p0!!.getValue(String::class.java))
                setupGridAdapters()
                setupGameInfo()
            }
        })
    }
    // go to game list on back button press
    override fun onBackPressed() {
        val intent = Intent(this, GameListActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spectate_screen)
        // Setup the functionality for the "Exit Match" button.
        exitButton.setOnClickListener(View.OnClickListener {
            // go back to the list of games
            val intent = Intent(this, GameListActivity::class.java)
            startActivity(intent)
        })
    }

    /**Updates the labels describing the state of the game*/
    private fun setupGameInfo() {
        if (!GameState.status.endsWith("wins!")) { // Setup the labels this way if the game hasn't been won
            // Set up the player turn and ships remaining labels when the state changes
            val playerTurnPlaceholder: String
            if (GameState.playerTurn == 1) {
                playerTurnPlaceholder = GameState.player1Name + "'s Turn"
            } else {
                playerTurnPlaceholder = GameState.player2Name + "'s Turn"
            }
            playerTurn.text = playerTurnPlaceholder

            val yourShipsPlaceholder: String = GameState.player1Name + "'s Ships: " +
                    GameState.player1.getNumShipsRemaining()
            yourShips.text = yourShipsPlaceholder
            val opponentShipsPlaceholder: String = GameState.player2Name + "'s Ships: " +
                    GameState.player2.getNumShipsRemaining()
            opponentShips.text = opponentShipsPlaceholder

        } else { // the game has been won, setup labels in a different way
            declareWinner()
        }
    }

    /**Used for setting up the winning game state*/
    private fun declareWinner() {
        playerTurn.text = GameState.status
        val player1ShipsPlaceholder: String = GameState.player1Name + "'s Ships: " +
                GameState.player1.getNumShipsRemaining()
        yourShips.text = player1ShipsPlaceholder
        val player2ShipsPlaceholder: String = GameState.player2Name + "'s Ships: " +
                GameState.player2.getNumShipsRemaining()
        opponentShips.text = player2ShipsPlaceholder
    }

    /**Used to determine which player's ships to show in the small grid*/
    private fun getPlayerNotTurn(): Int {
        if (GameState.playerTurn == 1) {
            return 2
        }
        return 1
    }
    /**Displays the play field as the player whose turn it is sees it*/
    private fun setupGridAdapters() {
        //Setting up the grid displaying small board
        val g1: GridView = findViewById(R.id.viewGrid)
        g1.adapter = null
        g1.adapter = TileAdapter(this, 50, false, getPlayerNotTurn())

        //Setting up the grid displaying the play field (current turn)
        val g: GridView = findViewById(R.id.playGrid)
        g.adapter = null
        val adapter = TileAdapter(this, 102, true, GameState.playerTurn)
        g.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}