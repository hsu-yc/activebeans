package lombok.eclipse.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseASTAdapter;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;

import org.activebeans.Active;
import org.activebeans.Conditions;
import org.activebeans.Model;
import org.activebeans.Models;
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
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
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
					ClassFileConstants.AccInterface, node.get());
			Expression activeBeanProps = memberMap(ast.memberValuePairs()).get(
					"with");
			if (!valdateAtLeastOneKey(activeBeanProps)) {
				node.addError("You MUST configure at least one key property on your bean.");
			}
			List<PropertyDefinition> props = properties(activeBeanProps);
			List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
			for (PropertyDefinition p : props) {
				MethodDeclaration getter = p.getter(activeItf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(getter);
				MethodDeclaration setter = p.setter(activeItf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(setter);
			}
			List<BelongsToDefinition> belongTos = belongsTos(memberMap(
					ast.memberValuePairs()).get("belongsTo"));
			for (BelongsToDefinition b : belongTos) {
				MethodDeclaration getter = b.getter(activeItf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(getter);
				MethodDeclaration setter = b.setter(activeItf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(setter);
			}
			List<HasManyDefinition> hasManys = hasManys(memberMap(
					ast.memberValuePairs()).get("hasMany"));
			for (HasManyDefinition h : hasManys) {
				MethodDeclaration getter = h.getter(activeItf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(getter);
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
			Arrays.fill(poss, node.get().sourceStart);
			QualifiedTypeReference activeTypeRef = new QualifiedTypeReference(
					activateQName, poss);
			Eclipse.setGeneratedBy(activeTypeRef, node.get());
			TypeDeclaration beanType = (TypeDeclaration) node.up().get();
			char[][] modelInterf = Eclipse.fromQualifiedName(Model.class
					.getCanonicalName());
			final TypeReference[][] typeArguments = new TypeReference[modelInterf.length][];
			long[] poss3 = new long[modelInterf.length];
			Arrays.fill(poss3, node.get().sourceStart);
			SingleTypeReference beanRef = new SingleTypeReference(
					beanType.name, node.get().sourceStart);
			beanRef.sourceStart = node.get().sourceStart;
			beanRef.sourceEnd = node.get().sourceEnd;
			Eclipse.setGeneratedBy(beanRef, node.get());
			typeArguments[modelInterf.length - 1] = new TypeReference[] { beanRef };
			ParameterizedQualifiedTypeReference modelInterfRef = new ParameterizedQualifiedTypeReference(
					modelInterf, typeArguments, 0, poss3);
			modelInterfRef.sourceStart = node.get().sourceStart;
			modelInterfRef.sourceEnd = node.get().sourceEnd;
			Eclipse.setGeneratedBy(modelInterfRef, source);
			beanType.modifiers |= ClassFileConstants.AccAbstract;
			if (beanType.superInterfaces == null) {
				beanType.superInterfaces = new TypeReference[] { activeTypeRef,
						modelInterfRef };
			} else {
				List<TypeReference> superItfs = new ArrayList<TypeReference>(
						Arrays.asList(beanType.superInterfaces));
				superItfs.add(activeTypeRef);
				superItfs.add(modelInterfRef);
				beanType.superInterfaces = superItfs
						.toArray(new TypeReference[0]);
			}
			TypeDeclaration modelsInterf = modelsInterface(beanType,
					ClassFileConstants.AccPublic
							| ClassFileConstants.AccInterface, node);
			injectType(beanType, modelsInterf);
			node.up().add(modelsInterf, Kind.TYPE).recursiveSetHandled();
		} catch (Exception e) {
			StringWriter msg = new StringWriter();
			e.printStackTrace(new PrintWriter(msg));
			node.addWarning(msg.toString());
		}
		return true;
	}

	private static boolean valdateAtLeastOneKey(Expression activeBeanProps) {
		boolean hasKeys = false;
		if (activeBeanProps instanceof Annotation) {
			Annotation activeBeanProp = (Annotation) activeBeanProps;
			Map<String, Expression> propMembers = memberMap(activeBeanProp
					.memberValuePairs());
			hasKeys = propMembers.get("key") instanceof TrueLiteral;
		} else if (activeBeanProps instanceof ArrayInitializer) {
			ArrayInitializer propArray = (ArrayInitializer) activeBeanProps;
			for (Expression propAnnotation : propArray.expressions) {
				if (hasKeys = valdateAtLeastOneKey(propAnnotation)) {
					break;
				}
			}
		}
		return hasKeys;
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

	private static List<BelongsToDefinition> belongsTos(
			Expression activeBeanProps) {
		List<BelongsToDefinition> belongsTos = new ArrayList<BelongsToDefinition>();
		if (activeBeanProps instanceof Annotation) {
			Annotation activeBeanProp = (Annotation) activeBeanProps;
			Map<String, Expression> propMembers = memberMap(activeBeanProp
					.memberValuePairs());
			belongsTos.add(new BelongsToDefinition(
					((ClassLiteralAccess) propMembers.get("with")).type));
		} else if (activeBeanProps instanceof ArrayInitializer) {
			ArrayInitializer propArray = (ArrayInitializer) activeBeanProps;
			for (Expression propAnnotation : propArray.expressions) {
				belongsTos.addAll(belongsTos(propAnnotation));
			}
		}
		return belongsTos;
	}

	private static List<HasManyDefinition> hasManys(Expression activeBeanProps) {
		List<HasManyDefinition> hasManys = new ArrayList<HasManyDefinition>();
		if (activeBeanProps instanceof Annotation) {
			Annotation activeBeanProp = (Annotation) activeBeanProps;
			Map<String, Expression> propMembers = memberMap(activeBeanProp
					.memberValuePairs());
			hasManys.add(new HasManyDefinition(
					((ClassLiteralAccess) propMembers.get("with")).type));
		} else if (activeBeanProps instanceof ArrayInitializer) {
			ArrayInitializer propArray = (ArrayInitializer) activeBeanProps;
			for (Expression propAnnotation : propArray.expressions) {
				hasManys.addAll(hasManys(propAnnotation));
			}
		}
		return hasManys;
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
		interf.bodyStart = interf.declarationSourceStart = interf.sourceStart = source.sourceStart;
		interf.bodyEnd = interf.declarationSourceEnd = interf.sourceEnd = source.sourceEnd;
		return interf;
	}

	private static TypeDeclaration modelsInterface(TypeDeclaration bean,
			int modifier, EclipseNode node) {
		ASTNode source = node.get();
		TypeDeclaration interf = new TypeDeclaration(bean.compilationResult);
		Eclipse.setGeneratedBy(interf, source);
		interf.name = "Models".toCharArray();
		interf.modifiers = modifier;
		char[][] superInterf = Eclipse.fromQualifiedName(Models.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[superInterf.length][];
		long[] poss = new long[superInterf.length];
		Arrays.fill(poss, source.sourceStart);
		SingleTypeReference beanRef = new SingleTypeReference(bean.name,
				source.sourceStart);
		beanRef.sourceStart = source.sourceStart;
		beanRef.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(beanRef, source);
		typeArguments[superInterf.length - 1] = new TypeReference[] { beanRef };
		ParameterizedQualifiedTypeReference superInterfRef = new ParameterizedQualifiedTypeReference(
				superInterf, typeArguments, 0, poss);
		superInterfRef.sourceStart = source.sourceStart;
		superInterfRef.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(superInterfRef, source);
		interf.superInterfaces = new TypeReference[] { superInterfRef };
		SingleTypeReference interfRef = new SingleTypeReference(interf.name,
				source.sourceStart);
		interfRef.sourceStart = source.sourceStart;
		interfRef.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(interfRef, source);
		List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
		methods.add(covariantAllFinder(interf, interfRef,
				ExtraCompilerModifiers.AccSemicolonBody, source));
		methods.add(covariantAllFinderWithOptions(interf, interfRef,
				Eclipse.copyType(beanRef, source),
				ExtraCompilerModifiers.AccSemicolonBody, source));
		methods.add(covariantAdder(interf, interfRef,
				Eclipse.copyType(beanRef, source),
				ExtraCompilerModifiers.AccSemicolonBody, source));
		methods.add(covariantAttrsMethod(interf, interfRef,
				Eclipse.copyType(beanRef, source),
				ExtraCompilerModifiers.AccSemicolonBody, source));
		methods.addAll(new FinderMethodRetriever(node.up())
				.extractInterfaceMethods(interf, source));
		interf.methods = methods.toArray(new MethodDeclaration[0]);
		interf.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		interf.bodyStart = interf.declarationSourceStart = interf.sourceStart = source.sourceStart;
		interf.bodyEnd = interf.declarationSourceEnd = interf.sourceEnd = source.sourceEnd;
		return interf;
	}

	private static MethodDeclaration covariantAllFinder(TypeDeclaration parent,
			TypeReference type, int modifier, ASTNode source) {
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = Eclipse.copyType(type, source);
		method.annotations = null;
		method.arguments = null;
		method.selector = "all".toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

	private static MethodDeclaration covariantAllFinderWithOptions(
			TypeDeclaration parent, TypeReference type, TypeReference beanType,
			int modifier, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		char[][] argType = Eclipse.fromQualifiedName(Conditions.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[argType.length][];
		long[] poss = new long[argType.length];
		Arrays.fill(poss, pS);
		typeArguments[argType.length - 1] = new TypeReference[] { beanType };
		ParameterizedQualifiedTypeReference argTypeRef = new ParameterizedQualifiedTypeReference(
				argType, typeArguments, 0, poss);
		Eclipse.setGeneratedBy(argTypeRef, source);
		Argument param = new Argument("options".toCharArray(), p, argTypeRef,
				Modifier.FINAL);
		param.sourceStart = pS;
		param.sourceEnd = pE;
		Eclipse.setGeneratedBy(param, source);
		MethodDeclaration method = covariantAllFinder(parent, type, modifier,
				source);
		method.arguments = new Argument[] { param };
		return method;
	}

	private static MethodDeclaration covariantAdder(TypeDeclaration parent,
			TypeReference rtnType, TypeReference argType, int modifier,
			ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		Argument arg = new Argument("model".toCharArray(), p, Eclipse.copyType(
				argType, source), Modifier.FINAL);
		arg.sourceStart = pS;
		arg.sourceEnd = pE;
		Eclipse.setGeneratedBy(arg, source);
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = Eclipse.copyType(rtnType, source);
		method.annotations = null;
		method.arguments = new Argument[] { arg };
		method.selector = "add".toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

	private static MethodDeclaration covariantAttrsMethod(
			TypeDeclaration parent, TypeReference type, TypeReference beanType,
			int modifier, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		char[][] argType = Eclipse.fromQualifiedName(Conditions.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[argType.length][];
		long[] poss = new long[argType.length];
		Arrays.fill(poss, pS);
		typeArguments[argType.length - 1] = new TypeReference[] { beanType };
		ParameterizedQualifiedTypeReference argTypeRef = new ParameterizedQualifiedTypeReference(
				argType, typeArguments, 0, poss);
		Eclipse.setGeneratedBy(argTypeRef, source);
		Argument param = new Argument("options".toCharArray(), p, argTypeRef,
				Modifier.FINAL);
		param.sourceStart = pS;
		param.sourceEnd = pE;
		Eclipse.setGeneratedBy(param, source);
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = Eclipse.copyType(type, source);
		method.annotations = null;
		method.arguments = new Argument[] { param };
		method.selector = "attrs".toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

	private static void injectType(TypeDeclaration parentType,
			final TypeDeclaration memberType) {
		if (parentType.memberTypes == null) {
			parentType.memberTypes = new TypeDeclaration[1];
			parentType.memberTypes[0] = memberType;
		} else {
			TypeDeclaration[] newArray = new TypeDeclaration[parentType.memberTypes.length + 1];
			System.arraycopy(parentType.memberTypes, 0, newArray, 0,
					parentType.memberTypes.length);
			newArray[parentType.memberTypes.length] = memberType;
			parentType.memberTypes = newArray;
		}
	}

	static String capitalize(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

}

class PropertyDefinition {

	private List<String> methods = new ArrayList<String>();

	private String name;

	private TypeReference type;

	private String getter;

	private String setter;

	PropertyDefinition(String name, TypeReference type) {
		this.name = name;
		this.type = type;
		String capitalizedName = HandleActive.capitalize(name);
		getter = type + " get" + capitalizedName + "();";
		setter = "void set" + capitalizedName + "(" + type + " val);";
		methods.add(getter);
		methods.add(setter);
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
		method.selector = ("get" + HandleActive.capitalize(name)).toCharArray();
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
		method.selector = ("set" + HandleActive.capitalize(name)).toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

}

class BelongsToDefinition {

	private String name;

	private TypeReference type;

	BelongsToDefinition(TypeReference type) {
		this.name = String.valueOf(type.getLastToken());
		this.type = type;
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
		method.selector = ("get" + HandleActive.capitalize(name)).toCharArray();
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
		method.selector = ("set" + HandleActive.capitalize(name)).toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

}

class HasManyDefinition {

	private String name;

	private String qName;

	public HasManyDefinition(TypeReference type) {
		this.name = String.valueOf(type.getLastToken());
		this.qName = Eclipse.toQualifiedName(type.getTypeName());
	}

	public MethodDeclaration getter(TypeDeclaration parent, int modifier,
			ASTNode source) {
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		char[][] rtnType = Eclipse.fromQualifiedName(qName + ".Models");
		long[] poss = new long[rtnType.length];
		Arrays.fill(poss, source.sourceStart);
		QualifiedTypeReference rtnTypeRef = new QualifiedTypeReference(rtnType,
				poss);
		Eclipse.setGeneratedBy(rtnTypeRef, source);
		method.returnType = rtnTypeRef;
		method.annotations = null;
		method.arguments = null;
		method.selector = ("get" + HandleActive.capitalize(name) + "s")
				.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

}

class FinderMethodRetriever extends EclipseASTAdapter {

	private List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();

	FinderMethodRetriever(EclipseNode bean) {
		bean.up().traverse(new EclipseASTAdapter() {
			@Override
			public void visitMethod(EclipseNode methodNode,
					AbstractMethodDeclaration method) {
				MethodDeclaration m;
				if (method.isStatic()
						&& (method.modifiers & ClassFileConstants.AccPublic) == 1
						&& String.valueOf(
								(m = (MethodDeclaration) method).returnType)
								.equals("Models")) {
					methods.add(m);
				}
			}
		});
	}

	public List<MethodDeclaration> extractInterfaceMethods(
			TypeDeclaration parent, ASTNode source) {
		List<MethodDeclaration> interfMethods = new ArrayList<MethodDeclaration>();
		for (MethodDeclaration m : methods) {
			interfMethods.add(extractInterface(m, parent, source));
		}
		return interfMethods;
	}

	private static MethodDeclaration extractInterface(MethodDeclaration finder,
			TypeDeclaration parent, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = ExtraCompilerModifiers.AccSemicolonBody;
		method.returnType = Eclipse.copyType(finder.returnType, source);
		Eclipse.setGeneratedBy(method.returnType, source);
		method.annotations = null;
		Argument[] args = finder.arguments;
		if (args != null) {
			List<Argument> argList = new ArrayList<Argument>();
			for (Argument arg : finder.arguments) {
				Argument a = new Argument(arg.name, p, Eclipse.copyType(
						arg.type, source), Modifier.FINAL);
				a.sourceStart = pS;
				a.sourceEnd = pE;
				Eclipse.setGeneratedBy(a, source);
				argList.add(a);
			}
			method.arguments = argList.toArray(new Argument[0]);
		}
		method.selector = finder.selector;
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}

}
