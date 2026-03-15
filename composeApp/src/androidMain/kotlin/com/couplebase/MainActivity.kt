package com.couplebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.couplebase.core.datastore.PlatformStorage
import com.couplebase.core.datastore.PreferencesDataStoreImpl
import com.couplebase.di.StubAuthRepository
import com.couplebase.di.StubBudgetRepository
import com.couplebase.di.StubChecklistRepository
import com.couplebase.di.StubCoupleRepository
import com.couplebase.di.StubGuestRepository
import com.couplebase.di.StubTimelineRepository
import com.couplebase.di.StubVendorRepository
import com.couplebase.navigation.RootComponent
import com.couplebase.navigation.RootContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesDataStore = PreferencesDataStoreImpl(PlatformStorage(this))

        val rootComponent = RootComponent(
            componentContext = defaultComponentContext(),
            authRepository = StubAuthRepository(preferencesDataStore),
            coupleRepository = StubCoupleRepository(),
            checklistRepository = StubChecklistRepository(),
            budgetRepository = StubBudgetRepository(),
            guestRepository = StubGuestRepository(),
            vendorRepository = StubVendorRepository(),
            timelineRepository = StubTimelineRepository(),
        )

        setContent {
            RootContent(rootComponent)
        }
    }
}
