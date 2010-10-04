package lombok.eclipse.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.activebeans.Active;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class HandleActive implements EclipseAnnotationHandler<Active> {

	@Override
	public boolean handle(AnnotationValues<Active> at, Annotation ast,
			EclipseNode node) {
		try {
			CompilationUnitDeclaration source = (CompilationUnitDeclaration) node
					.up().up().get();
			TypeDeclaration activeItf = activeInterface(source,
					(TypeDeclaration) node.up().get(),
					ClassFileConstants.AccInterface, node.up().up().get());
			List<PropertyDefinition> props = properties(memberMap(
					ast.memberValuePairs()).get("value"));
			List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
			for (PropertyDefinition p : props) {
				MethodDeclaration getter = p.getter((TypeDeclaration) node.up()
						.get(), ClassFileConstants.AccPublic
						| ClassFileConstants.AccAbstract
						| ExtraCompilerModifiers.AccSemicolonBody, node.up()
						.get());
				methods.add(getter);
				MethodDeclaration setter = p.setter((TypeDeclaration) node.up()
						.get(), ClassFileConstants.AccPublic
						| ClassFileConstants.AccAbstract
						| ExtraCompilerModifiers.AccSemicolonBody, node.up()
						.get());
				methods.add(setter);
			}
			activeItf.methods = methods
					.toArray(new AbstractMethodDeclaration[0]);
			List<TypeDeclaration> types = new ArrayList<TypeDeclaration>(
					Arrays.asList(source.types));
			types.add(activeItf);
			source.types = types.toArray(new TypeDeclaration[0]);
			node.up().up().add(activeItf, Kind.TYPE).recursiveSetHandled();
			char[][] activateQName = Eclipse.fromQualifiedName(node.up()
					.getPackageDeclaration()
					+ "."
					+ String.valueOf(activeItf.name));
			long[] poss = new long[activateQName.length];
			Arrays.fill(poss, node.up().get().sourceStart);
			QualifiedTypeReference activatedTypeRef = new QualifiedTypeReference(
					activateQName, poss);
			Eclipse.setGeneratedBy(activatedTypeRef, node.up().get());

			TypeDeclaration beanType = (TypeDeclaration) node.up().get();
			beanType.modifiers |= ClassFileConstants.AccAbstract;
			if (beanType.superInterfaces == null) {
				beanType.superInterfaces = new TypeReference[] { activatedTypeRef };
			} else {
				List<TypeReference> superItfs = new ArrayList<TypeReference>(
						Arrays.asList(beanType.superInterfaces));
				superItfs.add(activatedTypeRef);
				beanType.superInterfaces = superItfs
						.toArray(new TypeReference[0]);
			}

		} catch (Exception e) {
			StringWriter msg = new StringWriter();
			e.printStackTrace(new PrintWriter(msg));
			node.addWarning(msg.toString());
		}
		return true;
	}

	private static List<PropertyDefinition> properties(
			Expression activeBeanProps) {
		List<PropertyDefinition> props = new ArrayList<PropertyDefinition>();
		if (activeBeanProps instanceof Annotation) {
			Annotation activeBeanProp = (Annotation) activeBeanProps;
			Map<String, Expression> propMembers = memberMap(activeBeanProp
					.memberValuePairs());
			props.add(new PropertyDefinition(
					String.valueOf(((StringLiteral) propMembers.get("name"))
							.source()), ((ClassLiteralAccess) propMembers
							.get("type")).type));
		} else if (activeBeanProps instanceof ArrayInitializer) {
			ArrayInitializer propArray = (ArrayInitializer) activeBeanProps;
			for (Expression propAnnotation : propArray.expressions) {
				props.addAll(properties(propAnnotation));
			}
		}
		return props;
	}

	private static Map<String, Expression> memberMap(MemberValuePair[] pairs) {
		Map<String, Expression> map = new HashMap<String, Expression>();
		for (MemberValuePair p : pairs) {
			map.put(String.valueOf(p.name), p.value);
		}
		return map;
	}

	private static TypeDeclaration activeInterface(
			CompilationUnitDeclaration parent, TypeDeclaration bean,
			int modifier, ASTNode source) {
		TypeDeclaration interf = new TypeDeclaration(parent.compilationResult);
		Eclipse.setGeneratedBy(interf, source);
		interf.name = ("Active" + String.valueOf(bean.name)).toCharArray();
		interf.modifiers = modifier;
		interf.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		interf.bodyStart = interf.declarationSourceStart = interf.sourceStart = source.sourceEnd;
		interf.bodyEnd = interf.declarationSourceEnd = interf.sourceEnd = source.sourceEnd;
		return interf;
	}

}

interface Definition {

	List<String> methods();

}

class PropertyDefinition implements Definition {

	private List<String> methods = new ArrayList<String>();

	private String name;

	private TypeReference type;

	private String getter;

	private String setter;

	PropertyDefinition(String name, TypeReference type) {
		this.name = name;
		this.type = type;
		String capitalizedName = capitalize(name);
		getter = type + " get" + capitalizedName + "();";
		setter = "void set" + capitalizedName + "(" + type + " val);";
		methods.add(getter);
		methods.add(setter);
	}

	private static String capitalize(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	public String name() {
		return name;
	}

	public TypeReference type() {
		return type;
	}

	public MethodDeclaration getter(TypeDeclaration parent, int modifier,
			ASTNode source) {
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = Eclipse.copyType(type, source);
		method.annotations = null;
		method.arguments = null;
		method.selector = ("get" + capitalize(name)).toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

	public MethodDeclaration setter(TypeDeclaration parent, int modifier,
			ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		method.returnType.sourceStart = pS;
		method.returnType.sourceEnd = pE;
		Eclipse.setGeneratedBy(method.returnType, source);
		method.annotations = null;
		Argument param = new Argument(name.toCharArray(), p, Eclipse.copyType(
				type, source), Modifier.FINAL);
		param.sourceStart = pS;
		param.sourceEnd = pE;
		Eclipse.setGeneratedBy(param, source);
		method.arguments = new Argument[] { param };
		method.selector = ("set" + capitalize(name)).toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

	@Override
	public List<String> methods() {
		return Collections.unmodifiableList(methods);
	}

}
