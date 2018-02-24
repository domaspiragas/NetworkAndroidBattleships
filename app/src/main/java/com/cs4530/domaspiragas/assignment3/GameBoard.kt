package com.cs4530.domaspiragas.assignment3

import java.security.SecureRandom

/**Represents the 10x10 play field for the game*/
class GameBoard {
    private var tiles : MutableList<Tile> = arrayListOf() //Contains the actual tiles of the grid
    private var ships : MutableList<Ship> = arrayListOf() //Contains the ships on this game board, used to check their status (sunk or not)
    private var shipIndexes : MutableList<Int> = arrayListOf() //All tile indexes which represent ships. Used to avoid overlapping when randomly placing ships
    @Transient private val random : SecureRandom = SecureRandom()
    private var shipsRemaining : Int = 5 //Number of unsunken ships on the board
    init {
        for(i in 1..100){
            tiles.add(Tile()) // Generate 100 tiles for our game board
        }
        placeShips() // Randomly place the ships onto the game board
    }

    /**Function used to determine the results of a game board tile being shot at
     * Boolean returned is true if something was hit*/
    fun receiveShot(index:Int): Boolean{
        // avoid invalid indexes
        if(index !in 0..99){
            return false
        }
        // shoot at a blank space
        if(tiles[index].getType() == "blank"){
            miss(index)
            return false
        } else {
            // hit a ship, which one?
            for(ship in ships){
                if(ship.inPosition(index)){
                    // tell the ship it was hit
                    ship.hit(index)
                    if(ship.isSunk()){
                        sunk(ship.getPositions()) //mark tiles as sunk if the ship has sunk
                        shipsRemaining-- //ship sunk, decrease number of remaining ships
                    } else {
                        hit(index) // if the ship hasn't sunk just mark the tile as hit
                    }
                    return true
                }
            }
            return false
        }
    }
    /**Returns the tiles for the game board*/
    fun getTile(position:Int):Tile{
        return tiles[position]
    }
    /**Return the number of unsunken ships*/
    fun getNumShipsRemaining():Int {
        return shipsRemaining
    }
    /**Used for setting game board tiles to hit*/
    private fun hit(index:Int){
        tiles[index].hit()
    }
    /**Used for setting game board tiles to miss*/
    private fun miss(index:Int){
        tiles[index].miss()
    }
    /**Used for setting game board tiles to sunk*/
    private fun sunk(indexes:List<Int>){
        for(i in indexes){
            tiles[i].sunk()
        }
    }
    /**Used for setting game board tiles to ship*/
    private fun ship(indexes:List<Int>){
        ships.add(Ship(indexes))
        for(i in indexes){
            tiles[i].ship()
            shipIndexes.add(i)
        }
    }

    /**Places a 2x1 ship on the board in a random position*/
    private fun place2x1Ship(){
        if(random.nextInt(2) == 0) {
            //horizontal
            ship(findPosition(2, true))
        } else {
            //vertical
            ship(findPosition(2, false))
        }
    }
    /**Places a 3x1 ship on the board in a random position*/
    private fun place3x1Ship(){
        if(random.nextInt(2) == 0) {
            //horizontal
            ship(findPosition(3, true))
        } else {
            //vertical
            ship(findPosition(3, false))
        }
    }
    /**Places a 4x1 ship on the board in a random position*/
    private fun place4x1Ship(){
        if(random.nextInt(2) == 0) {
            //horizontal
            ship(findPosition(4, true))
        } else {
            //vertical
            ship(findPosition(4, false))
        }
    }
    /**Places a 5x1 ship on the board in a random position*/
    private fun place5x1Ship(){
        if(random.nextInt(2) == 0) {
            //horizontal
            ship(findPosition(5, true))
        } else {
            //vertical
            ship(findPosition(5, false))
        }
    }

    /**Places the ships required for a new game in random positions
     * 1 2x1, 2 3x1, 1 4x1, 1 5x1*/
    private fun placeShips(){
        place2x1Ship()
        place3x1Ship()
        place3x1Ship()
        place4x1Ship()
        place5x1Ship()
    }

    /**Helper function which takes in a length and orientation for a ship to place on the board.
     * It randomly tries positions on the board following restrictions until it succeeds.
     * These restrictions include:
     *  - No 2 ships can overlap
     *  - Ship must fit within a 10x10 grid
     *  - Ships must be placed horizontally or vertically*/
    private fun findPosition(length:Int, horizontal: Boolean): List<Int> {
        var positions : MutableList<Int> = arrayListOf()
        while(true) {
            val startIndex: Int = random.nextInt(100) //choose a random starting position
            if (!shipIndexes.contains(startIndex)) { // if the starting position is not taken, continue
                positions.add(startIndex)
                if (horizontal) {
                    for(i in 1 until length){
                        //Try expanding the ship to the right first
                        val nextIndex : Int = startIndex + i
                        if(!shipIndexes.contains(nextIndex) && nextIndex % 10 != 0 && nextIndex <= 99){ // avoid wrapping to the next row
                            positions.add(nextIndex)                                                    // or colliding with another ship
                        } else { // will wrap or collide, reset and continue
                            positions.clear()
                            positions.add(startIndex)
                            break
                        }
                    }
                    if(positions.size != length) {
                        for (i in 1 until length) {
                            //Try expanding the ship to the left
                            val nextIndex: Int = startIndex - i
                            if (!shipIndexes.contains(nextIndex) && nextIndex % 10 != 9 && nextIndex >= 0) { // avoid wrapping to row above
                                positions.add(nextIndex)                                                     // or colliding with another ship
                            } else { // will wrap or collide, reset and continue
                                positions.clear()
                                positions.add(startIndex)
                                break
                            }
                        }
                    }
                    if(positions.size == length){ // we made it through without resetting
                        return positions // return the successful position
                    }
                } else {
                    //vertical
                    for (i in 1 until length) {
                        // Try expanding the ship down first
                        val nextIndex: Int = startIndex + (i * 10)
                        if (!shipIndexes.contains(nextIndex) && nextIndex <= 99) { // avoid falling off the board,
                            positions.add(nextIndex)                               // and avoid colliding with another ship
                        } else { // collided or fell off board, reset and continue
                            positions.clear()
                            positions.add(startIndex)
                            break
                        }
                    }
                    if (positions.size != length) {
                        for (i in 1 until length) {
                            //try expanding the ship up
                            val nextIndex: Int = startIndex - (i * 10)
                            if (!shipIndexes.contains(nextIndex) && nextIndex >= 0) { // avoid falling off the board,
                                positions.add(nextIndex)                              // and avoid colliding with another ship
                            } else { // collided or fell off board, reset and continue
                                positions.clear()
                                positions.add(startIndex)
                                break
                            }
                        }
                    }
                    if(positions.size == length){ // we made it through without resetting
                        return positions // return the successful position
                    }
                }
                // If we didn't return then we will go back to the top and choose a new starting point.
            }
        }
    }
}