package com.cs4530.domaspiragas.assignment3

/**A representation of a ship on the game board*/
class Ship (ship :List<Int>){
    private var _shipStatus: MutableMap<Int, String> = mutableMapOf() //manages whether ship segments are hit or ok
    private var _shipPositions : List<Int> = arrayListOf() //list of positions the ship is composed of
    init {
        _shipPositions = ship
        for(index in ship){
            _shipStatus.put(index, "ok") //initially all segments are okay
        }
    }
    /**Marks the given segment of the ship as hit*/
    fun hit(index : Int){
        _shipStatus[index] = "hit"
    }
    /**Returns whether or not the ship has been sunk*/
    fun isSunk() : Boolean {
        for(entry in _shipStatus){
            if(entry.value == "ok"){ // if any segment is okay, not sunk
                return false
            }
        }
        return true
    }
    /**Returns the positions of all ship segments*/
    fun getPositions() : List<Int>{
        return _shipPositions
    }
    /**Used to check whether a given position is part of the ship*/
    fun inPosition(position : Int) : Boolean {
        if (_shipPositions.contains(position)){
            return true
        }
        return false
    }
}