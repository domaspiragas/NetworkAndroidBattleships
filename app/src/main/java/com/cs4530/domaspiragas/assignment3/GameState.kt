package com.cs4530.domaspiragas.assignment3

/**A singleton representing the state of our game. Persists data between activity transitions and
 * rotations*/
object GameState{
    var player1 : GameBoard = GameBoard()
    var player2 : GameBoard = GameBoard()
    var playerTurn : Int = 1
    var status : String = "New Game"
    var selectedPos : Int = -1
    var gameId : String = ""
    var player1Name : String = ""
    var player2Name : String = "Waiting For Opponent"
}