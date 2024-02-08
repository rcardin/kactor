package `in`.rcard.kactor

sealed interface Signal {
    data object Terminate : Signal
}
