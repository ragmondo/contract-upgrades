package com.template

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

open class NewContract : Contract {
    companion object {
        val id = "com.template.NewContract"
    }

    override fun verify(tx: LedgerTransaction) {}

    class Action : TypeOnlyCommandData()
}