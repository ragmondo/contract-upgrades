package com.upgrade

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.UpgradedContract
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.transactions.LedgerTransaction
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob
import javax.persistence.Table

// Upgraded contracts must implement the UpgradedContract interface.
// We're not upgrading the state, so we pass the same state as the input and output state.
open class NewContract : UpgradedContract<State, NewState> {
    companion object {
        val id = "com.upgrade.NewContract"
    }

    override val legacyContract = OldContract.id

    // Let's use a new state as well. Currently, there is only a 1-1 in / out state mapping.
    override fun upgrade(state: State): NewState = NewState(state.a, state.b, 10)

    override fun verify(tx: LedgerTransaction) {}

    class Action : TypeOnlyCommandData()
}


data class NewState(val a: AbstractParty, val b: AbstractParty, val new_shiny: Int = 0) : ContractState, QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is StateSchemaV2 -> StateSchemaV2.NewStateEntity(this)
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(StateSchemaV2)

    override val participants get() = listOf(a, b)
    fun newFunction() = "This doesn't do much"
}

object StateSchemaV2 : MappedSchema(NewState::class.java, 2, listOf(NewStateEntity::class.java)) {
    @Entity
    @Table(name = "new_states")
    class NewStateEntity(newState: NewState) : PersistentState() {
        @Column
        var a: String = newState.a.toString()
        @Column
        var b: String = newState.b.toString()
        @Column
        var newness: Int = newState.new_shiny
        @Column
        @Lob
        var a_key: ByteArray = newState.a.owningKey.encoded
        @Column
        @Lob
        var b_key: ByteArray = newState.b.owningKey.encoded
    }
}