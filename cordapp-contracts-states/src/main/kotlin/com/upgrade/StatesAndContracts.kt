package com.upgrade

import kotlinx.html.A
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.LedgerTransaction
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob
import javax.persistence.Table

data class State(val a: AbstractParty, val b: AbstractParty) : ContractState, QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is StateSchemaV1 -> StateSchemaV1.StateEntity(this)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(StateSchemaV1)
    override val participants get() = listOf(a, b)

}

open class OldContract : Contract {
    companion object {
        val id = "com.upgrade.OldContract"
    }
    override fun verify(tx: LedgerTransaction) {}
    class Action : TypeOnlyCommandData()
}

object StateSchemaV1 : MappedSchema(State::class.java, 1, listOf(StateEntity::class.java)) {
    @Entity
    @Table(name = "states")
    class StateEntity(state: State) : PersistentState() {
        @Column
        var a: String = state.a.toString()
        @Column
        var b: String = state.b.toString()
        @Column
        @Lob
        var a_key: ByteArray = state.a.owningKey.encoded
        @Column
        @Lob
        var b_key: ByteArray = state.b.owningKey.encoded
    }
}
