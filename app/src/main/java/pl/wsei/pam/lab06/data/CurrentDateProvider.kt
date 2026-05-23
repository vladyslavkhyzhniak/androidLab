package pl.wsei.pam.lab06.data

import java.time.LocalDate

interface CurrentDateProvider {
    val now: LocalDate
}

class DefaultCurrentDateProvider : CurrentDateProvider {
    override val now: LocalDate get() = LocalDate.now()
}
