package com.cs4530.domaspiragas.assignment3

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_play_screen.*
import java.security.SecureRandom

class PlayScreenActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseGames: DatabaseReference = FirebaseDatabase.getInstance().getReference("games")
    private val rand :SecureRandom = SecureRandom()
    override fun onStart() {
        super.onStart()

        databaseGames.child(GameState.gameId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                //Don't care about this
            }

            override fun onDataChange(p0: DataSnapshot?) {
                Utils.updateGameStateFromDB(p0!!.getValue(String::class.java))
                setupGridAdapters()
                setupGameInfo()
                preventInteractionIfNoOpponent()
            }
        })
    }
    override fun onBackPressed() {
        val intent = Intent(this, GameListActivity::class.java)
        startActivity(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_screen)

        //Setting up the "Fire!" button's click functionality
        fireButton.setOnClickListener(View.OnClickListener {
            var hit: Boolean
            if (GameState.selectedPos != -1) { // only fire if a position on the grid is selected
                if (GameState.playerTurn == 1) { // player1 shoots at player2's board
                    hit = GameState.player2.receiveShot(GameState.selectedPos)
                } else { // player2 shoots at player1's board
                    hit = GameState.player1.receiveShot(GameState.selectedPos)
                }
                if (GameState.status == "New Game") {
                    GameState.status = "In Progress" // change new game to in progress after first move
                }
                if(!hit) {
                    if (GameState.playerTurn == 1) {
                        GameState.playerTurn = 2
                    } else {
                        GameState.playerTurn = 1
                    }
                    //reset the grid selection in case previous player made another selection after shooting
                    GameState.selectedPos = -1
                    //fireButton.isEnabled = false //disable fire button after shooting
                }
                else{
                    Toast.makeText(this, " Hit! Fire Again!", Toast.LENGTH_SHORT).show()
                }
                GameState.selectedPos = -1 // reset the grid selection after shooting
                updateGameState()
                //startActivity(intent) //TODO: Remove this when client side doesn't switch views
            } else { // no position on the grid is selected, tell the player
                Toast.makeText(this, "Select a position to fire at!", Toast.LENGTH_SHORT).show()
            }
        })
        // Setup the functionality for the "Exit Match" button.
        exitButton.setOnClickListener(View.OnClickListener {
            // go back to the list of games
            val intent = Intent(this, GameListActivity::class.java)
            startActivity(intent)
        })
    }
    private fun preventInteractionIfNoOpponent(){
        if(GameState.player2Name == "Waiting For Opponent" || GameState.playerTurn != getWhichPlayer() || GameState.status.endsWith("wins!")){
            preventInteraction()
        } else {
            enableInteraction()
        }
    }
    private fun getWhichPlayer(): Int{
        val username: String = firebaseAuth.currentUser!!.email.toString().trim()
        if(GameState.player1Name == username){
            return 1
        }
        return 2
    }
    private fun getWhichOpponent(): Int{
        if(getWhichPlayer() == 1){
            return 2
        }
        return 1
    }
    private fun setupGameInfo(){
        if (!GameState.status.endsWith("wins!")) { // Setup the labels this way if the game hasn't been won
            // Set up the player turn and ships remaining labels when the activity is loaded
            val playerTurnPlaceholder: String
            if (GameState.playerTurn == 1) {
                playerTurnPlaceholder = GameState.player1Name +"'s Turn"
            } else {
                playerTurnPlaceholder = GameState.player2Name +"'s Turn"
            }
            playerTurn.text = playerTurnPlaceholder
            if(GameState.player1Name == firebaseAuth.currentUser!!.email.toString().trim()){
                val yourShipsPlaceholder: String = getString(R.string.your_ships) + " " +
                        GameState.player1.getNumShipsRemaining()
                yourShips.text = yourShipsPlaceholder
                val opponentShipsPlaceholder: String = getString(R.string.opponent_ships) + " " +
                        GameState.player2.getNumShipsRemaining()
                opponentShips.text = opponentShipsPlaceholder
            } else {
                val yourShipsPlaceholder: String = getString(R.string.your_ships) + " " +
                        GameState.player2.getNumShipsRemaining()
                yourShips.text = yourShipsPlaceholder
                val opponentShipsPlaceholder: String = getString(R.string.opponent_ships) + " " +
                        GameState.player1.getNumShipsRemaining()
                opponentShips.text = opponentShipsPlaceholder
            }
        } else { // the game has been won, setup labels in a different way
            declareWinner(GameState.status.replace(" wins!", ""))
        }
    }
    private fun setupGridAdapters(){
        //Setting up the grid displaying your board
        val g1: GridView = findViewById(R.id.viewGrid)
        g1.adapter = null
        g1.adapter = TileAdapter(this, 50, false, getWhichOpponent())

        //Setting up the grid displaying the play field (your opponent's board)
        val g: GridView = findViewById(R.id.playGrid)
        g.adapter = null
        val adapter = TileAdapter(this, 102, true, getWhichPlayer())
        g.adapter = adapter
        g.onItemClickListener = (object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, v: View, position: Int, id: Long) {
                if (getWhichOpponent() == 2) { // if it's player1's turn, shoot at player2's board
                    if (GameState.player2.getTile(position).getType() == "blank" || GameState.player2.getTile(position).getType() == "ship") {
                        //don't allow selection of HIT MISS or SUNK squares
                        GameState.selectedPos = position // highlight grid selection
                        adapter.notifyDataSetChanged()
                    }
                } else { // player2's turn, shoot at player1's board
                    if (GameState.player1.getTile(position).getType() == "blank" || GameState.player1.getTile(position).getType() == "ship") {
                        //don't allow selection of HIT MISS or SUNK squares
                        GameState.selectedPos = position // highlight grid selection
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
        adapter.notifyDataSetChanged()
    }
    /**Used to update the status of the game when a missile is fired.
     * Updates the number of ships remaining, and whether the game has ended, and who won.*/
    private fun updateGameState() {
        if (GameState.playerTurn == 1) {
            if (GameState.player2.getNumShipsRemaining() == 0) {
                //Player 1 wins
                declareWinner(GameState.player1Name)
            }
            // Update the ships remaining label
            val opponentShipsPlaceholder: String = getString(R.string.opponent_ships) + " " +
                    GameState.player2.getNumShipsRemaining()
            opponentShips.text = opponentShipsPlaceholder
        } else {
            if (GameState.player1.getNumShipsRemaining() == 0) {
                //Player 2 wins
                declareWinner(GameState.player2Name)
            }
            if (GameState.status == "In Progress") {
                // Update the ships remaining label
                val opponentShipsPlaceholder: String = getString(R.string.opponent_ships) + " " +
                        GameState.player1.getNumShipsRemaining()
                opponentShips.text = opponentShipsPlaceholder
            }
        }
        Utils.pushGameUpdateToDB(FirebaseDatabase.getInstance().getReference("games"))
    }

    /**Used for setting up the winning game state*/
    private fun declareWinner(winner: String) {
        GameState.status =winner + " wins!"

        playerTurn.text = GameState.status

        val player1ShipsPlaceholder: String = GameState.player1Name + "'s Ships: " +
                GameState.player1.getNumShipsRemaining()
        yourShips.text = player1ShipsPlaceholder
        val player2ShipsPlaceholder: String = GameState.player2Name + "'s Ships: " +
                GameState.player2.getNumShipsRemaining()
        opponentShips.text = player2ShipsPlaceholder
        preventInteraction()
    }
    private fun preventInteraction(){
        fireButton.isEnabled = false
        playGrid.isEnabled = false
    }
    private fun enableInteraction(){
        fireButton.isEnabled = true
        playGrid.isEnabled = true
    }
}
