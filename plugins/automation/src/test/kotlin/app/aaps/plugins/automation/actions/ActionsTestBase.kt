package app.aaps.plugins.automation.actions

import app.aaps.core.data.model.GlucoseUnit
import app.aaps.core.data.model.OE
import app.aaps.core.data.plugin.PluginType
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.aps.Loop
import app.aaps.core.interfaces.configuration.ConfigBuilder
import app.aaps.core.interfaces.constraints.Constraint
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginDescription
import app.aaps.core.interfaces.profile.Profile
import app.aaps.core.interfaces.profile.ProfileSource
import app.aaps.core.interfaces.queue.CommandQueue
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.smsCommunicator.SmsCommunicator
import app.aaps.core.objects.constraints.ConstraintObject
import app.aaps.plugins.automation.triggers.Trigger
import app.aaps.shared.tests.TestBaseWithProfile
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito.`when`

open class
ActionsTestBase : TestBaseWithProfile() {

    open class TestLoopPlugin(
        aapsLogger: AAPSLogger,
        rh: ResourceHelper,
        pluginDescription: PluginDescription
    ) : PluginBase(
        pluginDescription, aapsLogger, rh
    ), Loop {

        private var suspended = false
        override var lastRun: Loop.LastRun? = Loop.LastRun()
        override var closedLoopEnabled: Constraint<Boolean>? = ConstraintObject(false, aapsLogger)
        override val isSuspended: Boolean = suspended
        override val isLGS: Boolean = false
        override val isSuperBolus: Boolean = false
        override val isDisconnected: Boolean = false
        override var lastBgTriggeredRun: Long = 0

        override fun invoke(initiator: String, allowNotification: Boolean, tempBasalFallback: Boolean) {}
        override fun acceptChangeRequest() {}
        override fun minutesToEndOfSuspend(): Int = 0
        override fun goToZeroTemp(durationInMinutes: Int, profile: Profile, reason: OE.Reason, action: app.aaps.core.data.ue.Action, source: Sources, listValues: List<ValueWithUnit>) {}
        override fun suspendLoop(durationInMinutes: Int, action: app.aaps.core.data.ue.Action, source: Sources, note: String?, listValues: List<ValueWithUnit>) {}
        override fun disableCarbSuggestions(durationMinutes: Int) {}
        override fun buildAndStoreDeviceStatus() {}
        override fun entries(): Array<CharSequence> = emptyArray()

        override fun entryValues(): Array<CharSequence> = emptyArray()

        override fun setPluginEnabled(type: PluginType, newState: Boolean) {}
    }

    @Mock lateinit var commandQueue: CommandQueue
    @Mock lateinit var configBuilder: ConfigBuilder
    @Mock lateinit var profilePlugin: ProfileSource
    @Mock lateinit var smsCommunicator: SmsCommunicator
    @Mock lateinit var loopPlugin: TestLoopPlugin
    @Mock lateinit var uel: UserEntryLogger
    @Mock lateinit var persistenceLayer: PersistenceLayer

    init {
        addInjector {
            if (it is Action) {
                it.aapsLogger = aapsLogger
                it.rh = rh
                it.instantiator = instantiator
            }
            if (it is ActionStopTempTarget) {
                it.dateUtil = dateUtil
                it.persistenceLayer = persistenceLayer
            }
            if (it is ActionStartTempTarget) {
                it.activePlugin = activePlugin
                it.persistenceLayer = persistenceLayer
                it.profileFunction = profileFunction
                it.dateUtil = dateUtil
                it.profileUtil = profileUtil
            }
            if (it is ActionSendSMS) {
                it.smsCommunicator = smsCommunicator
            }
            if (it is ActionProfileSwitch) {
                it.activePlugin = activePlugin
                it.profileFunction = profileFunction
                it.dateUtil = dateUtil
            }
            if (it is ActionProfileSwitchPercent) {
                it.profileFunction = profileFunction
            }
            if (it is ActionNotification) {
                it.rxBus = rxBus
            }
            if (it is ActionLoopSuspend) {
                it.loop = loopPlugin
                it.rxBus = rxBus
                it.uel = uel
            }
            if (it is ActionLoopResume) {
                it.loopPlugin = loopPlugin
                it.configBuilder = configBuilder
                it.rxBus = rxBus
                it.persistenceLayer = persistenceLayer
                it.dateUtil = dateUtil
            }
            if (it is ActionLoopEnable) {
                it.loopPlugin = loopPlugin
                it.configBuilder = configBuilder
                it.rxBus = rxBus
                it.uel = uel
            }
            if (it is ActionLoopDisable) {
                it.loopPlugin = loopPlugin
                it.configBuilder = configBuilder
                it.commandQueue = commandQueue
                it.rxBus = rxBus
                it.uel = uel
            }
            if (it is ActionCarePortalEvent) {
                it.persistenceLayer = persistenceLayer
                it.sp = sp
                it.dateUtil = dateUtil
                it.profileFunction = profileFunction
            }
            if (it is Trigger) {
                it.rh = rh
                it.profileFunction = profileFunction
            }
        }
    }

    @BeforeEach
    fun mock() {
        `when`(profileFunction.getUnits()).thenReturn(GlucoseUnit.MGDL)
        `when`(activePlugin.activeProfileSource).thenReturn(profilePlugin)
        `when`(profilePlugin.profile).thenReturn(getValidProfileStore())

        `when`(rh.gs(app.aaps.core.ui.R.string.ok)).thenReturn("OK")
        `when`(rh.gs(app.aaps.core.ui.R.string.error)).thenReturn("Error")
    }
}