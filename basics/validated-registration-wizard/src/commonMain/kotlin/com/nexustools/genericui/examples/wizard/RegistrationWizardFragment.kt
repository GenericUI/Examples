package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.feedback.Alert
import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.form.CheckBox
import com.nexustools.genericui.component.form.PasswordField
import com.nexustools.genericui.component.form.RangeSlider
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Text
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FontWeight
import com.nexustools.genericui.enums.bootstrap.ShadowElevation
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.style.color.RgbaColor
import kotlin.math.roundToInt

/**
 * Multi-step validated onboarding wizard [Fragment] for the Validated Registration Wizard example.
 *
 * Step 1: Account Credentials (`input-username`, `input-email`, `input-password`)
 * Step 2: Developer Profile (`select-role`, `slider-experience`)
 * Step 3: Review & Terms (`check-terms`, `btn-submit`)
 */
@MainAppFragment
class RegistrationWizardFragment(
    val preferenceStore: PreferenceStore = PreferenceStore(RegistrationProfile.DEFAULT_STORE_NAME)
) : Fragment() {

    var currentStep: Int = 1
        private set

    var profile: RegistrationProfile = RegistrationProfile.loadFrom(preferenceStore) ?: RegistrationProfile()
        private set

    var isSubmitted: Boolean = false
        private set

    val wizardProgress: ProgressBar = ProgressBar(
        progress = 1f / 3f,
        indeterminate = false,
        type = BootstrapType.PRIMARY
    ).apply {
        id = "wizard-progress"
        label = "Step 1 of 3"
    }

    val currentStepBadge: Badge = Badge(
        text = "Step 1 of 3: Account Credentials",
        type = BootstrapType.PRIMARY,
        pill = true
    ).apply {
        id = "badge-current-step"
    }

    val validationMessageBadge: Badge = Badge(
        text = "Enter your credentials to continue",
        type = BootstrapType.INFO
    ).apply {
        id = "validation-message"
    }

    val validationAlert: Alert = Alert(
        message = "Complete all required fields in each step before advancing.",
        type = BootstrapType.INFO
    ).apply {
        id = "validation-alert"
    }

    // Step 1 controls
    val usernameInput: TextField = TextField(
        value = profile.username,
        placeholder = "Username (min 3 chars)"
    ).apply {
        id = "input-username"
    }

    val emailInput: TextField = TextField(
        value = profile.email,
        placeholder = "developer@example.com"
    ).apply {
        id = "input-email"
    }

    val passwordInput: PasswordField = PasswordField(
        value = "",
        placeholder = "Password (min 6 chars)"
    ).apply {
        id = "input-password"
    }

    // Step 2 controls
    val roleSelect: Select = Select(
        options = RegistrationProfile.AVAILABLE_ROLES.map { SelectOption(it, it) },
        selectedValue = profile.role
    ).apply {
        id = "select-role"
    }

    val experienceSlider: RangeSlider = RangeSlider(
        minValue = profile.experienceYears.toDouble(),
        maxValue = 30.0,
        min = 0.0,
        max = 30.0,
        step = 1.0
    ).apply {
        id = "slider-experience"
        getStyle().apply {
            accentColor = RgbaColor.fromHex("#10B981")
        }
    }

    val experienceYearsBadge: Badge = Badge(
        text = "${profile.experienceYears} years",
        type = BootstrapType.SECONDARY
    ).apply {
        id = "badge-experience-years"
        getBootstrap().apply {
            type = BootstrapType.SECONDARY
            fontWeight = FontWeight.SEMIBOLD
        }
    }

    // Step 3 controls
    val summaryUsernameText: Text = Text("Username: -").apply {
        id = "summary-username"
    }
    val summaryEmailText: Text = Text("Email: -").apply {
        id = "summary-email"
    }
    val summaryRoleText: Text = Text("Role: Developer").apply {
        id = "summary-role"
    }
    val summaryExperienceText: Text = Text("Experience: 3 years").apply {
        id = "summary-experience"
    }

    val termsCheckBox: CheckBox = CheckBox(
        label = "I accept the Terms of Service & Privacy Policy",
        checked = profile.acceptedTerms
    ).apply {
        id = "check-terms"
        getStyle().apply {
            accentColor = RgbaColor.fromHex("#10B981")
        }
    }

    val submitButton: Button = Button(
        text = "Submit Registration",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.MD
    ).apply {
        id = "btn-submit"
        getBootstrap().apply {
            type = BootstrapType.SUCCESS
            size = BootstrapSize.MD
            fontWeight = FontWeight.SEMIBOLD
        }
    }

    // Navigation buttons
    val backButton: Button = Button(
        text = "Back",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.MD
    ).apply {
        id = "btn-back"
        getBootstrap().apply {
            type = BootstrapType.SECONDARY
            size = BootstrapSize.MD
        }
    }

    val nextButton: Button = Button(
        text = "Next",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.MD
    ).apply {
        id = "btn-next"
        getBootstrap().apply {
            type = BootstrapType.PRIMARY
            size = BootstrapSize.MD
            fontWeight = FontWeight.SEMIBOLD
        }
    }

    val step1Panel: VStack = VStack(gap = SpacingScale.TWO).apply {
        id = "step-1-panel"
        addClass("wizard-step-panel")
        getBootstrap().apply {
            padding = SpacingScale.THREE
            borderRadius = BorderRadiusStyle.MEDIUM
        }
    }

    val step2Panel: VStack = VStack(gap = SpacingScale.TWO).apply {
        id = "step-2-panel"
        addClass("wizard-step-panel")
        getBootstrap().apply {
            padding = SpacingScale.THREE
            borderRadius = BorderRadiusStyle.MEDIUM
        }
    }

    val step3Panel: VStack = VStack(gap = SpacingScale.TWO).apply {
        id = "step-3-panel"
        addClass("wizard-step-panel")
        getBootstrap().apply {
            padding = SpacingScale.THREE
            borderRadius = BorderRadiusStyle.MEDIUM
        }
    }

    private var uiBuilt: Boolean = false
    private var lastObservedTermsChecked: Boolean = profile.acceptedTerms

    init {
        ensureUiBuilt()
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        if (!uiBuilt || children.isEmpty()) {
            uiBuilt = false
            ensureUiBuilt()
        }
        scheduleTermsSyncLoop()
    }

    private fun scheduleTermsSyncLoop() {
        setTimeout(1) {
            syncTermsCheckBoxIfNeeded()
            scheduleTermsSyncLoop()
        }
    }

    private fun syncTermsCheckBoxIfNeeded() {
        val currentChecked = termsCheckBox.isChecked
        if (currentChecked != lastObservedTermsChecked) {
            lastObservedTermsChecked = currentChecked
            profile = profile.copy(acceptedTerms = currentChecked)
            if (currentStep == 3) {
                updateLiveValidationFeedback()
            }
        }
    }

    private fun ensureUiBuilt() {
        if (uiBuilt) return
        uiBuilt = true
        clearChildren()
        addClass("wizard-root")

        val rootStack = VStack(gap = SpacingScale.THREE)

        val headerRow = HStack(gap = SpacingScale.TWO)
        headerRow.addChild(Heading("Developer Onboarding Wizard", level = 3))
        headerRow.addChild(currentStepBadge)
        headerRow.addChild(validationMessageBadge)
        rootStack.addChild(headerRow)
        rootStack.addChild(wizardProgress)
        rootStack.addChild(validationAlert)

        // Step 1 Card Content
        step1Panel.clearChildren()
        step1Panel.addChild(Heading("Step 1: Account Credentials", level = 4))
        step1Panel.addChild(Text("Choose a unique username, valid email address, and secure password."))
        step1Panel.addChild(usernameInput)
        step1Panel.addChild(emailInput)
        step1Panel.addChild(passwordInput)

        // Step 2 Card Content
        step2Panel.clearChildren()
        step2Panel.addChild(Heading("Step 2: Developer Profile", level = 4))
        step2Panel.addChild(Text("Select your primary engineering role and years of experience."))
        val roleRow = HStack(gap = SpacingScale.TWO)
        roleRow.addChild(Text("Primary Role:"))
        roleRow.addChild(roleSelect)
        step2Panel.addChild(roleRow)

        val expRow = HStack(gap = SpacingScale.TWO)
        expRow.addChild(Text("Experience (Years):"))
        expRow.addChild(experienceSlider)
        expRow.addChild(experienceYearsBadge)
        step2Panel.addChild(expRow)

        // Step 3 Card Content
        step3Panel.clearChildren()
        step3Panel.addChild(Heading("Step 3: Review & Terms", level = 4))
        step3Panel.addChild(summaryUsernameText)
        step3Panel.addChild(summaryEmailText)
        step3Panel.addChild(summaryRoleText)
        step3Panel.addChild(summaryExperienceText)
        step3Panel.addChild(termsCheckBox)
        step3Panel.addChild(submitButton)

        val wizardCard = Card("Onboarding Steps").apply {
            addClass("wizard-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.SMALL
                padding = SpacingScale.THREE
            }
        }
        wizardCard.addChild(step1Panel)
        wizardCard.addChild(step2Panel)
        wizardCard.addChild(step3Panel)
        rootStack.addChild(wizardCard)

        val footerRow = HStack(gap = SpacingScale.TWO).apply {
            addClass("wizard-footer")
        }
        footerRow.addChild(backButton)
        footerRow.addChild(nextButton)
        rootStack.addChild(footerRow)

        addChild(rootStack)
        wireHandlers()
        syncStepVisibilityAndSummary()
    }

    private fun wireHandlers() {
        usernameInput.onValueChanged = { updated ->
            profile = profile.copy(username = updated.trim())
            updateLiveValidationFeedback()
        }
        emailInput.onValueChanged = { updated ->
            profile = profile.copy(email = updated.trim())
            updateLiveValidationFeedback()
        }
        passwordInput.onValueChanged = {
            updateLiveValidationFeedback()
        }
        roleSelect.onSelectionChanged = { selected ->
            profile = profile.copy(role = selected)
            refreshReviewSummary()
            updateLiveValidationFeedback()
        }
        experienceSlider.onValueChanged = { sliderVal ->
            val years = sliderVal.roundToInt().coerceIn(0, 50)
            profile = profile.copy(experienceYears = years)
            experienceYearsBadge.text = "$years years"
            refreshReviewSummary()
        }
        termsCheckBox.onCheckedChanged = { checked ->
            lastObservedTermsChecked = checked
            profile = profile.copy(acceptedTerms = checked)
            updateLiveValidationFeedback()
        }
        termsCheckBox.setOnClick {
            lastObservedTermsChecked = termsCheckBox.isChecked
            profile = profile.copy(acceptedTerms = termsCheckBox.isChecked)
            updateLiveValidationFeedback()
        }
        backButton.setOnClick {
            goToPreviousStep()
        }
        nextButton.setOnClick {
            goToNextStep()
        }
        submitButton.setOnClick {
            submitRegistration()
        }
    }

    fun validateStep1(): String? {
        val uname = usernameInput.value.trim()
        val email = emailInput.value.trim()
        val pass = passwordInput.value
        if (uname.length < 3) {
            return "Invalid username: must be at least 3 characters."
        }
        if (!email.contains("@") || !email.contains(".") || email.startsWith("@") || email.endsWith(".")) {
            return "Invalid email: please enter a valid email address."
        }
        if (pass.length < 6) {
            return "Invalid password: must be at least 6 characters."
        }
        return null
    }

    fun validateStep2(): String? {
        val selectedRole = roleSelect.selectedValue?.trim().orEmpty()
        if (selectedRole.isEmpty()) {
            return "Please select a developer role."
        }
        return null
    }

    fun validateStep3(): String? {
        val step1Error = validateStep1()
        if (step1Error != null) return step1Error
        val step2Error = validateStep2()
        if (step2Error != null) return step2Error
        if (!termsCheckBox.isChecked) {
            return "Please accept the Terms of Service before submitting."
        }
        return null
    }

    fun goToNextStep(): Boolean {
        syncProfileFromInputs()
        when (currentStep) {
            1 -> {
                val err = validateStep1()
                if (err != null) {
                    setValidationStatus(err, BootstrapType.DANGER)
                    return false
                }
                currentStep = 2
                syncStepVisibilityAndSummary()
                setValidationStatus("Step 2: Configure your developer profile", BootstrapType.INFO)
                return true
            }
            2 -> {
                val err = validateStep2()
                if (err != null) {
                    setValidationStatus(err, BootstrapType.DANGER)
                    return false
                }
                currentStep = 3
                syncStepVisibilityAndSummary()
                updateLiveValidationFeedback()
                return true
            }
            else -> return false
        }
    }

    fun goToPreviousStep(): Boolean {
        syncProfileFromInputs()
        if (currentStep > 1) {
            currentStep -= 1
            syncStepVisibilityAndSummary()
            updateLiveValidationFeedback()
            return true
        }
        return false
    }

    fun submitRegistration(): Boolean {
        syncProfileFromInputs()
        val err = validateStep3()
        if (err != null) {
            setValidationStatus(err, BootstrapType.DANGER)
            return false
        }
        isSubmitted = true
        RegistrationProfile.saveTo(preferenceStore, profile)
        setValidationStatus("Registration complete! Profile saved for ${profile.username}.", BootstrapType.SUCCESS)
        return true
    }

    fun resetWizard() {
        currentStep = 1
        isSubmitted = false
        profile = RegistrationProfile()
        usernameInput.value = ""
        emailInput.value = ""
        passwordInput.value = ""
        roleSelect.selectedValue = "Developer"
        experienceSlider.value = 3.0
        experienceYearsBadge.text = "3 years"
        termsCheckBox.isChecked = false
        lastObservedTermsChecked = false
        syncStepVisibilityAndSummary()
        setValidationStatus("Enter your credentials to continue", BootstrapType.INFO)
    }

    private fun syncProfileFromInputs() {
        val years = experienceSlider.value.roundToInt().coerceIn(0, 50)
        val roleVal = roleSelect.selectedValue ?: profile.role
        val termsVal = termsCheckBox.isChecked
        lastObservedTermsChecked = termsVal
        profile = profile.copy(
            username = usernameInput.value.trim(),
            email = emailInput.value.trim(),
            role = roleVal,
            experienceYears = years,
            acceptedTerms = termsVal
        )
        experienceYearsBadge.text = "$years years"
        refreshReviewSummary()
    }

    private fun updateLiveValidationFeedback() {
        syncProfileFromInputs()
        when (currentStep) {
            1 -> {
                val err = validateStep1()
                if (err == null) {
                    setValidationStatus("Step 1 valid — ready to continue", BootstrapType.SUCCESS)
                } else {
                    setValidationStatus(err, BootstrapType.WARNING)
                }
            }
            2 -> {
                val err = validateStep2()
                if (err == null) {
                    setValidationStatus("Step 2 valid — ready to review", BootstrapType.SUCCESS)
                } else {
                    setValidationStatus(err, BootstrapType.WARNING)
                }
            }
            3 -> {
                if (termsCheckBox.isChecked) {
                    setValidationStatus("Ready to submit registration", BootstrapType.SUCCESS)
                } else {
                    setValidationStatus("Accept the Terms of Service to submit", BootstrapType.WARNING)
                }
            }
        }
    }

    private fun setValidationStatus(message: String, type: BootstrapType) {
        validationMessageBadge.text = message
        validationMessageBadge.type = type
        validationAlert.messageText.text = message
        validationAlert.type = type
    }

    private fun syncStepVisibilityAndSummary() {
        step1Panel.isVisible = (currentStep == 1)
        step2Panel.isVisible = (currentStep == 2)
        step3Panel.isVisible = (currentStep == 3)

        wizardProgress.progress = currentStep.toFloat() / 3.0f
        wizardProgress.label = "Step $currentStep of 3"
        currentStepBadge.text = when (currentStep) {
            1 -> "Step 1 of 3: Account Credentials"
            2 -> "Step 2 of 3: Developer Profile"
            else -> "Step 3 of 3: Review & Terms"
        }

        refreshReviewSummary()
    }

    private fun refreshReviewSummary() {
        summaryUsernameText.text = "Username: ${profile.username.ifEmpty { "-" }}"
        summaryEmailText.text = "Email: ${profile.email.ifEmpty { "-" }}"
        summaryRoleText.text = "Role: ${profile.role}"
        summaryExperienceText.text = "Experience: ${profile.experienceYears} years"
    }
}
