# solver.py
from sympy import symbols, integrate, sympify, latex, N
from sympy.parsing.sympy_parser import parse_expr, standard_transformations, implicit_multiplication_application

def parse_safe(expr_str, var_str):
    transformations = standard_transformations + (implicit_multiplication_application,)
    all_vars = {chr(i): symbols(chr(i)) for i in range(97, 123)}
    all_vars.update({'sin': 'sin', 'cos': 'cos'})
    target_var = symbols(var_str)
    all_vars[var_str] = target_var
    safe_expr = expr_str.replace("^", "**")
    return parse_expr(safe_expr, transformations=transformations, local_dict=all_vars), target_var

def calculate_indefinite(expr_str, var_str='x'):
    try:
        expr, var = parse_safe(expr_str, var_str)
        result = integrate(expr, var)
        return {"success": True, "latex": latex(result)}
    except Exception as e:
        return {"success": False, "error": str(e)}

def calculate_definite(expr_str, var_str='x', lower='0', upper='1'):
    try:
        expr, var = parse_safe(expr_str, var_str)
        l_val = sympify(lower)
        u_val = sympify(upper)
        result = integrate(expr, (var, l_val, u_val))
        return {"success": True, "latex": latex(result)}
    except Exception as e:
        return {"success": False, "error": str(e)}