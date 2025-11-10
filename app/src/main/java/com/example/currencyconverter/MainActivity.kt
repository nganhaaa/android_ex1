package com.example.currencyconverter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Khai báo các thành phần UI
    private lateinit var etFromAmount: EditText
    private lateinit var spinnerFromCurrency: Spinner
    private lateinit var etToAmount: EditText
    private lateinit var spinnerToCurrency: Spinner

    // Tỷ giá cố định so với 1 USD
    private val exchangeRates = mapOf(
        "USD" to 1.0,
        "VND" to 25450.0,
        "EUR" to 0.92,
        "JPY" to 157.0,
        "GBP" to 0.79,
        "AUD" to 1.5,
        "CAD" to 1.37,
        "CHF" to 0.9,
        "CNY" to 7.25,
        "KRW" to 1380.0
    )

    private val currencies = exchangeRates.keys.toList()

    // Cờ để tránh vòng lặp vô hạn khi cập nhật EditText
    private var isUpdatingFrom = false
    private var isUpdatingTo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ánh xạ View
        etFromAmount = findViewById(R.id.etFromAmount)
        spinnerFromCurrency = findViewById(R.id.spinnerFromCurrency)
        etToAmount = findViewById(R.id.etToAmount)
        spinnerToCurrency = findViewById(R.id.spinnerToCurrency)

        setupSpinners()
        setupListeners()
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerFromCurrency.adapter = adapter
        spinnerToCurrency.adapter = adapter

        // Set giá trị mặc định
        spinnerFromCurrency.setSelection(currencies.indexOf("USD"))
        spinnerToCurrency.setSelection(currencies.indexOf("VND"))
    }

    private fun setupListeners() {
        // Lắng nghe sự kiện thay đổi lựa chọn trên Spinner
        val selectionListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Khi thay đổi loại tiền tệ, tính toán lại
                convert(etFromAmount, etToAmount, spinnerFromCurrency, spinnerToCurrency)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerFromCurrency.onItemSelectedListener = selectionListener
        spinnerToCurrency.onItemSelectedListener = selectionListener

        // Lắng nghe sự kiện nhập liệu trên EditText
        etFromAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdatingTo) { // Nếu không phải là đang được cập nhật bởi etToAmount
                    isUpdatingFrom = true // Đặt cờ báo hiệu etFromAmount đang cập nhật
                    convert(etFromAmount, etToAmount, spinnerFromCurrency, spinnerToCurrency)
                    isUpdatingFrom = false
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etToAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdatingFrom) { // Nếu không phải là đang được cập nhật bởi etFromAmount
                    isUpdatingTo = true // Đặt cờ báo hiệu etToAmount đang cập nhật
                    convert(etToAmount, etFromAmount, spinnerToCurrency, spinnerFromCurrency)
                    isUpdatingTo = false
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun convert(sourceEt: EditText, targetEt: EditText, sourceSpinner: Spinner, targetSpinner: Spinner) {
        val amountString = sourceEt.text.toString()
        if (amountString.isEmpty()) {
            targetEt.setText("")
            return
        }

        val amount = amountString.toDoubleOrNull()
        if (amount == null) {
            targetEt.setText("Invalid number")
            return
        }

        val fromCurrency = sourceSpinner.selectedItem.toString()
        val toCurrency = targetSpinner.selectedItem.toString()

        val fromRate = exchangeRates[fromCurrency]!!
        val toRate = exchangeRates[toCurrency]!!

        // Công thức chuyển đổi qua đồng tiền trung gian (USD)
        val result = amount * (toRate / fromRate)

        // Định dạng kết quả cho dễ nhìn
        targetEt.setText(String.format("%.2f", result))
    }
}
