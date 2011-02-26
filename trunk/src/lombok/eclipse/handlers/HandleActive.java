package lombok.eclipse.handlers;

import java.beans.Introspector;
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
import org.activebeans.CollectionOption;
import org.activebeans.Condition;
import org.activebeans.Model;
import org.activebeans.Models;
import org.activebeans.SingularOption;
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

	private static final String ATTRIBUTES_INTERFACE = "Attributes";

	private static final String OPTIONS_INTERFACE = "Options";
	
	private static final String CONDITIONS_INTERFACE = "Conditions";
	
	private static final String MODELS_INTERFACE = "Models";

	public static String attributesInterface(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		return activeClass.getPackage().getName() + "." + activeClass.getSimpleName() + ATTRIBUTES_INTERFACE;
	}
	
	public static String modelsInterface(Class<? extends Model<?, ?, ?, ?>> activeClass) {
		return activeClass.getName() + "$" + MODELS_INTERFACE;
	}
	
	@Override
	public boolean handle(AnnotationValues<Active> at, Annotation ast,
			EclipseNode node) {
		try {
			CompilationUnitDeclaration source = (CompilationUnitDeclaration) node
					.up().up().get();
			TypeDeclaration attrInterf = attrsInterface(source,
					(TypeDeclaration) node.up().get(),
					ClassFileConstants.AccInterface, node.get());
			TypeDeclaration beanType = (TypeDeclaration) node.up().get();
			TypeDeclaration optionsInterf = optionsInterface(beanType,
					ClassFileConstants.AccPublic
							| ClassFileConstants.AccInterface, node);
			TypeDeclaration conditionsInterf = conditionsInterface(beanType,
					ClassFileConstants.AccPublic
							| ClassFileConstants.AccInterface, node);
			char[][] qualifiedOptionsName = Eclipse.fromQualifiedName(
					Eclipse.toQualifiedName(source.currentPackage.tokens) + "." +
					String.valueOf(beanType.name) + "." +String.valueOf(optionsInterf.name));
			long[] poss2 = new long[qualifiedOptionsName.length];
			Arrays.fill(poss2, node.get().sourceStart);
			QualifiedTypeReference optionsRef = new QualifiedTypeReference(qualifiedOptionsName, poss2);
			optionsRef.sourceStart = node.get().sourceStart;
			optionsRef.sourceEnd = node.get().sourceEnd;
			Eclipse.setGeneratedBy(optionsRef, node.get());
			char[][] qualifiedConditionsName = Eclipse.fromQualifiedName(
					Eclipse.toQualifiedName(source.currentPackage.tokens) + "." +
					String.valueOf(beanType.name) + "." +String.valueOf(conditionsInterf.name));
			long[] poss4 = new long[qualifiedConditionsName.length];
			Arrays.fill(poss4, node.get().sourceStart);
			QualifiedTypeReference conditionsRef = new QualifiedTypeReference(qualifiedConditionsName, poss4);
			conditionsRef.sourceStart = node.get().sourceStart;
			conditionsRef.sourceEnd = node.get().sourceEnd;
			Eclipse.setGeneratedBy(conditionsRef, node.get());
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
			char[][] qualifiedModelsName = Eclipse.fromQualifiedName(
					Eclipse.toQualifiedName(source.currentPackage.tokens) + "." +
					String.valueOf(beanType.name) + ".Models");
			long[] poss5 = new long[qualifiedModelsName.length];
			Arrays.fill(poss5, node.get().sourceStart);
			QualifiedTypeReference modelsRef = new QualifiedTypeReference(qualifiedModelsName, poss5);
			modelsRef.sourceStart = node.get().sourceStart;
			modelsRef.sourceEnd = node.get().sourceEnd;
			Eclipse.setGeneratedBy(modelsRef, node.get());
			typeArguments[modelInterf.length - 1] = new TypeReference[] { beanRef, optionsRef, conditionsRef, modelsRef };
			ParameterizedQualifiedTypeReference modelInterfRef = new ParameterizedQualifiedTypeReference(
					modelInterf, typeArguments, 0, poss3);
			modelInterfRef.sourceStart = node.get().sourceStart;
			modelInterfRef.sourceEnd = node.get().sourceEnd;
			Eclipse.setGeneratedBy(modelInterfRef, source);
			Map<String, Expression> activeMemberMap = memberMap(ast.memberValuePairs());
			Expression activeProps = activeMemberMap.get("with");
			if (!valdateAtLeastOneKey(activeProps)) {
				node.addError("You MUST configure at least one key property on your bean.");
			}
			List<PropertyDefinition> props = properties(activeProps, optionsRef, conditionsRef);
			List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
			List<MethodDeclaration> optMethods = new ArrayList<MethodDeclaration>();
			List<MethodDeclaration> condMethods = new ArrayList<MethodDeclaration>();
			for (PropertyDefinition p : props) {
				MethodDeclaration getter = p.getter(attrInterf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(getter);
				MethodDeclaration setter = p.setter(attrInterf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(setter);
				optMethods.add(p.option(optionsInterf, ExtraCompilerModifiers.AccSemicolonBody, node.get()));
				condMethods.add(p.condition(conditionsInterf, ExtraCompilerModifiers.AccSemicolonBody, node.get()));
			}
			List<BelongsToDefinition> belongTos = belongsTos(activeMemberMap.get("belongsTo"), optionsRef, conditionsRef);
			for (BelongsToDefinition b : belongTos) {
				MethodDeclaration getter = b.getter(attrInterf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(getter);
				MethodDeclaration setter = b.setter(attrInterf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(setter);
				optMethods.add(b.option(optionsInterf, ExtraCompilerModifiers.AccSemicolonBody, node.get()));
				condMethods.add(b.condition(conditionsInterf, ExtraCompilerModifiers.AccSemicolonBody, node.get()));
			}
			List<HasManyDefinition> hasManys = hasManys(activeMemberMap.get("hasMany"), optionsRef, conditionsRef);
			for (HasManyDefinition h : hasManys) {
				MethodDeclaration getter = h.getter(attrInterf,
						ExtraCompilerModifiers.AccSemicolonBody, node.get());
				methods.add(getter);
				optMethods.add(h.option(optionsInterf, ExtraCompilerModifiers.AccSemicolonBody, node.get()));
				condMethods.add(h.condition(conditionsInterf, ExtraCompilerModifiers.AccSemicolonBody, node.get()));
			}
			attrInterf.methods = methods.toArray(new AbstractMethodDeclaration[0]);
			optionsInterf.methods = optMethods.toArray(new AbstractMethodDeclaration[0]);
			conditionsInterf.methods = condMethods.toArray(new AbstractMethodDeclaration[0]);
			List<TypeDeclaration> types = new ArrayList<TypeDeclaration>(
					Arrays.asList(source.types));
			types.add(attrInterf);
			source.types = types.toArray(new TypeDeclaration[0]);
			node.up().up().add(attrInterf, Kind.TYPE).recursiveSetHandled();
			char[][] activateQName = Eclipse.fromQualifiedName(node.up()
					.getPackageDeclaration()
					+ "."
					+ String.valueOf(attrInterf.name));
			long[] poss = new long[activateQName.length];
			Arrays.fill(poss, node.get().sourceStart);
			QualifiedTypeReference activeTypeRef = new QualifiedTypeReference(
					activateQName, poss);
			Eclipse.setGeneratedBy(activeTypeRef, node.get());
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
				Eclipse.copyType(optionsRef, node.get()),
				Eclipse.copyType(conditionsRef, node.get()),
				Eclipse.copyType(modelsRef, node.get()),
				ClassFileConstants.AccPublic | ClassFileConstants.AccInterface, node);
			injectType(beanType, modelsInterf);
			injectType(beanType, optionsInterf);
			injectType(beanType, conditionsInterf);
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
			Expression activeBeanProps, TypeReference options, TypeReference conditions) {
		List<PropertyDefinition> props = new ArrayList<PropertyDefinition>();
		if (activeBeanProps instanceof Annotation) {
			Annotation activeBeanProp = (Annotation) activeBeanProps;
			Map<String, Expression> propMembers = memberMap(activeBeanProp
					.memberValuePairs());
			props.add(new PropertyDefinition(
					String.valueOf(((StringLiteral) propMembers.get("name"))
							.source()), ((ClassLiteralAccess) propMembers
							.get("type")).type, options, conditions));
		} else if (activeBeanProps instanceof ArrayInitializer) {
			ArrayInitializer propArray = (ArrayInitializer) activeBeanProps;
			for (Expression propAnnotation : propArray.expressions) {
				props.addAll(properties(propAnnotation, options, conditions));
			}
		}
		return props;
	}

	private static List<BelongsToDefinition> belongsTos(
			Expression activeBeanProps, TypeReference options, TypeReference conditions) {
		List<BelongsToDefinition> belongsTos = new ArrayList<BelongsToDefinition>();
		if (activeBeanProps instanceof Annotation) {
			Annotation activeBeanProp = (Annotation) activeBeanProps;
			Map<String, Expression> propMembers = memberMap(activeBeanProp
					.memberValuePairs());
			TypeReference type = ((ClassLiteralAccess) propMembers.get("with")).type;
			belongsTos.add(new BelongsToDefinition(type, options, conditions));
		} else if (activeBeanProps instanceof ArrayInitializer) {
			ArrayInitializer propArray = (ArrayInitializer) activeBeanProps;
			for (Expression propAnnotation : propArray.expressions) {
				belongsTos.addAll(belongsTos(propAnnotation, options, conditions));
			}
		}
		return belongsTos;
	}

	private static List<HasManyDefinition> hasManys(Expression activeBeanProps, TypeReference options, TypeReference conditions) {
		List<HasManyDefinition> hasManys = new ArrayList<HasManyDefinition>();
		if (activeBeanProps instanceof Annotation) {
			Annotation activeBeanProp = (Annotation) activeBeanProps;
			Map<String, Expression> propMembers = memberMap(activeBeanProp
					.memberValuePairs());
			hasManys.add(new HasManyDefinition(
					((ClassLiteralAccess) propMembers.get("with")).type, options, conditions));
		} else if (activeBeanProps instanceof ArrayInitializer) {
			ArrayInitializer propArray = (ArrayInitializer) activeBeanProps;
			for (Expression propAnnotation : propArray.expressions) {
				hasManys.addAll(hasManys(propAnnotation, options, conditions));
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

	private static TypeDeclaration attrsInterface(
			CompilationUnitDeclaration parent, TypeDeclaration bean,
			int modifier, ASTNode source) {
		TypeDeclaration interf = new TypeDeclaration(parent.compilationResult);
		Eclipse.setGeneratedBy(interf, source);
		interf.name = (String.valueOf(bean.name) + ATTRIBUTES_INTERFACE).toCharArray();
		interf.modifiers = modifier;
		interf.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		interf.bodyStart = interf.declarationSourceStart = interf.sourceStart = source.sourceStart;
		interf.bodyEnd = interf.declarationSourceEnd = interf.sourceEnd = source.sourceEnd;
		return interf;
	}

	private static TypeDeclaration modelsInterface(TypeDeclaration bean,
			TypeReference optionsRef, TypeReference conditionsRef, 
			TypeReference modelsRef, int modifier, EclipseNode node) {
		ASTNode source = node.get();
		TypeDeclaration interf = new TypeDeclaration(bean.compilationResult);
		Eclipse.setGeneratedBy(interf, source);
		interf.name = MODELS_INTERFACE.toCharArray();
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
		typeArguments[superInterf.length - 1] = new TypeReference[] { beanRef, optionsRef, conditionsRef, modelsRef };
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
				Eclipse.copyType(conditionsRef, source),
				ExtraCompilerModifiers.AccSemicolonBody, source));
		methods.add(covariantAdder(interf, interfRef,
				Eclipse.copyType(beanRef, source),
				ExtraCompilerModifiers.AccSemicolonBody, source));
		methods.add(covariantAttrsMethod(interf, interfRef,
				Eclipse.copyType(optionsRef, source),
				ExtraCompilerModifiers.AccSemicolonBody, source));
		methods.addAll(new FinderMethodRetriever(node.up())
				.extractInterfaceMethods(interf, source));
		interf.methods = methods.toArray(new MethodDeclaration[0]);
		interf.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		interf.bodyStart = interf.declarationSourceStart = interf.sourceStart = source.sourceStart;
		interf.bodyEnd = interf.declarationSourceEnd = interf.sourceEnd = source.sourceEnd;
		return interf;
	}
	
	private static TypeDeclaration optionsInterface(TypeDeclaration bean,
			int modifier, EclipseNode node) {
		ASTNode source = node.get();
		TypeDeclaration interf = new TypeDeclaration(bean.compilationResult);
		Eclipse.setGeneratedBy(interf, source);
		interf.name = OPTIONS_INTERFACE.toCharArray();
		interf.modifiers = modifier;
		interf.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		interf.bodyStart = interf.declarationSourceStart = interf.sourceStart = source.sourceStart;
		interf.bodyEnd = interf.declarationSourceEnd = interf.sourceEnd = source.sourceEnd;
		return interf;
	}
	
	private static TypeDeclaration conditionsInterface(TypeDeclaration bean,
			int modifier, EclipseNode node) {
		ASTNode source = node.get();
		TypeDeclaration interf = new TypeDeclaration(bean.compilationResult);
		Eclipse.setGeneratedBy(interf, source);
		interf.name = CONDITIONS_INTERFACE.toCharArray();
		interf.modifiers = modifier;
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
			TypeDeclaration parent, TypeReference type, TypeReference conditions,
			int modifier, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		Argument param = new Argument("options".toCharArray(), p, conditions,
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
			TypeDeclaration parent, TypeReference type, TypeReference optionsType,
			int modifier, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long) pS << 32 | pE;
		Argument param = new Argument("options".toCharArray(), p, optionsType,
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

	private String name;

	private TypeReference type;
	
	private TypeReference options;
	
	private TypeReference conditions;

	PropertyDefinition(String name, TypeReference type, TypeReference options, TypeReference conditions) {
		this.name = name;
		this.type = type;
		this.options = options;
		this.conditions = conditions;
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
	
	public MethodDeclaration option(TypeDeclaration parent, int modifier,
			ASTNode source) {
		char[][] optionInterf = Eclipse.fromQualifiedName(SingularOption.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[optionInterf.length][];
		long[] poss3 = new long[optionInterf.length];
		Arrays.fill(poss3, source.sourceStart);
		typeArguments[optionInterf.length - 1] = new TypeReference[] { 
			Eclipse.copyType(options, source),  Eclipse.copyType(type, source)};
		ParameterizedQualifiedTypeReference optionType = new ParameterizedQualifiedTypeReference(
				optionInterf, typeArguments, 0, poss3);
		optionType.sourceStart = source.sourceStart;
		optionType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(optionType, source);
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = optionType;
		method.annotations = null;
		method.arguments = null;
		method.selector = name.toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}
	
	public MethodDeclaration condition(TypeDeclaration parent, int modifier,
			ASTNode source) {
		char[][] conditionInterf = Eclipse.fromQualifiedName(Condition.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[conditionInterf.length][];
		long[] poss3 = new long[conditionInterf.length];
		Arrays.fill(poss3, source.sourceStart);
		typeArguments[conditionInterf.length - 1] = new TypeReference[] { 
			Eclipse.copyType(conditions, source),  Eclipse.copyType(type, source)};
		ParameterizedQualifiedTypeReference conditionType = new ParameterizedQualifiedTypeReference(
				conditionInterf, typeArguments, 0, poss3);
		conditionType.sourceStart = source.sourceStart;
		conditionType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(conditionType, source);
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = conditionType;
		method.annotations = null;
		method.arguments = null;
		method.selector = name.toCharArray();
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

	private String qualifiedTypeName;
	
	private TypeReference type;
	
	private TypeReference options;
	
	private TypeReference condtions;

	BelongsToDefinition(TypeReference type, TypeReference options, TypeReference condtions) {
		this.name = String.valueOf(type.getLastToken());
		this.qualifiedTypeName = Eclipse.toQualifiedName(type.getTypeName());
		this.type = type;
		this.options = options;
		this.condtions = condtions;
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
	
	public MethodDeclaration option(TypeDeclaration parent, int modifier,
			ASTNode source) {
		char[][] optionsTypeName = Eclipse.fromQualifiedName(qualifiedTypeName + ".Options");
		long[] poss = new long[optionsTypeName.length];
		Arrays.fill(poss, source.sourceStart);
		QualifiedTypeReference optionsType = new QualifiedTypeReference(optionsTypeName, poss);
		optionsType.sourceStart = source.sourceStart;
		optionsType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(optionsType, source);
		char[][] optionInterf = Eclipse.fromQualifiedName(SingularOption.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[optionInterf.length][];
		long[] poss3 = new long[optionInterf.length];
		Arrays.fill(poss3, source.sourceStart);
		typeArguments[optionInterf.length - 1] = new TypeReference[] { 
			Eclipse.copyType(options, source),  optionsType};
		ParameterizedQualifiedTypeReference optionType = new ParameterizedQualifiedTypeReference(
				optionInterf, typeArguments, 0, poss3);
		optionType.sourceStart = source.sourceStart;
		optionType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(optionType, source);
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = optionType;
		method.annotations = null;
		method.arguments = null;
		method.selector = Introspector.decapitalize(name).toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}
	
	public MethodDeclaration condition(TypeDeclaration parent, int modifier,
			ASTNode source) {
		char[][] conditionsTypeName = Eclipse.fromQualifiedName(qualifiedTypeName + ".Conditions");
		long[] poss = new long[conditionsTypeName.length];
		Arrays.fill(poss, source.sourceStart);
		QualifiedTypeReference conditionsType = new QualifiedTypeReference(conditionsTypeName, poss);
		conditionsType.sourceStart = source.sourceStart;
		conditionsType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(conditionsType, source);
		char[][] optionInterf = Eclipse.fromQualifiedName(SingularOption.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[optionInterf.length][];
		long[] poss3 = new long[optionInterf.length];
		Arrays.fill(poss3, source.sourceStart);
		typeArguments[optionInterf.length - 1] = new TypeReference[] { 
			Eclipse.copyType(condtions, source),  conditionsType};
		ParameterizedQualifiedTypeReference optionsType = new ParameterizedQualifiedTypeReference(
				optionInterf, typeArguments, 0, poss3);
		optionsType.sourceStart = source.sourceStart;
		optionsType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(optionsType, source);
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = optionsType;
		method.annotations = null;
		method.arguments = null;
		method.selector = Introspector.decapitalize(name).toCharArray();
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
	
	private TypeReference options;
	
	private TypeReference conditions;

	public HasManyDefinition(TypeReference type, TypeReference options, TypeReference conditions) {
		this.name = String.valueOf(type.getLastToken());
		this.qName = Eclipse.toQualifiedName(type.getTypeName());
		this.options = options;
		this.conditions = conditions;
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
	
	public MethodDeclaration option(TypeDeclaration parent, int modifier,
			ASTNode source) {
		char[][] optionsTypeName = Eclipse.fromQualifiedName(qName + ".Options");
		long[] poss = new long[optionsTypeName.length];
		Arrays.fill(poss, source.sourceStart);
		QualifiedTypeReference optionsType = new QualifiedTypeReference(optionsTypeName, poss);
		optionsType.sourceStart = source.sourceStart;
		optionsType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(optionsType, source);
		char[][] optionInterf = Eclipse.fromQualifiedName(CollectionOption.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[optionInterf.length][];
		long[] poss3 = new long[optionInterf.length];
		Arrays.fill(poss3, source.sourceStart);
		typeArguments[optionInterf.length - 1] = new TypeReference[] { 
			Eclipse.copyType(options, source),  Eclipse.copyType(optionsType, source)};
		ParameterizedQualifiedTypeReference optionType = new ParameterizedQualifiedTypeReference(
				optionInterf, typeArguments, 0, poss3);
		optionType.sourceStart = source.sourceStart;
		optionType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(optionType, source);
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = optionType;
		method.annotations = null;
		method.arguments = null;
		method.selector = Introspector.decapitalize(name + "s").toCharArray();
		method.binding = null;
		method.thrownExceptions = null;
		method.typeParameters = null;
		method.bits |= Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
		method.bodyStart = method.declarationSourceStart = method.sourceStart = source.sourceStart;
		method.bodyEnd = method.declarationSourceEnd = method.sourceEnd = source.sourceEnd;
		return method;
	}
	
	public MethodDeclaration condition(TypeDeclaration parent, int modifier,
			ASTNode source) {
		char[][] conditionsTypeName = Eclipse.fromQualifiedName(qName + ".Conditions");
		long[] poss = new long[conditionsTypeName.length];
		Arrays.fill(poss, source.sourceStart);
		QualifiedTypeReference optionsType = new QualifiedTypeReference(conditionsTypeName, poss);
		optionsType.sourceStart = source.sourceStart;
		optionsType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(optionsType, source);
		char[][] optionInterf = Eclipse.fromQualifiedName(SingularOption.class
				.getCanonicalName());
		final TypeReference[][] typeArguments = new TypeReference[optionInterf.length][];
		long[] poss3 = new long[optionInterf.length];
		Arrays.fill(poss3, source.sourceStart);
		typeArguments[optionInterf.length - 1] = new TypeReference[] { 
			Eclipse.copyType(conditions, source),  Eclipse.copyType(optionsType, source)};
		ParameterizedQualifiedTypeReference optionType = new ParameterizedQualifiedTypeReference(
				optionInterf, typeArguments, 0, poss3);
		optionType.sourceStart = source.sourceStart;
		optionType.sourceEnd = source.sourceEnd;
		Eclipse.setGeneratedBy(optionType, source);
		MethodDeclaration method = new MethodDeclaration(
				parent.compilationResult);
		Eclipse.setGeneratedBy(method, source);
		method.modifiers = modifier;
		method.returnType = optionType;
		method.annotations = null;
		method.arguments = null;
		method.selector = Introspector.decapitalize(name + "s").toCharArray();
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
