package com.couplebase.feature.wedding.vendors

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.VendorRepository
import com.couplebase.core.model.Vendor
import com.couplebase.core.model.VendorPayment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VendorListComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val repository: VendorRepository,
    private val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        VendorHandler(coupleId, repository)
    }

    val state: StateFlow<VendorListUiState> = handler.state

    fun onShowAddSheet() {
        handler.updateState { it.copy(showVendorSheet = true, editingVendor = null) }
    }

    fun onEditVendor(vendor: Vendor) {
        handler.updateState { it.copy(showVendorSheet = true, editingVendor = vendor) }
    }

    fun onDismissVendorSheet() {
        handler.updateState { it.copy(showVendorSheet = false, editingVendor = null) }
    }

    fun onAddVendor(
        name: String,
        category: String,
        phone: String?,
        email: String?,
        website: String?,
        totalCost: Double,
        notes: String?,
    ) {
        handler.addVendor(name, category, phone, email, website, totalCost, notes)
    }

    fun onUpdateVendor(vendor: Vendor) {
        handler.updateVendor(vendor)
    }

    fun onDeleteVendor(id: String) {
        handler.deleteVendor(id)
    }

    fun onSelectVendor(vendor: Vendor) {
        handler.selectVendor(vendor)
    }

    fun onBackFromDetail() {
        handler.updateState { it.copy(selectedVendor = null) }
    }

    fun onShowPaymentSheet() {
        handler.updateState { it.copy(showPaymentSheet = true, editingPayment = null) }
    }

    fun onEditPayment(payment: VendorPayment) {
        handler.updateState { it.copy(showPaymentSheet = true, editingPayment = payment) }
    }

    fun onDismissPaymentSheet() {
        handler.updateState { it.copy(showPaymentSheet = false, editingPayment = null) }
    }

    fun onAddPayment(
        description: String,
        amount: Double,
        dueDate: String,
    ) {
        handler.addPayment(description, amount, dueDate)
    }

    fun onUpdatePayment(payment: VendorPayment) {
        handler.updatePayment(payment)
    }

    fun onMarkPaymentPaid(id: String) {
        handler.markPaymentPaid(id)
    }

    fun onDeletePayment(id: String) {
        handler.deletePayment(id)
    }

    fun onBackClicked() = onBack()
}

data class VendorListUiState(
    val vendors: List<Vendor> = emptyList(),
    val isLoading: Boolean = true,
    val showVendorSheet: Boolean = false,
    val editingVendor: Vendor? = null,
    val selectedVendor: Vendor? = null,
    val payments: List<VendorPayment> = emptyList(),
    val totalPaid: Double = 0.0,
    val showPaymentSheet: Boolean = false,
    val editingPayment: VendorPayment? = null,
)

private class VendorHandler(
    private val coupleId: String,
    private val repository: VendorRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(VendorListUiState())
    val state: StateFlow<VendorListUiState> = _state.asStateFlow()
    private val selectedVendorId = MutableStateFlow<String?>(null)

    init {
        scope.launch {
            combine(
                repository.vendorsFlow(coupleId),
                selectedVendorId,
            ) { vendors, selId ->
                val selected = selId?.let { id -> vendors.find { it.id == id } }
                Triple(vendors, selected, selId)
            }.collect { (vendors, selected, _) ->
                _state.update { old ->
                    old.copy(
                        vendors = vendors,
                        isLoading = false,
                        selectedVendor = selected ?: old.selectedVendor?.let { prev ->
                            vendors.find { it.id == prev.id }
                        },
                    )
                }
            }
        }

        // Observe payments for selected vendor
        scope.launch {
            selectedVendorId.collect { vendorId ->
                if (vendorId != null) {
                    scope.launch {
                        repository.paymentsFlow(vendorId).collect { payments ->
                            val paid = payments.filter { it.isPaid }.sumOf { it.amount }
                            _state.update { old ->
                                old.copy(payments = payments, totalPaid = paid)
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateState(transform: (VendorListUiState) -> VendorListUiState) {
        _state.update(transform)
    }

    fun selectVendor(vendor: Vendor) {
        selectedVendorId.value = vendor.id
        _state.update { it.copy(selectedVendor = vendor, payments = emptyList(), totalPaid = 0.0) }
    }

    fun addVendor(
        name: String,
        category: String,
        phone: String?,
        email: String?,
        website: String?,
        totalCost: Double,
        notes: String?,
    ) {
        val vendor = Vendor(
            id = generateUuid(),
            coupleId = coupleId,
            name = name,
            category = category,
            phone = phone,
            email = email,
            website = website,
            totalCost = totalCost,
            notes = notes,
            updatedAt = "",
        )
        scope.launch { repository.upsertVendor(vendor) }
        _state.update { it.copy(showVendorSheet = false, editingVendor = null) }
    }

    fun updateVendor(vendor: Vendor) {
        scope.launch { repository.upsertVendor(vendor) }
        _state.update { it.copy(showVendorSheet = false, editingVendor = null) }
    }

    fun deleteVendor(id: String) {
        scope.launch { repository.deleteVendor(id) }
        _state.update { it.copy(selectedVendor = null) }
    }

    fun addPayment(description: String, amount: Double, dueDate: String) {
        val vendorId = _state.value.selectedVendor?.id ?: return
        val payment = VendorPayment(
            id = generateUuid(),
            vendorId = vendorId,
            coupleId = coupleId,
            description = description,
            amount = amount,
            dueDate = dueDate,
            updatedAt = "",
        )
        scope.launch { repository.upsertPayment(payment) }
        _state.update { it.copy(showPaymentSheet = false, editingPayment = null) }
    }

    fun updatePayment(payment: VendorPayment) {
        scope.launch { repository.upsertPayment(payment) }
        _state.update { it.copy(showPaymentSheet = false, editingPayment = null) }
    }

    fun markPaymentPaid(id: String) {
        scope.launch { repository.markPaymentPaid(id) }
    }

    fun deletePayment(id: String) {
        scope.launch { repository.deletePayment(id) }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
