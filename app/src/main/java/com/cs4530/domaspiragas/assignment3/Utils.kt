package com.cs4530.domaspiragas.assignment3

import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import com.google.gson.GsonBuilder
/** This class creates some global access methods used across multiple activities*/
object Utils{
    fun formatPlayerNode(username:String) : String{
        return username
                .replace(".", "")
                .replace("#", "")
                .replace("$", "")
                .replace("[","")
                .replace("]","")
                .toLowerCase()
    }

    fun pushGameUpdateToDB(databaseGames : DatabaseReference) {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        //set up game state for serializing
        val sergs = SerializableGameState(GameState.player1, GameState.player2,
                GameState.playerTurn, GameState.status, GameState.gameId,
                GameState.player1Name, GameState.player2Name)
        //write GameState to json
        val jsonifiedGameState: String = gson.toJson(sergs)
        databaseGames.child(GameState.gameId).setValue(jsonifiedGameState)
    }
    fun updateGameStateFromDB(newGameState: String?) {
        val gson = Gson()
        val sergs: SerializableGameState = gson.fromJson(newGameState, SerializableGameState::class.java)
        //read the values from the temp game state into our actual game state
        GameState.player1 = sergs.player1
        GameState.player2 = sergs.player2
        GameState.playerTurn = sergs.playerTurn
        GameState.status = sergs.status
        GameState.player1Name = sergs.player1Name
        GameState.player2Name = sergs.player2Name
        GameState.gameId = sergs.gameId
    }
}
