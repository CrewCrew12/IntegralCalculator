package com.example.integralcalculator.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.integralcalculator.R
import com.example.integralcalculator.presentation.state.CalculatorState
import com.example.integralcalculator.presentation.viewmodel.AuthViewModel
import com.example.integralcalculator.presentation.viewmodel.CalculatorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.content.Context
import android.view.inputmethod.InputMethodManager

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: CalculatorViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

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
    private lateinit var btnHistory: Button

    private var currentTab = 0
    private var isSelectingVarMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Python.isStarted()) Python.start(AndroidPlatform(this))

        bindViews()
        setupWebViews()
        initUI()
        observeAuthState()
        observeCalculatorState()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        webviewPreview.postDelayed({
            updatePreview(viewModel.state.value)
        }, 1000)
    }

    override fun onResume() {
        super.onResume()
        authViewModel.refreshAuthStatus()
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
        btnToggleFunc = findViewById(R.id.btnToggleFun)
        btnToggle123 = findViewById(R.id.btnToggle123)
        btnHistory = findViewById(R.id.btnHistory)
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { authState ->
                    if (!authState.isLoggedIn) {
                        val intent = Intent(this@MainActivity, AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    } else {
                        btnHistory.isEnabled = true
                        btnHistory.alpha = 1f
                    }
                }
            }
        }
    }

    private fun observeCalculatorState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updatePreview(state)

                    state.result?.let { res ->
                        if (res.success) {
                            screenInput.visibility = View.GONE
                            screenResult.visibility = View.VISIBLE

                            val finalLatex = if (res.latex.isNotEmpty()) {
                                if (!state.isDefinite) "${res.latex} + C" else res.latex
                            } else {
                                "\\text{Нет результата}"
                            }
                            renderLatex(webviewResult, finalLatex)
                        } else if (res.error != null) {
                            screenInput.visibility = View.GONE
                            screenResult.visibility = View.VISIBLE
                            val errorLatex = "\\text{Ошибка: ${res.error}}"
                            renderLatex(webviewResult, errorLatex)
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
        val fullLatex = buildFullLatex(state)
        if (fullLatex.isEmpty()) {
            renderLatex(webviewPreview, "")
            return
        }
        renderLatex(webviewPreview, fullLatex)
    }

    private fun buildFullLatex(state: CalculatorState): String {
        val expression = if (state.latexPreview.isNotEmpty()) {
            state.latexPreview
        } else {
            ""
        }

        val integralSymbol = if (state.isDefinite) {
            val lower = if (state.lowerLimit.isEmpty()) "a" else state.lowerLimit
            val upper = if (state.upperLimit.isEmpty()) "b" else state.upperLimit
            "\\int_{${lower}}^{${upper}}"
        } else {
            "\\int"
        }

        if (expression.isEmpty()) {
            return "$integralSymbol d${state.integrationVar}"
        }

        return "$integralSymbol ( $expression ) d${state.integrationVar}"
    }

    private fun setupWebViews() {
        listOf(webviewPreview, webviewResult).forEach { webView ->
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.loadWithOverviewMode = true
            webView.settings.useWideViewPort = true
            webView.setBackgroundColor(Color.TRANSPARENT)

            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { 
                            margin: 0; 
                            padding: 10px; 
                            color: #ffffff; 
                            font-size: 20px; 
                            text-align: center; 
                            background-color: transparent;
                            font-family: 'Times New Roman', serif;
                        }
                    </style>
                    <script>
                        window.MathJax = {
                            tex: {
                                inlineMath: [['$', '$'], ['\\(', '\\)']],
                                displayMath: [['$$', '$$'], ['\\[', '\\]']]
                            }
                        };
                    </script>
                    <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js"></script>
                    <script>
                        function renderLatex(latex) {
                            var container = document.getElementById('math-container');
                            if (!container) return;
                            if (!latex || latex === '') {
                                container.innerHTML = '';
                                return;
                            }
                            container.innerHTML = '\\[' + latex + '\\]';
                            if (window.MathJax) {
                                MathJax.typesetPromise([container]).catch(function(err) {
                                    container.innerHTML = '\\text{Ошибка: } ' + latex;
                                });
                            }
                        }
                    </script>
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

        val safeLatex = latex
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", " ")
            .replace("\r", " ")

        val jsCode = """
            (function() {
                var container = document.getElementById('math-container');
                if (!container) return;
                if (window.renderLatex) {
                    window.renderLatex('${safeLatex}');
                } else {
                    setTimeout(function() {
                        if (window.renderLatex) window.renderLatex('${safeLatex}');
                    }, 100);
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    private fun initUI() {
        setupTabs()
        setupModeSwitch()
        setupDiffVarButton()
        setupButtons()
        setupLimitsSync()
    }

    private fun setupTabs() {
        currentTab = 0
        layoutBasic.visibility = View.VISIBLE
        layoutLetters.visibility = View.GONE
        layoutFunctions.visibility = View.GONE
        updateTabButtons(0)
        btnToggleABC.setOnClickListener { switchTab(1) }
        btnToggleFunc.setOnClickListener { switchTab(2) }
        btnToggle123.setOnClickListener { switchTab(0) }
    }

    private fun switchTab(tab: Int) {
        currentTab = tab
        layoutBasic.visibility = if (tab == 0) View.VISIBLE else View.GONE
        layoutLetters.visibility = if (tab == 1) View.VISIBLE else View.GONE
        layoutFunctions.visibility = if (tab == 2) View.VISIBLE else View.GONE
        updateTabButtons(tab)
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

    private fun setupModeSwitch() {
        rgMode.setOnCheckedChangeListener { _, checkedId ->
            val isDefinite = checkedId == R.id.rbDefinite
            llLimits.visibility = if (isDefinite) View.VISIBLE else View.GONE
            viewModel.setMode(isDefinite)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            currentFocus?.let { view ->
                imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    private fun setupLimitsSync() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setLimits(etLowerLimit.text.toString(), etUpperLimit.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etLowerLimit.addTextChangedListener(watcher)
        etUpperLimit.addTextChangedListener(watcher)
    }

    private fun setupDiffVarButton() {
        btnDiffVar.setOnClickListener {
            isSelectingVarMode = true
            btnDiffVar.setBackgroundColor(Color.parseColor("#4CAF50"))
            switchTab(1)
            Toast.makeText(this, "Выберите переменную (a-z)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        val basicButtons = listOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
            R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
            R.id.btn8 to "8", R.id.btn9 to "9", R.id.btnDot to ".",
            R.id.btnAdd to " + ", R.id.btnSub to " - ", R.id.btnMul to " * ",
            R.id.btnDiv to " / ", R.id.btnOpen to "(", R.id.btnClose to ")",
            R.id.btnPow to "^", R.id.btnSqrt to "sqrt(", R.id.btnAbs to "abs(",
            R.id.btnPi to "pi", R.id.btnE to "e", R.id.btnFrac to "()/()"
        )
        basicButtons.forEach { (id, text) ->
            findViewById<Button>(id).setOnClickListener {
                if (text == "()/()") viewModel.appendInput("()/()", "\\frac{}{}")
                else {
                    val latex = mapOf("*" to "\\cdot ", "pi" to "\\pi", "sqrt(" to "\\sqrt{",
                        "abs(" to "\\left|", "^" to "^{")[text] ?: text
                    viewModel.appendInput(text, latex)
                }
            }
        }

        findViewById<Button>(R.id.btnVarX).setOnClickListener {
            if (isSelectingVarMode) selectVariable("x") else viewModel.appendInput("x", "x")
        }

        val letters = listOf(
            R.id.btnVarA to "a", R.id.btnVarB to "b", R.id.btnVarC to "c", R.id.btnVarD to "d",
            R.id.btnVarE to "e", R.id.btnVarF to "f", R.id.btnVarG to "g", R.id.btnVarH to "h",
            R.id.btnVarI to "i", R.id.btnVarJ to "j", R.id.btnVarK to "k", R.id.btnVarL to "l",
            R.id.btnVarM to "m", R.id.btnVarN to "n", R.id.btnVarO to "o", R.id.btnVarP to "p",
            R.id.btnVarQ to "q", R.id.btnVarR to "r", R.id.btnVarS to "s", R.id.btnVarT to "t",
            R.id.btnVarU to "u", R.id.btnVarV to "v", R.id.btnVarW to "w", R.id.btnVarX2 to "x",
            R.id.btnVarY to "y", R.id.btnVarZ to "z"
        )
        letters.forEach { (id, char) ->
            findViewById<Button>(id).setOnClickListener {
                if (isSelectingVarMode) selectVariable(char) else viewModel.appendInput(char, char)
            }
        }

        val functions = mapOf(
            R.id.btnSin to Pair("sin()", "\\sin()"),
            R.id.btnCos to Pair("cos()", "\\cos()"),
            R.id.btnTan to Pair("tan()", "\\tan()"),
            R.id.btnCot to Pair("cot()", "\\cot()"),
            R.id.btnAsin to Pair("asin()", "\\arcsin()"),
            R.id.btnAcos to Pair("acos()", "\\arccos()"),
            R.id.btnAtg to Pair("atan()", "\\arctan()"),
            R.id.btnActg to Pair("acot()", "\\arccot()"),
            R.id.btnLog to Pair("log()", "\\log()"),
            R.id.btnLn to Pair("ln()", "\\ln()"),
            R.id.btnExp to Pair("exp(", "e^{"),
            R.id.btnSqrtAdv to Pair("sqrt()", "\\sqrt{}"),
            R.id.btnFracAdv to Pair("()/()", "\\frac{}{}"),
            R.id.btnAbsAdv to Pair("abs()", "|"),
            R.id.btnPowAdv to Pair("^", "^{"),
            R.id.btnPiAdv to Pair("pi", "\\pi"),
            R.id.btnEAdv to Pair("e", "e"),
            R.id.btnOpenAdv to Pair("(", "("),
            R.id.btnCloseAdv to Pair(")", ")"),
            R.id.btnDotAdv to Pair(".", ".")
        )
        functions.forEach { (id, pair) ->
            findViewById<Button>(id).setOnClickListener {
                if (pair.first == "()/()") viewModel.appendInput("()/()", "\\frac{}{}")
                else viewModel.appendInput(pair.first, pair.second)
            }
        }

        findViewById<Button>(R.id.btnBackAdv).setOnClickListener { viewModel.backspace() }
        findViewById<Button>(R.id.btnCAdv).setOnClickListener { viewModel.clear() }
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

        btnHistory.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun selectVariable(variable: String) {
        viewModel.setVariable(variable)
        btnDiffVar.text = "d$variable"
        isSelectingVarMode = false
        btnDiffVar.setBackgroundColor(Color.parseColor("#00BFA5"))
        switchTab(0)
        Toast.makeText(this, "Переменная: $variable", Toast.LENGTH_SHORT).show()
    }
}