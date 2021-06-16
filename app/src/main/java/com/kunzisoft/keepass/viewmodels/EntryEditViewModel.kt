package com.kunzisoft.keepass.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kunzisoft.keepass.app.database.IOActionTask
import com.kunzisoft.keepass.database.element.*
import com.kunzisoft.keepass.database.element.icon.IconImage
import com.kunzisoft.keepass.database.element.icon.IconImageStandard
import com.kunzisoft.keepass.database.element.node.NodeId
import com.kunzisoft.keepass.database.element.template.Template
import com.kunzisoft.keepass.model.*
import com.kunzisoft.keepass.otp.OtpElement
import com.kunzisoft.keepass.view.DataDate
import com.kunzisoft.keepass.view.DataTime
import java.util.*


class EntryEditViewModel: ViewModel() {

    private val mDatabase: Database? = Database.getInstance()

    private var mParent : Group? = null
    private var mEntry : Entry? = null
    private var mIsTemplate: Boolean = false

    private val mTempAttachments = mutableListOf<EntryAttachmentState>()

    val entryInfoLoaded : LiveData<EntryInfo> get() = _entryInfoLoaded
    private val _entryInfoLoaded = SingleLiveEvent<EntryInfo>()

    val requestEntryInfoUpdate : LiveData<Void?> get() = _requestEntryInfoUpdate
    private val _requestEntryInfoUpdate = SingleLiveEvent<Void?>()
    val onEntrySaved : LiveData<EntrySave> get() = _onEntrySaved
    private val _onEntrySaved = SingleLiveEvent<EntrySave>()

    val templates : LiveData<TemplatesLoad> get() = _templates
    private val _templates = MutableLiveData<TemplatesLoad>()
    val onTemplateChanged : LiveData<Template> get() = _onTemplateChanged
    private val _onTemplateChanged = SingleLiveEvent<Template>()

    val requestIconSelection : LiveData<IconImage> get() = _requestIconSelection
    private val _requestIconSelection = SingleLiveEvent<IconImage>()
    val onIconSelected : LiveData<IconImage> get() = _onIconSelected
    private val _onIconSelected = SingleLiveEvent<IconImage>()

    val requestPasswordSelection : LiveData<Field> get() = _requestPasswordSelection
    private val _requestPasswordSelection = SingleLiveEvent<Field>()
    val onPasswordSelected : LiveData<Field> get() = _onPasswordSelected
    private val _onPasswordSelected = SingleLiveEvent<Field>()

    val requestCustomFieldEdition : LiveData<Field> get() = _requestCustomFieldEdition
    private val _requestCustomFieldEdition = SingleLiveEvent<Field>()
    val onCustomFieldEdited : LiveData<FieldEdition> get() = _onCustomFieldEdited
    private val _onCustomFieldEdited = SingleLiveEvent<FieldEdition>()
    val onCustomFieldError : LiveData<Void?> get() = _onCustomFieldError
    private val _onCustomFieldError = SingleLiveEvent<Void?>()

    val requestDateTimeSelection : LiveData<DateInstant> get() = _requestDateTimeSelection
    private val _requestDateTimeSelection = SingleLiveEvent<DateInstant>()
    val onDateSelected : LiveData<DataDate> get() = _onDateSelected
    private val _onDateSelected = SingleLiveEvent<DataDate>()
    val onTimeSelected : LiveData<DataTime> get() = _onTimeSelected
    private val _onTimeSelected = SingleLiveEvent<DataTime>()

    val requestSetupOtp : LiveData<Void?> get() = _requestSetupOtp
    private val _requestSetupOtp = SingleLiveEvent<Void?>()
    val onOtpCreated : LiveData<OtpElement> get() = _onOtpCreated
    private val _onOtpCreated = SingleLiveEvent<OtpElement>()

    val onBuildNewAttachment : LiveData<AttachmentBuild> get() = _onBuildNewAttachment
    private val _onBuildNewAttachment = SingleLiveEvent<AttachmentBuild>()
    val onStartUploadAttachment : LiveData<AttachmentUpload> get() = _onStartUploadAttachment
    private val _onStartUploadAttachment = SingleLiveEvent<AttachmentUpload>()
    val attachmentDeleted : LiveData<Attachment> get() = _attachmentDeleted
    private val _attachmentDeleted = SingleLiveEvent<Attachment>()
    val onAttachmentAction : LiveData<EntryAttachmentState?> get() = _onAttachmentAction
    private val _onAttachmentAction = MutableLiveData<EntryAttachmentState?>()
    val onBinaryPreviewLoaded : LiveData<AttachmentPosition> get() = _onBinaryPreviewLoaded
    private val _onBinaryPreviewLoaded = SingleLiveEvent<AttachmentPosition>()

    fun initializeEntryToUpdate(entryId: NodeId<UUID>,
                                registerInfo: RegisterInfo?,
                                searchInfo: SearchInfo?) {
        // Create an Entry copy to modify from the database entry
        mEntry = mDatabase?.getEntryById(entryId)
        // Retrieve the parent
        mEntry?.let { entry ->
            // If no parent, add root group as parent
            if (entry.parent == null) {
                entry.parent = mDatabase?.rootGroup
            }
        }

        loadTemplates()
        loadEntryInfo(registerInfo, searchInfo)
    }

    fun initializeEntryToCreate(parentId: NodeId<*>,
                                registerInfo: RegisterInfo?,
                                searchInfo: SearchInfo?) {
        mParent = mDatabase?.getGroupById(parentId)
        mEntry = mDatabase?.createEntry()?.apply {
            // Add the default icon from parent if not a folder
            val parentIcon = mParent?.icon
            // Set default icon
            if (parentIcon != null) {
                if (parentIcon.custom.isUnknown
                    && parentIcon.standard.id != IconImageStandard.FOLDER_ID) {
                    icon = IconImage(parentIcon.standard)
                }
                if (!parentIcon.custom.isUnknown) {
                    icon = IconImage(parentIcon.custom)
                }
            }
            // Set default username
            username = mDatabase?.defaultUsername ?: ""
            // Warning only the entry recognize is parent, parent don't yet recognize the new entry
            // Useful to recognize child state (ie: entry is a template)
            parent = mParent
        }

        loadTemplates()
        loadEntryInfo(registerInfo, searchInfo)
    }

    private fun loadTemplates() {
        // Define is current entry is a template (in direct template group)
        mIsTemplate = mDatabase?.entryIsTemplate(mEntry) ?: false

        val templates = mDatabase?.getTemplates(mIsTemplate) ?: listOf()

        val entryTemplate = mEntry?.let {
            mDatabase?.getTemplate(it)
        } ?: Template.STANDARD

        _templates.value = TemplatesLoad(templates, entryTemplate)
        changeTemplate(entryTemplate)
    }

    fun changeTemplate(template: Template) {
        if (_onTemplateChanged.value != template) {
            _onTemplateChanged.value = template
        }
    }

    // TODO Move
    fun entryIsTemplate(): Boolean {
        return mIsTemplate
    }

    fun requestEntryInfoUpdate() {
        _requestEntryInfoUpdate.call()
    }

    private fun loadEntryInfo(registerInfo: RegisterInfo?, searchInfo: SearchInfo?) {
        // Decode the entry
        mEntry?.let {
            mDatabase?.decodeEntryWithTemplateConfiguration(it)?.let { entry ->
                // Load entry info
                entry.getEntryInfo(mDatabase, true).let { tempEntryInfo ->
                    // Retrieve data from registration
                    (registerInfo?.searchInfo ?: searchInfo)?.let { tempSearchInfo ->
                        tempEntryInfo.saveSearchInfo(mDatabase, tempSearchInfo)
                    }
                    registerInfo?.let { regInfo ->
                        tempEntryInfo.saveRegisterInfo(mDatabase, regInfo)
                    }

                    internalUpdateEntryInfo(tempEntryInfo) { entryInfoUpdated ->
                        _entryInfoLoaded.value = entryInfoUpdated
                    }
                }
            }
        }
    }

    fun saveEntryInfo(entryInfo: EntryInfo) {
        internalUpdateEntryInfo(entryInfo) { entryInfoUpdated ->
            mEntry?.let { oldEntry ->
                // Create a clone
                var newEntry = Entry(oldEntry)

                // Build info
                newEntry.setEntryInfo(mDatabase, entryInfoUpdated)

                // Encode entry properties for template
                _onTemplateChanged.value?.let { template ->
                    newEntry = mDatabase?.encodeEntryWithTemplateConfiguration(newEntry, template)
                        ?: newEntry
                }

                // Delete temp attachment if not used
                mTempAttachments.forEach { tempAttachmentState ->
                    val tempAttachment = tempAttachmentState.attachment
                    mDatabase?.attachmentPool?.let { binaryPool ->
                        if (!newEntry.getAttachments(binaryPool).contains(tempAttachment)) {
                            mDatabase.removeAttachmentIfNotUsed(tempAttachment)
                        }
                    }
                }

                _onEntrySaved.value = EntrySave(oldEntry, newEntry, mParent)
            }
        }
    }

    private fun internalUpdateEntryInfo(entryInfo: EntryInfo,
                                        actionOnFinish: ((entryInfo: EntryInfo) -> Unit)? = null) {
        IOActionTask(
            {
                // Do not save entry in upload progression
                mTempAttachments.forEach { attachmentState ->
                    if (attachmentState.streamDirection == StreamDirection.UPLOAD) {
                        when (attachmentState.downloadState) {
                            AttachmentState.START,
                            AttachmentState.IN_PROGRESS,
                            AttachmentState.CANCELED,
                            AttachmentState.ERROR -> {
                                // Remove attachment not finished from info
                                entryInfo.attachments = entryInfo.attachments.toMutableList().apply {
                                    remove(attachmentState.attachment)
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }
                entryInfo
            },
            {
                if (it != null)
                    actionOnFinish?.invoke(it)
            }
        ).execute()
    }

    fun requestIconSelection(oldIconImage: IconImage) {
        _requestIconSelection.value = oldIconImage
    }

    fun selectIcon(iconImage: IconImage) {
        _onIconSelected.value = iconImage
    }

    fun requestPasswordSelection(passwordField: Field) {
        _requestPasswordSelection.value = passwordField
    }

    fun selectPassword(passwordField: Field) {
        _onPasswordSelected.value = passwordField
    }

    fun requestCustomFieldEdition(customField: Field) {
        _requestCustomFieldEdition.value = customField
    }

    fun addCustomField(newField: Field) {
        _onCustomFieldEdited.value = FieldEdition(null, newField)
    }

    fun editCustomField(oldField: Field, newField: Field) {
        _onCustomFieldEdited.value = FieldEdition(oldField, newField)
    }

    fun removeCustomField(oldField: Field) {
        _onCustomFieldEdited.value = FieldEdition(oldField, null)
    }

    fun showCustomFieldEditionError() {
        _onCustomFieldError.call()
    }

    fun requestDateTimeSelection(dateInstant: DateInstant) {
        _requestDateTimeSelection.value = dateInstant
    }

    fun selectDate(year: Int, month: Int, day: Int) {
        _onDateSelected.value = DataDate(year, month, day)
    }

    fun selectTime(hours: Int, minutes: Int) {
        _onTimeSelected.value = DataTime(hours, minutes)
    }

    fun setupOtp() {
        _requestSetupOtp.call()
    }

    fun createOtp(otpElement: OtpElement) {
        _onOtpCreated.value = otpElement
    }

    fun buildNewAttachment(attachmentToUploadUri: Uri, fileName: String) {
        _onBuildNewAttachment.value = AttachmentBuild(attachmentToUploadUri, fileName)
    }

    fun startUploadAttachment(attachmentToUploadUri: Uri, attachment: Attachment) {
        _onStartUploadAttachment.value = AttachmentUpload(attachmentToUploadUri, attachment)
    }

    fun deleteAttachment(attachment: Attachment) {
        _attachmentDeleted.value = attachment
    }

    fun onAttachmentAction(entryAttachmentState: EntryAttachmentState?) {
        if (entryAttachmentState?.downloadState == AttachmentState.START) {
            // Add in temp list
            mTempAttachments.add(entryAttachmentState)
        }
        _onAttachmentAction.value = entryAttachmentState
    }

    fun binaryPreviewLoaded(entryAttachmentState: EntryAttachmentState, viewPosition: Float) {
        _onBinaryPreviewLoaded.value = AttachmentPosition(entryAttachmentState, viewPosition)
    }

    data class TemplatesLoad(val templates: List<Template>, val defaultTemplate: Template)
    data class EntrySave(val oldEntry: Entry, val newEntry: Entry, val parent: Group?)
    data class FieldEdition(val oldField: Field?, val newField: Field?)
    data class AttachmentBuild(val attachmentToUploadUri: Uri, val fileName: String)
    data class AttachmentUpload(val attachmentToUploadUri: Uri, val attachment: Attachment)
    data class AttachmentPosition(val entryAttachmentState: EntryAttachmentState, val viewPosition: Float)

    companion object {
        private val TAG = EntryEditViewModel::class.java.name
    }
}