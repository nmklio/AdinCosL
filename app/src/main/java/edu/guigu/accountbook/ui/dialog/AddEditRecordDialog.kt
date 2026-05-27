package edu.guigu.accountbook.ui.dialog

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import edu.guigu.accountbook.data.model.Record
import edu.guigu.accountbook.databinding.DialogAddEditRecordBinding
import edu.guigu.accountbook.util.DateUtils
import java.util.Calendar

class AddEditRecordDialog(
    private val editRecord: Record? = null,
    private val onSave: (Record) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogAddEditRecordBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: Long = System.currentTimeMillis()
    private var selectedType: Int = Record.TYPE_EXPENSE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupMode()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMode() {
        if (editRecord != null) {
            binding.tvDialogTitle.text = "编辑记录"
            selectedType = editRecord.type
            binding.spinnerType.setSelection(selectedType)
            updateCategorySpinner(selectedType)

            val index = Record.getCategoriesByType(selectedType).indexOf(editRecord.category)
            if (index >= 0) {
                binding.spinnerCategory.setSelection(index)
            }

            binding.etAmount.setText(DateUtils.formatAmount(editRecord.amount))
            if (!editRecord.note.isNullOrBlank()) {
                binding.etNote.setText(editRecord.note)
            }

            selectedDate = editRecord.date
            binding.tvSelectedDate.text = DateUtils.formatDate(selectedDate)
        } else {
            binding.tvDialogTitle.text = "添加记录"
            selectedType = Record.TYPE_EXPENSE
            binding.spinnerType.setSelection(selectedType)
            updateCategorySpinner(selectedType)
            selectedDate = System.currentTimeMillis()
            binding.tvSelectedDate.text = DateUtils.formatDate(selectedDate)
        }
    }

    private fun setupListeners() {
        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedType = position
                updateCategorySpinner(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.btnPickDate.setOnClickListener { showDatePicker() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveRecord() }
    }

    private fun updateCategorySpinner(type: Int) {
        val categories = Record.getCategoriesByType(type)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(year, month, day, 12, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                selectedDate = cal.timeInMillis
                binding.tvSelectedDate.text = DateUtils.formatDate(selectedDate)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveRecord() {
        binding.etAmount.error = null

        val amountStr = binding.etAmount.text?.toString()?.trim()
        if (amountStr.isNullOrBlank()) {
            binding.etAmount.error = "请输入金额"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "请输入有效的金额"
            return
        }

        val category = binding.spinnerCategory.selectedItem?.toString() ?: "其他"
        val note = binding.etNote.text?.toString()?.trim()?.takeIf { it.isNotBlank() }

        val record = Record(
            id = editRecord?.id ?: 0,
            type = selectedType,
            category = category,
            amount = amount,
            note = note,
            date = selectedDate
        )

        onSave(record)
        dismiss()
    }
}
