package com.cs4530.domaspiragas.assignment3

/**A serializable version of our GameState singleton that we can pass to and from Gson*/
data class SerializableGameState(
        val player1:GameBoard,
        val player2:GameBoard,
        val playerTurn: Int,
        val status: String,
        val gameId: String,
        val player1Name: String,
        val player2Name: String
        )