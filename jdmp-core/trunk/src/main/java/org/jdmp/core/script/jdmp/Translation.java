package org.jdmp.core.script.jdmp;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdmp.core.CoreObject;
import org.jdmp.core.algorithm.Algorithm;
import org.jdmp.core.interpreter.Result;
import org.jdmp.core.module.Module;
import org.jdmp.core.script.jdmp.analysis.DepthFirstAdapter;
import org.jdmp.core.script.jdmp.node.AArgumentListArgumentList;
import org.jdmp.core.script.jdmp.node.AArray;
import org.jdmp.core.script.jdmp.node.AArrayAssignment;
import org.jdmp.core.script.jdmp.node.AArrayMatrix;
import org.jdmp.core.script.jdmp.node.ABooleanLiteral;
import org.jdmp.core.script.jdmp.node.ACharLiteral;
import org.jdmp.core.script.jdmp.node.AColumn;
import org.jdmp.core.script.jdmp.node.AColumnMatrix;
import org.jdmp.core.script.jdmp.node.ACommaValue;
import org.jdmp.core.script.jdmp.node.AEmptyFunction;
import org.jdmp.core.script.jdmp.node.AEmptyMatrix;
import org.jdmp.core.script.jdmp.node.AExpressionArgumentList;
import org.jdmp.core.script.jdmp.node.AExpressionDivExpression;
import org.jdmp.core.script.jdmp.node.AExpressionMinusExpression;
import org.jdmp.core.script.jdmp.node.AExpressionMultExpression;
import org.jdmp.core.script.jdmp.node.AExpressionPlusExpression;
import org.jdmp.core.script.jdmp.node.AExpressionValue;
import org.jdmp.core.script.jdmp.node.AFloatingPointLiteral;
import org.jdmp.core.script.jdmp.node.AFunctionValue;
import org.jdmp.core.script.jdmp.node.AIdentifierAssignment;
import org.jdmp.core.script.jdmp.node.AIdentifierValue;
import org.jdmp.core.script.jdmp.node.AIntegerLiteral;
import org.jdmp.core.script.jdmp.node.ALiteralValue;
import org.jdmp.core.script.jdmp.node.AMatrixValue;
import org.jdmp.core.script.jdmp.node.AMinusExpression;
import org.jdmp.core.script.jdmp.node.AParameterFunction;
import org.jdmp.core.script.jdmp.node.APlusExpression;
import org.jdmp.core.script.jdmp.node.AQualifiedName;
import org.jdmp.core.script.jdmp.node.ARow;
import org.jdmp.core.script.jdmp.node.ARowMatrix;
import org.jdmp.core.script.jdmp.node.ASemicolonRow;
import org.jdmp.core.script.jdmp.node.ASemicolonValue;
import org.jdmp.core.script.jdmp.node.ASimpleName;
import org.jdmp.core.script.jdmp.node.AStatement;
import org.jdmp.core.script.jdmp.node.AStringLiteral;
import org.jdmp.core.script.jdmp.node.AValueExpression;
import org.jdmp.core.script.jdmp.node.AValueMatrix;
import org.jdmp.core.script.jdmp.node.Node;
import org.jdmp.core.script.jdmp.node.PArgumentList;
import org.jdmp.core.script.jdmp.node.PArray;
import org.jdmp.core.script.jdmp.node.PColumn;
import org.jdmp.core.script.jdmp.node.PCommaValue;
import org.jdmp.core.script.jdmp.node.PExpression;
import org.jdmp.core.script.jdmp.node.PFunction;
import org.jdmp.core.script.jdmp.node.PLiteral;
import org.jdmp.core.script.jdmp.node.PMatrix;
import org.jdmp.core.script.jdmp.node.PName;
import org.jdmp.core.script.jdmp.node.PRow;
import org.jdmp.core.script.jdmp.node.PSemicolonRow;
import org.jdmp.core.script.jdmp.node.PSemicolonValue;
import org.jdmp.core.script.jdmp.node.PValue;
import org.jdmp.core.variable.Variable;
import org.jdmp.core.variable.VariableFactory;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.enums.ValueType;
import org.ujmp.core.exceptions.MatrixException;

public class Translation extends DepthFirstAdapter {

	private Module module = null;

	private Result result = null;

	public Translation(Module module) {
		this.module = module;
	}

	private Variable getVariable(PName identifier) {
		String label = identifier.toString().trim();
		Variable v = module.getVariableList().get(label);
		if (v == null) {
			v = VariableFactory.labeledVariable(label);
			module.getVariableList().put(label, v);
		}
		return v;
	}

	private Matrix getMatrix(PArray array) {
		AArray aArray = (AArray) array;

		ARow aRow = (ARow) aArray.getRow();

		int rows = aArray.getAdditionalRows().size() + 1;
		int columns = aRow.getAdditionalValues().size() + 1;

		for (PSemicolonRow semicolonRow : aArray.getAdditionalRows()) {
			aRow = (ARow) ((ASemicolonRow) semicolonRow).getRow();
			columns = Math.max(columns, aRow.getAdditionalValues().size() + 1);
		}
		Matrix m = MatrixFactory.zeros(ValueType.OBJECT, rows, columns);

		aRow = (ARow) aArray.getRow();
		PExpression expr = aRow.getExpression();
		m.setObject(getValue(expr), 0, 0);
		int c = 1;
		for (PCommaValue commaValue : aRow.getAdditionalValues()) {
			ACommaValue aCommaValue = (ACommaValue) commaValue;
			expr = aCommaValue.getExpression();
			m.setObject(getValue(expr), 0, c++);
		}

		int r = 1;
		for (PSemicolonRow semicolonRow : aArray.getAdditionalRows()) {
			aRow = (ARow) ((ASemicolonRow) semicolonRow).getRow();
			expr = aRow.getExpression();
			m.setObject(getValue(expr), r, 0);
			c = 1;
			for (PCommaValue commaValue : aRow.getAdditionalValues()) {
				ACommaValue aCommaValue = (ACommaValue) commaValue;
				expr = aCommaValue.getExpression();
				m.setObject(getValue(expr), r, c++);
			}
			r++;
		}

		return m;
	}

	private Matrix getMatrix(PRow row) {
		ARow aRow = (ARow) row;
		int columns = aRow.getAdditionalValues().size() + 1;
		Matrix m = MatrixFactory.zeros(ValueType.OBJECT, 1, columns);
		m.setObject(getValue(aRow.getExpression()), 0, 0);
		int i = 1;
		for (PCommaValue commaValue : aRow.getAdditionalValues()) {
			PExpression expr = ((ACommaValue) commaValue).getExpression();
			m.setObject(getValue(expr), 0, i++);
		}
		return m;
	}

	private Matrix getMatrix(PColumn column) {
		AColumn aColumn = (AColumn) column;
		int rows = aColumn.getAdditionalValues().size() + 1;
		Matrix m = MatrixFactory.zeros(ValueType.OBJECT, rows, 1);
		m.setObject(getValue(aColumn.getExpression()), 0, 0);
		int i = 1;
		for (PSemicolonValue semicolonValue : aColumn.getAdditionalValues()) {
			PExpression expr = ((ASemicolonValue) semicolonValue).getExpression();
			m.setObject(getValue(expr), i++, 0);
		}
		return m;
	}

	@Override
	public void defaultOut(Node node) {
		System.out.println(node.getClass().getSimpleName() + ": " + node);
	}

	public Result getResult() {
		return result;
	}

	@Override
	public void outAArrayAssignment(AArrayAssignment node) {

	}

	@Override
	public void outAIdentifierAssignment(AIdentifierAssignment node) {
		Matrix m = getMatrix(node.getExpression());
		if (m != null) {
			Variable v = getVariable(node.getName());
			v.addMatrix(m);
			result = new Result(v.getLabel() + " = " + m.toString());
			System.out.println(v.getLabel() + " = " + m.toString());
		}
	}

	/*
	 * TODO: many expressions are not parsed correctly
	 */
	private Matrix getMatrix(PExpression expression) {

		try {

			if (expression instanceof AValueExpression) {
				return getMatrix(((AValueExpression) expression).getValue());
			} else if (expression instanceof AExpressionPlusExpression) {
				AExpressionPlusExpression op = (AExpressionPlusExpression) expression;
				PValue left = op.getLeft();
				PExpression right = op.getRight();
				Matrix leftM = getMatrix(left);
				Matrix rightM = getMatrix(right);
				return leftM.plus(rightM);
			} else if (expression instanceof AExpressionMinusExpression) {
				AExpressionMinusExpression op = (AExpressionMinusExpression) expression;
				PValue left = op.getLeft();
				PExpression right = op.getRight();
				Matrix leftM = getMatrix(left);
				Matrix rightM = getMatrix(right);
				return leftM.minus(rightM);
			} else if (expression instanceof AExpressionMultExpression) {
				AExpressionMultExpression op = (AExpressionMultExpression) expression;
				PValue left = op.getLeft();
				PExpression right = op.getRight();
				Matrix leftM = getMatrix(left);
				Matrix rightM = getMatrix(right);
				return leftM.mtimes(rightM);
			} else if (expression instanceof AExpressionDivExpression) {
				AExpressionDivExpression op = (AExpressionDivExpression) expression;
				PValue left = op.getLeft();
				PExpression right = op.getRight();
				Matrix leftM = getMatrix(left);
				Matrix rightM = getMatrix(right);
				return leftM.divide(rightM);
			} else if (expression instanceof AMinusExpression) {
				AMinusExpression op = (AMinusExpression) expression;
				PExpression right = op.getRight();
				Matrix rightM = getMatrix(right);
				return rightM.times(-1);
			} else if (expression instanceof APlusExpression) {
				APlusExpression op = (APlusExpression) expression;
				PExpression right = op.getRight();
				Matrix rightM = getMatrix(right);
				return rightM;
			}

		} catch (Exception e) {
			result = new Result(e);
			throw new MatrixException(e);
		}

		MatrixException e = new MatrixException("Unknown expression type: "
				+ expression.getClass().getSimpleName());
		result = new Result(e);
		throw e;
	}

	private Matrix getMatrix(PValue value) {
		if (value instanceof ALiteralValue) {
			return MatrixFactory.linkToValue(getValue(((ALiteralValue) value).getLiteral()));
		} else if (value instanceof AIdentifierValue) {
			String name = ((AIdentifierValue) value).getName().toString().trim();
			Variable v = module.getVariableList().get(name);
			if (v == null) {
				MatrixException e = new MatrixException("Unknown variable: " + name);
				result = new Result(e);
				throw e;
			}
			return v.getMatrix();
		} else if (value instanceof AMatrixValue) {
			return getMatrix(((AMatrixValue) value).getMatrix());
		} else if (value instanceof AExpressionValue) {
			return getMatrix(((AExpressionValue) value).getExpression());
		} else if (value instanceof AFunctionValue) {
			return getMatrix(((AFunctionValue) value).getFunction());
		}

		MatrixException e = new MatrixException("Unknown value type: "
				+ value.getClass().getSimpleName());
		result = new Result(e);
		throw e;
	}

	public Matrix executeFunction(PName name, PArgumentList arguments) {
		if (name instanceof ASimpleName) {
			ASimpleName sn = (ASimpleName) name;
			String id = sn.getIdentifier().toString().trim();
			Algorithm algorithm = getAlgorithm(id);
			if (algorithm == null) {
				MatrixException e = new MatrixException("Unknown algorithm: " + id);
				result = new Result(e);
				throw e;
			}
			return executeAlgorithm(algorithm, arguments);
		} else if (name instanceof AQualifiedName) {
			AQualifiedName qn = (AQualifiedName) name;
			String id = qn.getName().toString().trim();
			CoreObject object = getObject(id);
			if (object == null) {
				MatrixException e = new MatrixException("Unknown identifier: " + id);
				result = new Result(e);
				throw e;
			}
			return executeMethod(object, qn.getIdentifier().toString().trim(), arguments);
		}

		MatrixException e = new MatrixException("Unknown function: "
				+ name.getClass().getSimpleName());
		result = new Result(e);
		throw e;
	}

	private List<Matrix> getArgumentsAsMatrices(PArgumentList arguments) {
		List<Matrix> matrices = new ArrayList<Matrix>();
		if (arguments instanceof AExpressionArgumentList) {
			PExpression expr = ((AExpressionArgumentList) arguments).getExpression();
			matrices.add(getMatrix(expr));
			return matrices;
		} else if (arguments instanceof AArgumentListArgumentList) {
			PExpression expr = ((AArgumentListArgumentList) arguments).getExpression();		
			matrices.addAll(getArgumentsAsMatrices(((AArgumentListArgumentList) arguments)
					.getArgumentList()));
			matrices.add(getMatrix(expr));
			return matrices;
		}
		MatrixException e = new MatrixException("Unknown arguments: "
				+ arguments.getClass().getSimpleName());
		result = new Result(e);
		throw e;
	}

	private Matrix executeAlgorithm(Algorithm algorithm, PArgumentList arguments) {
		List<Matrix> matrices = getArgumentsAsMatrices(arguments);
		try {
			Map<Object, Matrix> ret = algorithm.calculate(matrices);
			return ret.values().iterator().next();
		} catch (Exception e) {
			throw new MatrixException(e);
		}

	}

	private Algorithm getAlgorithm(String id) {
		id = id.substring(0, 1).toUpperCase() + id.substring(1, id.length()).toLowerCase();
		List<String> packages = new ArrayList<String>();

		packages.add("org.jdmp.core.algorithm.basic");
		packages.add("org.jdmp.core.algorithm.classification");
		packages.add("org.jdmp.core.algorithm.regression");

		Class<?> c = null;

		for (String p : packages) {
			if (c == null) {
				try {
					c = Class.forName(p + "." + id);
					break;
				} catch (Exception e) {
				}
			}
		}

		try {
			Constructor<?> constr = c.getConstructor(Variable.VARIABLEARRAY);
			Algorithm algorithm = (Algorithm) constr
					.newInstance(new Object[] { new Variable[] {} });
			return algorithm;
		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}

	private Matrix executeMethod(CoreObject object, String name, PArgumentList argumentList) {
		try {
			Object[] arguments = getArguments(argumentList);
			for (Method method : object.getClass().getMethods()) {
				if (method.getName().equals(name)
						&& method.getParameterTypes().length == arguments.length) {
					Object o = method.invoke(object, arguments);
					return getMatrixFromMethodResult(o);
				}
			}
		} catch (Exception e) {
			result = new Result(e);
			throw new MatrixException(e);
		}

		MatrixException e = new MatrixException("Unknown method: " + name);
		result = new Result(e);
		throw e;
	}

	private Matrix getMatrixFromMethodResult(Object o) {
		return MatrixFactory.linkToValue(o);
	}

	private Object[] getArguments(PArgumentList argumentList) {
		return new Object[] {};
	}

	private CoreObject getObject(String name) {
		CoreObject o = module.getVariableList().get(name);
		if (o != null) {
			return o;
		}
		return o;
	}

	private Matrix getMatrix(PFunction function) {
		if (function instanceof AParameterFunction) {
			return executeFunction(((AParameterFunction) function).getName(),
					((AParameterFunction) function).getArgumentList());
		} else if (function instanceof AEmptyFunction) {
			return executeFunction(((AEmptyFunction) function).getName(), null);
		}
		MatrixException e = new MatrixException("Unknown function type: "
				+ function.getClass().getSimpleName());
		result = new Result(e);
		throw e;
	}

	private Matrix getMatrix(PMatrix matrix) {
		if (matrix instanceof AEmptyMatrix) {
			return MatrixFactory.emptyMatrix();
		} else if (matrix instanceof AValueMatrix) {
			return MatrixFactory.linkToValue(((AValueMatrix) matrix).getExpression());
		} else if (matrix instanceof ARowMatrix) {
			return getMatrix(((ARowMatrix) matrix).getRow());
		} else if (matrix instanceof AColumnMatrix) {
			return getMatrix(((AColumnMatrix) matrix).getColumn());
		} else if (matrix instanceof AArrayMatrix) {
			return getMatrix(((AArrayMatrix) matrix).getArray());
		}
		MatrixException e = new MatrixException("Unknown matrix type: "
				+ matrix.getClass().getSimpleName());
		result = new Result(e);
		throw e;
	}

	private Object getValue(PExpression expression) {
		Matrix m = getMatrix(expression);
		if (m.isScalar()) {
			return m.getObject(0, 0);
		} else {
			return m.getEuklideanValue();
		}
	}

	private Object getValue(PLiteral literal) {
		if (literal instanceof AIntegerLiteral) {
			return Long.parseLong(literal.toString().trim());
		} else if (literal instanceof AFloatingPointLiteral) {
			String s = literal.toString().trim();
			if ("NaN".equalsIgnoreCase(s)) {
				return Double.NaN;
			}
			if ("Inf".equalsIgnoreCase(s)) {
				return Double.POSITIVE_INFINITY;
			}
			if ("-Inf".equalsIgnoreCase(s)) {
				return Double.NEGATIVE_INFINITY;
			}
			return Double.parseDouble(s);
		} else if (literal instanceof ABooleanLiteral) {
			return Boolean.parseBoolean(literal.toString().trim());
		} else if (literal instanceof ACharLiteral) {
			String s = literal.toString().trim();
			return s.substring(1, s.length() - 1);
		} else if (literal instanceof AStringLiteral) {
			String s = literal.toString().trim();
			return s.substring(1, s.length() - 1);
		}

		MatrixException e = new MatrixException("Unknown literal type: "
				+ literal.getClass().getSimpleName());
		result = new Result(e);
		throw e;
	}

	private boolean parseReservedKeywords(AStatement node) {
		PExpression expr = node.getExpression();

		if (expr instanceof AValueExpression) {
			AValueExpression avexpr = (AValueExpression) expr;
			if (avexpr.getValue() instanceof AIdentifierValue) {

				String name = avexpr.toString().trim();

				if ("exit".equals(name) || "quit".equals(name)) {
					System.exit(0);
				} else if ("clear".equals(name)) {
					module.clear();
					return true;
				} else if ("help".equals(name) || "info".equals(name) || "doc".equals(name)) {
					result = new Result("Please visit http://www.jdmp.org/ for more information.");
					return true;
				}

			}
		}

		return false;
	}

	@Override
	public void outAStatement(AStatement node) {
		boolean executed = parseReservedKeywords(node);

		if (executed) {
			if (result == null) {
				result = new Result("");
			}
			return;
		}

		PExpression expr = node.getExpression();

		if (expr instanceof AValueExpression) {
			AValueExpression avexpr = (AValueExpression) expr;
			if (avexpr.getValue() instanceof AIdentifierValue) {
				String id = avexpr.getValue().toString().trim();
				Variable v = module.getVariableList().get(id);
				Matrix m = null;
				if (v != null) {
					m = v.getMatrix();
					result = new Result(id + " = " + m);
				} else {
					MatrixException e = new MatrixException("Unknown object or command: " + id);
					result = new Result(e);
				}
				return;
			}
		}

		String label = "ans";
		Variable v = module.getVariableList().get(label);
		if (v == null) {
			v = VariableFactory.labeledVariable(label);
			module.getVariableList().put(label, v);
		}
		Matrix m = getMatrix(node.getExpression());
		v.addMatrix(m);
		result = new Result(v.getLabel() + " = " + m);
		System.out.println(v.getLabel() + " = " + m);

	}

}
