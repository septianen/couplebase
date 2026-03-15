package com.couplebase

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.couplebase.core.datastore.PlatformStorage
import com.couplebase.core.datastore.PreferencesDataStoreImpl
import com.couplebase.di.StubAuthRepository
import com.couplebase.di.StubBudgetRepository
import com.couplebase.di.StubChecklistRepository
import com.couplebase.di.StubCoupleRepository
import com.couplebase.di.StubGuestRepository
import com.couplebase.di.StubCommunicationRepository
import com.couplebase.di.StubFinanceRepository
import com.couplebase.di.StubLifeGoalRepository
import com.couplebase.di.StubMilestoneRepository
import com.couplebase.di.StubTimelineRepository
import com.couplebase.di.StubVendorRepository
import com.couplebase.navigation.RootComponent
import com.couplebase.navigation.RootContent

/**
 * Creates the main view controller.
 * @param deepLinkUri Optional deep link URI from iOS (e.g. universal link or custom scheme).
 *                    Pass from AppDelegate/SceneDelegate when handling openURL.
 */
fun MainViewController(deepLinkUri: String? = null) = ComposeUIViewController {
    val lifecycle = LifecycleRegistry()
    val preferencesDataStore = PreferencesDataStoreImpl(PlatformStorage())
    val rootComponent = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        authRepository = StubAuthRepository(preferencesDataStore),
        coupleRepository = StubCoupleRepository(),
        checklistRepository = StubChecklistRepository(),
        budgetRepository = StubBudgetRepository(),
        guestRepository = StubGuestRepository(),
        vendorRepository = StubVendorRepository(),
        timelineRepository = StubTimelineRepository(),
        milestoneRepository = StubMilestoneRepository(),
        lifeGoalRepository = StubLifeGoalRepository(),
        financeRepository = StubFinanceRepository(),
        communicationRepository = StubCommunicationRepository(),
        preferencesDataStore = preferencesDataStore,
        deepLinkUri = deepLinkUri,
    )
    RootContent(rootComponent)
}
