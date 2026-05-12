package com.example.integralcalculator.ui

import com.example.integralcalculator.R
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chaquo.python.Python
import androidx.lifecycle.Lifecycle
import com.chaquo.python.android.AndroidPlatform
import com.example.integralcalculator.data.datasource.PythonSolverDataSource
import com.example.integralcalculator.data.repository.IntegralRepositoryImpl
import com.example.integralcalculator.domain.usecase.CalculateIntegralUseCase
import com.example.integralcalculator.presentation.state.CalculatorState
import com.example.integralcalculator.presentation.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var btnToggleABC: Button
    private lateinit var btnToggleFunc: Button
    private lateinit var btnToggle123: Button
    private lateinit var webviewPreview: WebView
    private lateinit var webviewResult: WebView
    private lateinit var btnDiffVar: Button
    private lateinit var rgMode: RadioGroup
    private lateinit var llLimits: LinearLayout
    private lateinit var etUpperLimit: EditText
    private lateinit var etLowerLimit: EditText
    private lateinit var screenInput: LinearLayout
    private lateinit var screenResult: LinearLayout
    private lateinit var layoutBasic: GridLayout
    private lateinit var layoutLetters: GridLayout
    private lateinit var layoutFunctions: GridLayout
    private lateinit var btnNewCalc: Button
    private lateinit var viewModel: CalculatorViewModel
    private var currentTab = 0
    private var isSelectingVarMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Python.isStarted()) Python.start(AndroidPlatform(this))

        bindViews()
        initViewModel()
        setupWebViews()
        initUI()
        observeState()
    }

    private fun bindViews() {
        webviewPreview = findViewById(R.id.webviewPreview)
        webviewResult = findViewById(R.id.webviewResult)
        btnDiffVar = findViewById(R.id.btnDiffVar)
        rgMode = findViewById(R.id.rgMode)
        llLimits = findViewById(R.id.llLimits)
        etUpperLimit = findViewById(R.id.etUpperLimit)
        etLowerLimit = findViewById(R.id.etLowerLimit)
        screenInput = findViewById(R.id.screenInput)
        screenResult = findViewById(R.id.screenResult)
        layoutBasic = findViewById(R.id.layoutBasic)
        layoutLetters = findViewById(R.id.layoutLetters)
        layoutFunctions = findViewById(R.id.layoutFunctions)
        btnNewCalc = findViewById(R.id.btnNewCalc)
        btnToggleABC = findViewById(R.id.btnToggleABC)
        btnToggleFunc = findViewById(R.id.btnToggleFunc)
        btnToggle123 = findViewById(R.id.btnToggle123)
    }
    private fun updateTabButtons(activeTab: Int) {
        val colorActive = Color.parseColor("#00BFA5")
        val colorInactive = Color.parseColor("#333333")
        val textActive = Color.parseColor("#FFFFFF")
        val textInactive = Color.parseColor("#B388FF")

        btnToggleABC.setBackgroundColor(if (activeTab == 1) colorActive else colorInactive)
        btnToggleABC.setTextColor(if (activeTab == 1) textActive else textInactive)

        btnToggleFunc.setBackgroundColor(if (activeTab == 2) colorActive else colorInactive)
        btnToggleFunc.setTextColor(if (activeTab == 2) textActive else textInactive)

        btnToggle123.setBackgroundColor(if (activeTab == 0) colorActive else colorInactive)
        btnToggle123.setTextColor(if (activeTab == 0) textActive else textInactive)
    }
    private fun initViewModel() {
        val dataSource = PythonSolverDataSource()
        val repository = IntegralRepositoryImpl(dataSource)
        val useCase = CalculateIntegralUseCase(repository)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CalculatorViewModel(useCase) as T
        })[CalculatorViewModel::class.java]
    }

    private fun initUI() {
        setupTabs()
        setupModeSwitch()
        setupDiffVarButton()
        setupButtons()
        setupLimitsSync()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updatePreview(state)
                    state.result?.let { res ->
                        if (res.success || res.error != null) {
                            screenInput.visibility = View.GONE
                            screenResult.visibility = View.VISIBLE
                            val finalLatex = if (res.success && !state.isDefinite) "${res.latex} + C" else res.latex
                            renderLatex(webviewResult, finalLatex)
                        }
                    }
                    if (!isSelectingVarMode) {
                        btnDiffVar.setBackgroundColor(Color.parseColor("#00BFA5"))
                    }
                }
            }
        }
    }

    private fun updatePreview(state: CalculatorState) {
        if (state.rawInput.isEmpty()) {
            renderLatex(webviewPreview, "")
            return
        }

        val modeSymbol = if (state.isDefinite) {
            val up = state.upperLimit.ifEmpty { "b" }
            val low = state.lowerLimit.ifEmpty { "a" }
            "\\int_{${low}}^{${up}}"
        } else {
            "\\int"
        }
        val fullLatex = "${modeSymbol} \\left( ${state.latexPreview} \\right) \\, d${state.integrationVar}"

        renderLatex(webviewPreview, fullLatex)
    }

    private fun setupWebViews() {
        listOf(webviewPreview, webviewResult).forEach { webView ->
            webView.settings.javaScriptEnabled = true
            webView.setBackgroundColor(Color.TRANSPARENT)

            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
                    <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
                    <style>
                        body { 
                            margin: 0; 
                            padding: 10px; 
                            color: #fff; 
                            font-size: 18px; 
                            text-align: center; 
                            background-color: transparent;
                        }
                        /* Заставляем формулы быть крупными и по центру */
                        .MathJax_Display {
                            margin: 0 !important;
                            overflow-x: auto;
                            overflow-y: hidden;
                        }
                    </style>
                </head>
                <body>
                    <div id="math-container"></div>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        }
    }

    private fun renderLatex(webView: WebView, latex: String) {
        if (latex.isEmpty()) {
            webView.evaluateJavascript("document.getElementById('math-container').innerHTML = '';", null)
            return
        }
        val escapedLatex = latex.replace("\\", "\\\\").replace("'", "\\'")

        val jsCode = """
            var container = document.getElementById('math-container');
            container.innerHTML = '$$${escapedLatex}$$';
            MathJax.typesetPromise([container]).catch(function(err) { console.log(err); });
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    private fun setupTabs() {
        currentTab = 0
        layoutBasic.visibility = View.VISIBLE
        layoutLetters.visibility = View.GONE
        layoutFunctions.visibility = View.GONE
        updateTabButtons(0)

        btnToggleABC.setOnClickListener {
            currentTab = 1
            layoutBasic.visibility = View.GONE
            layoutLetters.visibility = View.VISIBLE
            layoutFunctions.visibility = View.GONE
            updateTabButtons(1)
        }

        btnToggleFunc.setOnClickListener {
            currentTab = 2
            layoutBasic.visibility = View.GONE
            layoutLetters.visibility = View.GONE
            layoutFunctions.visibility = View.VISIBLE
            updateTabButtons(2)
        }

        btnToggle123.setOnClickListener {
            currentTab = 0
            layoutBasic.visibility = View.VISIBLE
            layoutLetters.visibility = View.GONE
            layoutFunctions.visibility = View.GONE
            updateTabButtons(0)
        }
    }

    private fun setupModeSwitch() {
        rgMode.setOnCheckedChangeListener { _, checkedId ->
            val isDefinite = checkedId == R.id.rbDefinite
            llLimits.visibility = if (isDefinite) View.VISIBLE else View.GONE
            viewModel.setMode(isDefinite)
        }
    }

    private fun setupLimitsSync() {
        val limitsWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setLimits(etLowerLimit.text.toString(), etUpperLimit.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etLowerLimit.addTextChangedListener(limitsWatcher)
        etUpperLimit.addTextChangedListener(limitsWatcher)
    }

    private fun setupDiffVarButton() {
        btnDiffVar.setOnClickListener {
            isSelectingVarMode = true
            btnDiffVar.setBackgroundColor(Color.parseColor("#4CAF50"))
            currentTab = 1
            layoutBasic.visibility = View.GONE
            layoutLetters.visibility = View.VISIBLE
            layoutFunctions.visibility = View.GONE
            updateTabButtons(1)
            Toast.makeText(this, "Выберите переменную (a-z или греческую)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        val basicButtons: List<Pair<Int, String>> = listOf(
            Pair(R.id.btn0, "0"), Pair(R.id.btn1, "1"), Pair(R.id.btn2, "2"), Pair(R.id.btn3, "3"),
            Pair(R.id.btn4, "4"), Pair(R.id.btn5, "5"), Pair(R.id.btn6, "6"), Pair(R.id.btn7, "7"),
            Pair(R.id.btn8, "8"), Pair(R.id.btn9, "9"), Pair(R.id.btnDot, "."),
            Pair(R.id.btnAdd, " + "), Pair(R.id.btnSub, " - "), Pair(R.id.btnMul, " * "),
            Pair(R.id.btnDiv, " / "), Pair(R.id.btnOpen, "("), Pair(R.id.btnClose, ")"),
            Pair(R.id.btnPow, "^"), Pair(R.id.btnSqrt, "sqrt("), Pair(R.id.btnAbs, "abs("),
            Pair(R.id.btnPi, "pi"), Pair(R.id.btnE, "e"), Pair(R.id.btnFrac, "()/()")
        )

        basicButtons.forEach { pair ->
            val id = pair.first
            val text = pair.second

            findViewById<Button>(id).setOnClickListener {
                if (text == "()/()") {
                    viewModel.appendInput("()/()", "\\frac{}{}")
                } else {
                    val latexMap = mapOf(
                        "*" to "\\cdot ",
                        "pi" to "\\pi",
                        "sqrt(" to "\\sqrt{",
                        "abs(" to "\\left|",
                        "^" to "^{"
                    )
                    val latex = latexMap[text] ?: text
                    viewModel.appendInput(text, latex)
                }
            }
        }

        findViewById<Button>(R.id.btnVarX).setOnClickListener {
            if (isSelectingVarMode) selectVariable("x") else viewModel.appendInput("x", "x")
        }

        val latinLetters: List<Pair<Int, String>> = listOf(
            R.id.btnVarA to "a", R.id.btnVarB to "b", R.id.btnVarC to "c",
            R.id.btnVarD to "d", R.id.btnVarE to "e", R.id.btnVarF to "f",
            R.id.btnVarG to "g", R.id.btnVarH to "h", R.id.btnVarI to "i",
            R.id.btnVarJ to "j", R.id.btnVarK to "k", R.id.btnVarL to "l",
            R.id.btnVarM to "m", R.id.btnVarN to "n", R.id.btnVarO to "o",
            R.id.btnVarP to "p", R.id.btnVarQ to "q", R.id.btnVarR to "r",
            R.id.btnVarS to "s", R.id.btnVarT to "t", R.id.btnVarU to "u",
            R.id.btnVarV to "v", R.id.btnVarW to "w", R.id.btnVarX2 to "x",
            R.id.btnVarY to "y", R.id.btnVarZ to "z"
        )

        latinLetters.forEach { pair ->
            val id = pair.first
            val char = pair.second
            findViewById<Button>(id).setOnClickListener {
                if (isSelectingVarMode) selectVariable(char) else viewModel.appendInput(char, char)
            }
        }

        val greekLetters: Map<Int, Pair<String, String>> = mapOf(
            R.id.btnGreekAlpha to Pair("α", "\\alpha"),
            R.id.btnGreekBeta to Pair("β", "\\beta")
        )

        greekLetters.forEach { (id, pair) ->
            val text = pair.first
            val latex = pair.second
            findViewById<Button>(id).setOnClickListener {
                if (isSelectingVarMode) selectVariable(text) else viewModel.appendInput(text, latex)
            }
        }

        findViewById<Button>(R.id.btnBackAdv).setOnClickListener { viewModel.backspace() }
        findViewById<Button>(R.id.btnCAdv).setOnClickListener { viewModel.clear() }

        val functions: Map<Int, Pair<String, String>> = mapOf(
            R.id.btnSin to Pair("sin(", "\\sin\\left("),
            R.id.btnCos to Pair("cos(", "\\cos\\left("),
            R.id.btnTan to Pair("tan(", "\\tan\\left("),
            R.id.btnCot to Pair("cot(", "\\cot\\left("),
            R.id.btnAsin to Pair("asin(", "\\arcsin\\left("),
            R.id.btnAcos to Pair("acos(", "\\arccos\\left("),
            R.id.btnAtan to Pair("atan(", "\\arctan\\left("),
            R.id.btnAcot to Pair("acot(", "\\arccot\\left("),
            R.id.btnLog to Pair("log(", "\\log\\left("),
            R.id.btnLn to Pair("ln(", "\\ln\\left("),
            R.id.btnExp to Pair("exp(", "e^{"),
            R.id.btnSqrtAdv to Pair("sqrt(", "\\sqrt{"),
            R.id.btnFracAdv to Pair("()/()", "\\frac{}{}"),
            R.id.btnAbsAdv to Pair("abs(", "\\left|"),
            R.id.btnPowAdv to Pair("^", "^{"),
            R.id.btnPiAdv to Pair("pi", "\\pi"),
            R.id.btnEAdv to Pair("e", "e"),
            R.id.btnOpenAdv to Pair("(", "("),
            R.id.btnCloseAdv to Pair(")", ")"),
            R.id.btnDotAdv to Pair(".", ".")
        )

        functions.forEach { (id, pair) ->
            val text = pair.first
            val latex = pair.second
            findViewById<Button>(id).setOnClickListener {
                if (text == "()/()") viewModel.appendInput("()/()", "\\frac{}{}")
                else viewModel.appendInput(text, latex)
            }
        }

        findViewById<Button>(R.id.btnBackAdv2).setOnClickListener { viewModel.backspace() }
        findViewById<Button>(R.id.btnCAdv2).setOnClickListener { viewModel.clear() }
        findViewById<Button>(R.id.btnCalcAdv).setOnClickListener { viewModel.calculate() }

        findViewById<Button>(R.id.btnC).setOnClickListener { viewModel.clear() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { viewModel.backspace() }
        findViewById<Button>(R.id.btnCalc).setOnClickListener { viewModel.calculate() }

        btnNewCalc.setOnClickListener {
            viewModel.clear()
            screenResult.visibility = View.GONE
            screenInput.visibility = View.VISIBLE
        }
    }
    private fun selectVariable(variable: String) {
        viewModel.setVariable(variable)
        btnDiffVar.text = "d$variable"
        isSelectingVarMode = false
        btnDiffVar.setBackgroundColor(Color.parseColor("#00BFA5"))
        currentTab = 0
        layoutBasic.visibility = View.VISIBLE
        layoutLetters.visibility = View.GONE
        layoutFunctions.visibility = View.GONE
        updateTabButtons(0)
        Toast.makeText(this, "Переменная: $variable", Toast.LENGTH_SHORT).show()
    }
}