package pt.isel.ls.game.domain

import pt.isel.ls.utils.InvalidGenreException

enum class Genres {
    RPG,
    ADVENTURE,
    SHOOTER,
    TURNBASED,
    ACTION,
    MULTIPLAYER,
    PUZZLE,
    SURVIVAL,
    SPORTS,
    SIMULATION,
    RACING,
    BATTLEROYALE,
    PLATFORM,
    SANDBOX,
    HORROR;

    companion object {
        fun toGenre(genre: String): Genres {
            return when (genre.lowercase()) {
                "rpg" -> RPG
                "adventure" -> ADVENTURE
                "shooter" -> SHOOTER
                "turnbased" -> TURNBASED
                "action" -> ACTION
                "multiplayer" -> MULTIPLAYER
                "puzzle" -> PUZZLE
                "survival" -> SURVIVAL
                "sports" -> SPORTS
                "simulation" -> SIMULATION
                "racing" -> RACING
                "battleroyale" -> BATTLEROYALE
                "platform" -> PLATFORM
                "sandbox" -> SANDBOX
                "horror" -> HORROR
                else -> throw InvalidGenreException
            }
        }
    }
}
