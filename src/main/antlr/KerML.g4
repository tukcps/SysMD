grammar KerML;

@parser::header{
    import com.github.tukcps.sysmd.model.generated.ElementType
    import com.github.tukcps.sysmd.compiler.semantics.ActionsContext;
    import com.github.tukcps.sysmd.compiler.semantics.kerml.*
}

@parser::members {
    lateinit var semantics: ActionsContext;
}
/* Generated from KerML-textual-bnf.kebnf on 2026-07-02 at 13:26:35 */

/*  from KerML-textual-bnf.kebnf*:13:1 */
fragment LINE_TERMINATOR : '\n' | '\r' | '\r' '\n';

/*  from KerML-textual-bnf.kebnf*:21:1 */
WHITE_SPACE : (' ' | '\t' | '\f' | LINE_TERMINATOR) -> skip;

/*  from KerML-textual-bnf.kebnf*:32:1 */
SINGLE_LINE_NOTE : ('//' [^\r\n]*) -> skip;

/*  from KerML-textual-bnf.kebnf*:35:1 */
MULTILINE_NOTE : ('//*' .*? '*/') -> skip;

/*  from KerML-textual-bnf.kebnf*:38:1 */
REGULAR_COMMENT : '/*' .*? '*/';

/*  from KerML-textual-bnf.kebnf:50:1 */
NAME : BASIC_NAME | UNRESTRICTED_NAME;

/*  from KerML-textual-bnf.kebnf:53:1 */
BASIC_NAME : BASIC_INITIAL_CHARACTER BASIC_NAME_CHARACTER*;

/*  from KerML-textual-bnf.kebnf:56:1 */
SINGLE_QUOTE : '\'';

/*  from KerML-textual-bnf.kebnf:59:1 */
UNRESTRICTED_NAME : SINGLE_QUOTE (NAME_CHARACTER | ESCAPE_SEQUENCE)* SINGLE_QUOTE;

/*  from KerML-textual-bnf.kebnf:64:1 */
BASIC_INITIAL_CHARACTER : ALPHABETIC_CHARACTER | '_';

/*  from KerML-textual-bnf.kebnf:67:1 */
BASIC_NAME_CHARACTER : BASIC_INITIAL_CHARACTER | DECIMAL_DIGIT;

/*  from KerML-textual-bnf.kebnf:70:1 */
ALPHABETIC_CHARACTER : 'A' | 'B' | 'C' | 'D' | 'E' | 'F' | 'G' | 'H' | 'I' | 'J' | 'K' | 'L' | 'M' | 'N' | 'O' | 'P' | 'Q' | 'R' | 'S' | 'T' | 'U' | 'V' | 'W' | 'X' | 'Y' | 'Z' | 'a' | 'b' | 'c' | 'd' | 'e' | 'f' | 'g' | 'h' | 'i' | 'j' | 'k' | 'l' | 'm' | 'n' | 'o' | 'p' | 'q' | 'r' | 's' | 't' | 'u' | 'v' | 'w' | 'x' | 'y' | 'z';

/*  from KerML-textual-bnf.kebnf:75:1 */
DECIMAL_DIGIT : '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9';

/*  from KerML-textual-bnf.kebnf*:78:1 */
NAME_CHARACTER : [^'\\];

/*  from KerML-textual-bnf.kebnf*:81:1 */
ESCAPE_SEQUENCE : '\\' ('f' | 'n' | 't' | 'r' | 'v' | '\\' | '\'' | '"');

/*  from KerML-textual-bnf.kebnf:91:1 */
DECIMAL_VALUE : DECIMAL_DIGIT+;

/*  from KerML-textual-bnf.kebnf:94:1 */
EXPONENTIAL_VALUE : DECIMAL_VALUE ('e' | 'E') ('+' | '-')? DECIMAL_VALUE;

/*  from KerML-textual-bnf.kebnf:103:1 */
STRING_VALUE : '"' (STRING_CHARACTER | ESCAPE_SEQUENCE)* '"';

/*  from KerML-textual-bnf.kebnf*:106:1 */
STRING_CHARACTER : [^"\\];

/*  from KerML-textual-bnf.kebnf:134:1 */
TYPED_BY : ':' | 'typed' 'by';

/*  from KerML-textual-bnf.kebnf:140:1 */
REFERENCES : '::>' | 'references';

/*  from KerML-textual-bnf.kebnf:142:1 */
CROSSES : '=>' | 'crosses';

/*  from KerML-textual-bnf.kebnf:144:1 */
REDEFINES : ':>>' | 'redefines';

/*  from KerML-textual-bnf.kebnf:146:1 */
CONJUGATES : '~' | 'conjugates';

/* identification :: Element from KerML-textual-bnf.kebnf:152:1 */
identification  :
        ('<' shortName=NAME '>')? name=NAME?
        { semantics?.setIdentification($name?.text, $shortName?.text); }
    ;
/* relationshipBody :: Relationship from KerML-textual-bnf.kebnf:156:1 */
relationshipBody : ';' | '{' relationshipOwnedElement* '}';

/* relationshipOwnedElement :: Relationship from KerML-textual-bnf.kebnf:159:1 */
relationshipOwnedElement : ownedRelatedElement | ownedAnnotation;

/* ownedRelatedElement :: Element from KerML-textual-bnf.kebnf:163:1 */
ownedRelatedElement : nonFeatureElement | featureElement;

/* dependency :: Dependency from KerML-textual-bnf.kebnf:168:1 */
dependency : prefixMetadataAnnotation* 'dependency' (identification? 'from')? qualifiedName (',' qualifiedName)* 'to' qualifiedName (',' qualifiedName)* relationshipBody;

/* annotation :: Annotation from KerML-textual-bnf.kebnf:182:1 */
annotation : qualifiedName;

/* ownedAnnotation :: Annotation from KerML-textual-bnf.kebnf:185:1 */
ownedAnnotation : annotatingElement;

/* annotatingElement :: AnnotatingElement from KerML-textual-bnf.kebnf:188:1 */
annotatingElement : comment | documentation | textualRepresentation | metadataFeature;

/* ownedExpression :: OwningMembership from Ast.kt:188:1 */
ownedExpression : ();

/* comment :: Comment from KerML-textual-bnf.kebnf:199:1 */
comment : ('comment' identification ('about' annotation (',' annotation)*)?)? ('locale' STRING_VALUE)? REGULAR_COMMENT;

/* documentation :: Documentation from KerML-textual-bnf.kebnf:208:1 */
documentation : 'doc' identification ('locale' STRING_VALUE)? REGULAR_COMMENT;

/* textualRepresentation :: TextualRepresentation from KerML-textual-bnf.kebnf:225:1 */
textualRepresentation : ('rep' identification)? 'language' STRING_VALUE REGULAR_COMMENT;

/* rootNamespace :: Namespace from KerML-textual-bnf.kebnf:238:1 */
rootNamespace : namespaceBodyElement* EOF;

/* namespace :: Namespace from KerML-textual-bnf.kebnf:243:1 */
namespace : prefixMetadataMember* namespaceDeclaration namespaceBody;

/* namespaceDeclaration :: Namespace from KerML-textual-bnf.kebnf:249:1 */
namespaceDeclaration : 'namespace' identification;

/* namespaceBody :: Namespace from KerML-textual-bnf.kebnf:252:1 */
namespaceBody : ';' | '{' namespaceBodyElement* '}';

/* namespaceBodyElement :: Namespace from KerML-textual-bnf.kebnf:255:1 */
namespaceBodyElement :
    namespaceMember
    | aliasMember
    | import_
;

/* memberPrefix :: Membership from KerML-textual-bnf.kebnf:260:1 */
memberPrefix : visibilityIndicator?;

/* visibilityIndicator :: VisibilityKind from KerML-textual-bnf.kebnf:263:1 */
visibilityIndicator : 'public' | 'private' | 'protected';

/* namespaceMember :: OwningMembership from KerML-textual-bnf.kebnf:266:1 */
namespaceMember : nonFeatureMember | namespaceFeatureMember;

/* nonFeatureMember :: OwningMembership from KerML-textual-bnf.kebnf:270:1 */
nonFeatureMember : memberPrefix memberElement;

/* namespaceFeatureMember :: OwningMembership from KerML-textual-bnf.kebnf:274:1 */
namespaceFeatureMember : memberPrefix featureElement;

/* aliasMember :: Membership from KerML-textual-bnf.kebnf:278:1 */
aliasMember : memberPrefix 'alias' ('<' NAME '>')? NAME? 'for' qualifiedName relationshipBody;

/* qualifiedName :: QualifiedName from KerML-textual-bnf.kebnf:285:1 */
qualifiedName  : ('$' '::')? NAME ('::' NAME)*  ;

/* import_ :: Import from KerML-textual-bnf.kebnf*:297:1 */
import_ : importDeclaration;

/* importDeclaration :: Import from KerML-textual-bnf.kebnf:302:1 */
importDeclaration : membershipImport | namespaceImport;

/* membershipImport :: MembershipImport from KerML-textual-bnf.kebnf*:305:1 */
membershipImport : visibilityIndicator 'import' 'all'? (qualifiedName ('::' '**')?) relationshipBody;

/* namespaceImport :: NamespaceImport from KerML-textual-bnf.kebnf*:311:1 */
namespaceImport : visibilityIndicator 'import' 'all'? (qualifiedName '::' '*' ('::' '**')? | filterPackage) relationshipBody;

/* filterPackage :: Package from KerML-textual-bnf.kebnf:317:1 */
filterPackage : importDeclaration filterPackageMember+;

/* filterPackageMember :: ElementFilterMembership from KerML-textual-bnf.kebnf:321:1 */
filterPackageMember : '[' ownedExpression ']';

/* memberElement :: Element from KerML-textual-bnf.kebnf:329:1 */
memberElement : annotatingElement | nonFeatureElement;

/* nonFeatureElement :: Element from KerML-textual-bnf.kebnf:332:1 */
nonFeatureElement : dependency | namespace | type | classifier | dataType | class | structure | metaclass | association | associationStructure | interaction | behavior | function | predicate | multiplicity | package | libraryPackage | specialization | conjugation | subclassification | disjoining | featureInverting | featureTyping | subsetting | redefinition | typeFeaturing;

/* featureElement :: Feature from KerML-textual-bnf.kebnf:360:1 */
featureElement : feature | step | expression | booleanExpression | invariant | connector | bindingConnector | succession | flow | successionFlow;

/* type :: Type from KerML-textual-bnf.kebnf:388:1 */
type : typePrefix 'type' typeDeclaration typeBody;

/* typePrefix :: Type from KerML-textual-bnf.kebnf:392:1 */
typePrefix : 'abstract'? prefixMetadataMember*;

/* typeDeclaration :: Type from KerML-textual-bnf.kebnf:396:1 */
typeDeclaration : 'all'? identification ownedMultiplicity? (specializationPart | conjugationPart)+ typeRelationshipPart*;

/* specializationPart :: Type from KerML-textual-bnf.kebnf*:402:1 */
specializationPart : (':>' | 'specializes') ownedSpecialization (',' ownedSpecialization)*;

/* conjugationPart :: Type from KerML-textual-bnf.kebnf:406:1 */
conjugationPart : CONJUGATES ownedConjugation;

/* typeRelationshipPart :: Type from KerML-textual-bnf.kebnf:409:1 */
typeRelationshipPart : disjoiningPart | unioningPart | intersectingPart | differencingPart;

/* disjoiningPart :: Type from KerML-textual-bnf.kebnf:415:1 */
disjoiningPart : 'disjoint' 'from' ownedDisjoining (',' ownedDisjoining)*;

/* unioningPart :: Type from KerML-textual-bnf.kebnf:419:1 */
unioningPart : 'unions' unioning (',' unioning)*;

/* intersectingPart :: Type from KerML-textual-bnf.kebnf:423:1 */
intersectingPart : 'intersects' intersecting (',' intersecting)*;

/* differencingPart :: Type from KerML-textual-bnf.kebnf:427:1 */
differencingPart : 'differences' differencing (',' differencing)*;

/* typeBody :: Type from KerML-textual-bnf.kebnf:431:1 */
typeBody : ';' | '{' typeBodyElement* '}';

/* typeBodyElement :: Type from KerML-textual-bnf.kebnf:434:1 */
typeBodyElement : nonFeatureMember | featureMember | aliasMember | import_;

/* specialization :: Specialization from KerML-textual-bnf.kebnf*:442:1 */
specialization : ('specialization' identification)? 'subtype' specificType (':>' | 'specializes') generalType relationshipBody;

/* ownedSpecialization :: Specialization from KerML-textual-bnf.kebnf:448:1 */
ownedSpecialization : generalType;

/* specificType :: Specialization from KerML-textual-bnf.kebnf:451:1 */
specificType : qualifiedName | ownedFeatureChain;

/* generalType :: Specialization from KerML-textual-bnf.kebnf:456:1 */
generalType : qualifiedName | ownedFeatureChain;

/* conjugation :: Conjugation from KerML-textual-bnf.kebnf:463:1 */
conjugation : ('conjugation' identification)? 'conjugate' (qualifiedName | featureChain) CONJUGATES (qualifiedName | featureChain) relationshipBody;

/* ownedConjugation :: Conjugation from KerML-textual-bnf.kebnf:477:1 */
ownedConjugation : qualifiedName | featureChain;

/* disjoining :: Disjoining from KerML-textual-bnf.kebnf:484:1 */
disjoining : ('disjoining' identification)? 'disjoint' (qualifiedName | featureChain) 'from' (qualifiedName | featureChain) relationshipBody;

/* ownedDisjoining :: Disjoining from KerML-textual-bnf.kebnf:498:1 */
ownedDisjoining : qualifiedName | featureChain;

/* unioning :: Unioning from KerML-textual-bnf.kebnf:505:1 */
unioning : qualifiedName | ownedFeatureChain;

/* intersecting :: Intersecting from KerML-textual-bnf.kebnf:509:1 */
intersecting : qualifiedName | ownedFeatureChain;

/* differencing :: Differencing from KerML-textual-bnf.kebnf:513:1 */
differencing : qualifiedName | ownedFeatureChain;

/* featureMember :: OwningMembership from KerML-textual-bnf.kebnf:519:1 */
featureMember : typeFeatureMember | ownedFeatureMember;

/* typeFeatureMember :: OwningMembership from KerML-textual-bnf.kebnf:523:1 */
typeFeatureMember : memberPrefix 'member' featureElement;

/* ownedFeatureMember :: FeatureMembership from KerML-textual-bnf.kebnf:526:1 */
ownedFeatureMember : memberPrefix featureElement;

/* classifier :: Classifier from KerML-textual-bnf.kebnf:533:1 */
classifier : typePrefix 'classifier' classifierDeclaration typeBody;

/* classifierDeclaration :: Classifier from KerML-textual-bnf.kebnf:537:1 */
classifierDeclaration : 'all'? identification ownedMultiplicity? (superclassingPart | conjugationPart)? typeRelationshipPart*;

/* superclassingPart :: Classifier from KerML-textual-bnf.kebnf*:543:1 */
superclassingPart : (':>' | 'specializes') ownedSubclassification (',' ownedSubclassification)*;

/* subclassification :: Subclassification from KerML-textual-bnf.kebnf*:549:1 */
subclassification : ('specialization' identification)? 'subclassifier' qualifiedName (':>' | 'specializes') qualifiedName relationshipBody;

/* ownedSubclassification :: Subclassification from KerML-textual-bnf.kebnf:555:1 */
ownedSubclassification : qualifiedName;

/* feature :: Feature from KerML-textual-bnf.kebnf:562:1 */
feature : (featurePrefix ('feature' | prefixMetadataMember) featureDeclaration? | (endFeaturePrefix | basicFeaturePrefix) featureDeclaration) valuePart? typeBody;

/* endFeaturePrefix :: Feature from KerML-textual-bnf.kebnf:573:1 */
endFeaturePrefix : ('const')? 'end';

/* basicFeaturePrefix :: Feature from KerML-textual-bnf.kebnf:577:1 */
basicFeaturePrefix : featureDirection? 'derived'? 'abstract'? ('composite' | 'portion')? ('var' | 'const')?;

/* featurePrefix :: Feature from KerML-textual-bnf.kebnf*:584:1 */
featurePrefix : (endFeaturePrefix ownedCrossFeatureMember? | basicFeaturePrefix) prefixMetadataMember*;

/* ownedCrossFeatureMember :: OwningMembership from KerML-textual-bnf.kebnf:592:1 */
ownedCrossFeatureMember : ownedCrossFeature;

/* ownedCrossFeature :: Feature from KerML-textual-bnf.kebnf:595:1 */
ownedCrossFeature : basicFeaturePrefix featureDeclaration;

/* featureDirection :: FeatureDirectionKind from KerML-textual-bnf.kebnf:598:1 */
featureDirection : 'in' | 'out' | 'inout';

/* featureDeclaration :: Feature from KerML-textual-bnf.kebnf:601:1 */
featureDeclaration : 'all'? (featureIdentification (featureSpecializationPart | conjugationPart)? | featureSpecializationPart | conjugationPart) featureRelationshipPart*;

/* featureIdentification :: Feature from KerML-textual-bnf.kebnf:610:1 */
featureIdentification : '<' NAME '>' NAME? | NAME;

/* featureRelationshipPart :: Feature from KerML-textual-bnf.kebnf:614:1 */
featureRelationshipPart : typeRelationshipPart | chainingPart | invertingPart | typeFeaturingPart;

/* chainingPart :: Feature from KerML-textual-bnf.kebnf:620:1 */
chainingPart : 'chains' (ownedFeatureChaining | featureChain);

/* invertingPart :: Feature from KerML-textual-bnf.kebnf:625:1 */
invertingPart : 'inverse' 'of' ownedFeatureInverting;

/* typeFeaturingPart :: Feature from KerML-textual-bnf.kebnf:628:1 */
typeFeaturingPart : 'featured' 'by' ownedTypeFeaturing (',' ownedTypeFeaturing)*;

/* featureSpecializationPart :: Feature from KerML-textual-bnf.kebnf:632:1 */
featureSpecializationPart : featureSpecialization+ multiplicityPart? featureSpecialization* | multiplicityPart featureSpecialization*;

/* multiplicityPart :: Feature from KerML-textual-bnf.kebnf:636:1 */
multiplicityPart : ownedMultiplicity | ownedMultiplicity? ('ordered' ('nonunique')? | 'nonunique' 'ordered'?);

/* featureSpecialization :: Feature from KerML-textual-bnf.kebnf:642:1 */
featureSpecialization : typings | subsettings | references | crosses | redefinitions;

/* typings :: Feature from KerML-textual-bnf.kebnf:645:1 */
typings : typedBy (',' ownedFeatureTyping)*;

/* typedBy :: Feature from KerML-textual-bnf.kebnf:648:1 */
typedBy : TYPED_BY ownedFeatureTyping;

/* subsettings :: Feature from KerML-textual-bnf.kebnf:651:1 */
subsettings : subsets (',' ownedSubsetting)*;

/* subsets :: Feature from KerML-textual-bnf.kebnf*:654:1 */
subsets : (':>' | 'subsets') ownedSubsetting;

/* references :: Feature from KerML-textual-bnf.kebnf:657:1 */
references : REFERENCES ownedReferenceSubsetting;

/* crosses :: Feature from KerML-textual-bnf.kebnf:660:1 */
crosses : CROSSES ownedCrossSubsetting;

/* redefinitions :: Feature from KerML-textual-bnf.kebnf:663:1 */
redefinitions : redefines (',' ownedRedefinition)*;

/* redefines :: Feature from KerML-textual-bnf.kebnf:666:1 */
redefines : REDEFINES ownedRedefinition;

/* featureTyping :: FeatureTyping from KerML-textual-bnf.kebnf:674:1 */
featureTyping : ('specialization' identification)? 'typing' qualifiedName TYPED_BY generalType relationshipBody;

/* ownedFeatureTyping :: FeatureTyping from KerML-textual-bnf.kebnf:680:1 */
ownedFeatureTyping : generalType;

/* subsetting :: Subsetting from KerML-textual-bnf.kebnf*:685:1 */
subsetting : ('specialization' identification)? 'subset' specificType (':>' | 'subsets') generalType relationshipBody;

/* ownedSubsetting :: Subsetting from KerML-textual-bnf.kebnf:691:1 */
ownedSubsetting : generalType;

/* ownedReferenceSubsetting :: ReferenceSubsetting from KerML-textual-bnf.kebnf:694:1 */
ownedReferenceSubsetting : generalType;

/* ownedCrossSubsetting :: CrossSubsetting from KerML-textual-bnf.kebnf:697:1 */
ownedCrossSubsetting : generalType;

/* redefinition :: Redefinition from KerML-textual-bnf.kebnf:702:1 */
redefinition : ('specialization' identification)? 'redefinition' specificType REDEFINES generalType relationshipBody;

/* ownedRedefinition :: Redefinition from KerML-textual-bnf.kebnf:708:1 */
ownedRedefinition : generalType;

/* ownedFeatureChain :: Feature from KerML-textual-bnf.kebnf:713:1 */
ownedFeatureChain : featureChain;

/* featureChain :: Feature from KerML-textual-bnf.kebnf:716:1 */
featureChain : ownedFeatureChaining ('.' ownedFeatureChaining)+;

/* ownedFeatureChaining :: FeatureChaining from KerML-textual-bnf.kebnf:720:1 */
ownedFeatureChaining : qualifiedName;

/* featureInverting :: FeatureInverting from KerML-textual-bnf.kebnf:725:1 */
featureInverting : ('inverting' identification?)? 'inverse' (qualifiedName | ownedFeatureChain) 'of' (qualifiedName | ownedFeatureChain) relationshipBody;

/* ownedFeatureInverting :: FeatureInverting from KerML-textual-bnf.kebnf:739:1 */
ownedFeatureInverting : qualifiedName | ownedFeatureChain;

/* typeFeaturing :: TypeFeaturing from KerML-textual-bnf.kebnf:746:1 */
typeFeaturing : 'featuring' (identification 'of')? qualifiedName 'by' qualifiedName relationshipBody;

/* ownedTypeFeaturing :: TypeFeaturing from KerML-textual-bnf.kebnf:752:1 */
ownedTypeFeaturing : qualifiedName;

/* dataType :: DataType from KerML-textual-bnf.kebnf:759:1 */
dataType : typePrefix 'datatype' classifierDeclaration typeBody;

/* class :: Class from KerML-textual-bnf.kebnf:765:1 */
class : typePrefix 'class' classifierDeclaration typeBody;

/* structure :: Structure from KerML-textual-bnf.kebnf:771:1 */
structure : typePrefix 'struct' classifierDeclaration typeBody;

/* association :: Association from KerML-textual-bnf.kebnf:777:1 */
association : typePrefix 'assoc' classifierDeclaration typeBody;

/* associationStructure :: AssociationStructure from KerML-textual-bnf.kebnf:781:1 */
associationStructure : typePrefix 'assoc' 'struct' classifierDeclaration typeBody;

/* connector :: Connector from KerML-textual-bnf.kebnf:789:1 */
connector : featurePrefix 'connector' (featureDeclaration? valuePart? | connectorDeclaration) typeBody;

/* connectorDeclaration :: Connector from KerML-textual-bnf.kebnf:796:1 */
connectorDeclaration : binaryConnectorDeclaration | naryConnectorDeclaration;

/* binaryConnectorDeclaration :: Connector from KerML-textual-bnf.kebnf:799:1 */
binaryConnectorDeclaration : (featureDeclaration? 'from' | 'all' 'from'?)? connectorEndMember 'to' connectorEndMember;

/* naryConnectorDeclaration :: Connector from KerML-textual-bnf.kebnf:804:1 */
naryConnectorDeclaration : featureDeclaration? '(' connectorEndMember ',' connectorEndMember (',' connectorEndMember)* ')';

/* connectorEndMember :: EndFeatureMembership from KerML-textual-bnf.kebnf:811:1 */
connectorEndMember : connectorEnd;

/* connectorEnd :: Feature from KerML-textual-bnf.kebnf:814:1 */
connectorEnd : ownedCrossMultiplicityMember? (NAME REFERENCES)? ownedReferenceSubsetting;

/* ownedCrossMultiplicityMember :: OwningMembership from KerML-textual-bnf.kebnf:819:1 */
ownedCrossMultiplicityMember : ownedCrossMultiplicity;

/* ownedCrossMultiplicity :: Feature from KerML-textual-bnf.kebnf:822:1 */
ownedCrossMultiplicity : ownedMultiplicity;

/* bindingConnector :: BindingConnector from KerML-textual-bnf.kebnf:827:1 */
bindingConnector : featurePrefix 'binding' bindingConnectorDeclaration typeBody;

/* bindingConnectorDeclaration :: BindingConnector from KerML-textual-bnf.kebnf:831:1 */
bindingConnectorDeclaration : featureDeclaration ('of' connectorEndMember '=' connectorEndMember)? | 'all'? ('of'? connectorEndMember '=' connectorEndMember)?;

/* succession :: Succession from KerML-textual-bnf.kebnf:841:1 */
succession : featurePrefix 'succession' successionDeclaration typeBody;

/* successionDeclaration :: Succession from KerML-textual-bnf.kebnf:845:1 */
successionDeclaration : featureDeclaration ('first' connectorEndMember 'then' connectorEndMember)? | 'all'? ('first'? connectorEndMember 'then' connectorEndMember)?;

/* behavior :: Behavior from KerML-textual-bnf.kebnf:857:1 */
behavior : typePrefix 'behavior' classifierDeclaration typeBody;

/* step :: Step from KerML-textual-bnf.kebnf:863:1 */
step : featurePrefix 'step' featureDeclaration valuePart? typeBody;

/* function :: Function from KerML-textual-bnf.kebnf:872:1 */
function : typePrefix 'function' classifierDeclaration functionBody;

/* functionBody :: Type from KerML-textual-bnf.kebnf:876:1 */
functionBody : ';' | '{' functionBodyPart '}';

/* functionBodyPart :: Type from KerML-textual-bnf.kebnf:879:1 */
functionBodyPart : (typeBodyElement | returnFeatureMember)* resultExpressionMember?;

/* returnFeatureMember :: ReturnParameterMembership from KerML-textual-bnf.kebnf:885:1 */
returnFeatureMember : memberPrefix 'return' featureElement;

/* resultExpressionMember :: ResultExpressionMembership from KerML-textual-bnf.kebnf:889:1 */
resultExpressionMember : memberPrefix ownedExpression;

/* expression :: Expression from KerML-textual-bnf.kebnf:895:1 */
expression : featurePrefix 'expr' featureDeclaration valuePart? functionBody;

/* predicate :: Predicate from KerML-textual-bnf.kebnf:902:1 */
predicate : typePrefix 'predicate' classifierDeclaration functionBody;

/* booleanExpression :: BooleanExpression from KerML-textual-bnf.kebnf:908:1 */
booleanExpression : featurePrefix 'bool' featureDeclaration valuePart? functionBody;

/* invariant :: Invariant from KerML-textual-bnf.kebnf:913:1 */
invariant : featurePrefix 'inv' ('true' | 'false')? featureDeclaration valuePart? functionBody;

/* emptyResultMember :: ReturnParameterMembership from KerML-textual-bnf.kebnf:1055:1 */
emptyResultMember : emptyFeature;

/* emptyFeature :: Feature from KerML-textual-bnf.kebnf:1058:1 */
emptyFeature : ();

/* featureReferenceExpression :: FeatureReferenceExpression from KerML-textual-bnf.kebnf:1194:1 */
featureReferenceExpression : featureReferenceMember emptyResultMember;

/* featureReferenceMember :: Membership from KerML-textual-bnf.kebnf:1198:1 */
featureReferenceMember : featureReference;

/* featureReference :: Feature from KerML-textual-bnf.kebnf:1201:1 */
featureReference : qualifiedName;

/* literalExpression :: LiteralExpression from KerML-textual-bnf.kebnf:1264:1 */
literalExpression : literalBoolean | literalString | literalInteger | literalReal | literalInfinity;

/* literalBoolean :: LiteralBoolean from KerML-textual-bnf.kebnf:1271:1 */
literalBoolean : booleanValue;

/* booleanValue :: Boolean from KerML-textual-bnf.kebnf:1274:1 */
booleanValue : 'true' | 'false';

/* literalString :: LiteralString from KerML-textual-bnf.kebnf:1277:1 */
literalString : STRING_VALUE;

/* literalInteger :: LiteralInteger from KerML-textual-bnf.kebnf:1280:1 */
literalInteger : DECIMAL_VALUE;

/* literalReal :: LiteralRational from KerML-textual-bnf.kebnf*:1283:1 */
literalReal : realValue;

/* realValue :: Real from KerML-textual-bnf.kebnf:1286:1 */
realValue : DECIMAL_VALUE? '.' (DECIMAL_VALUE | EXPONENTIAL_VALUE) | EXPONENTIAL_VALUE;

/* literalInfinity :: LiteralInfinity from KerML-textual-bnf.kebnf:1290:1 */
literalInfinity : '*';

/* interaction :: Interaction from KerML-textual-bnf.kebnf:1297:1 */
interaction : typePrefix 'interaction' classifierDeclaration typeBody;

/* flow :: Flow from KerML-textual-bnf.kebnf:1303:1 */
flow : featurePrefix 'flow' flowDeclaration typeBody;

/* successionFlow :: SuccessionFlow from KerML-textual-bnf.kebnf:1307:1 */
successionFlow : featurePrefix 'succession' 'flow' flowDeclaration typeBody;

/* flowDeclaration :: Flow from KerML-textual-bnf.kebnf:1311:1 */
flowDeclaration : featureDeclaration valuePart? ('of' payloadFeatureMember)? ('from' flowEndMember 'to' flowEndMember)? | 'all'? flowEndMember 'to' flowEndMember;

/* payloadFeatureMember :: FeatureMembership from KerML-textual-bnf.kebnf:1320:1 */
payloadFeatureMember : payloadFeature;

/* payloadFeature :: PayloadFeature from KerML-textual-bnf.kebnf:1323:1 */
payloadFeature : identification payloadFeatureSpecializationPart valuePart? | identification valuePart | ownedFeatureTyping ownedMultiplicity? | ownedMultiplicity ownedFeatureTyping?;

/* payloadFeatureSpecializationPart :: Feature from KerML-textual-bnf.kebnf:1331:1 */
payloadFeatureSpecializationPart : featureSpecialization+ multiplicityPart? featureSpecialization* | multiplicityPart featureSpecialization+;

/* flowEndMember :: EndFeatureMembership from KerML-textual-bnf.kebnf:1336:1 */
flowEndMember : flowEnd;

/* flowEnd :: FlowEnd from KerML-textual-bnf.kebnf:1339:1 */
flowEnd : (ownedReferenceSubsetting '.')? flowFeatureMember;

/* flowFeatureMember :: FeatureMembership from KerML-textual-bnf.kebnf:1343:1 */
flowFeatureMember : flowFeature;

/* flowFeature :: Feature from KerML-textual-bnf.kebnf:1346:1 */
flowFeature : flowFeatureRedefinition;

/* flowFeatureRedefinition :: Redefinition from KerML-textual-bnf.kebnf:1351:1 */
flowFeatureRedefinition : qualifiedName;

/* valuePart :: Feature from KerML-textual-bnf.kebnf:1359:1 */
valuePart : featureValue;

/* featureValue :: FeatureValue from KerML-textual-bnf.kebnf:1362:1 */
featureValue : ('=' | ':=' | 'default' ('=' | ':=')?) ownedExpression;

/* multiplicity :: Multiplicity from KerML-textual-bnf.kebnf:1371:1 */
multiplicity : multiplicitySubset | multiplicityRange;

/* multiplicitySubset :: Multiplicity from KerML-textual-bnf.kebnf:1374:1 */
multiplicitySubset : 'multiplicity' identification subsets typeBody;

/* multiplicityRange :: MultiplicityRange from KerML-textual-bnf.kebnf:1378:1 */
multiplicityRange : 'multiplicity' identification multiplicityBounds typeBody;

/* ownedMultiplicity :: OwningMembership from KerML-textual-bnf.kebnf:1382:1 */
ownedMultiplicity : ownedMultiplicityRange;

/* ownedMultiplicityRange :: MultiplicityRange from KerML-textual-bnf.kebnf:1385:1 */
ownedMultiplicityRange : multiplicityBounds;

/* multiplicityBounds :: MultiplicityRange from KerML-textual-bnf.kebnf:1388:1 */
multiplicityBounds : '[' (multiplicityExpressionMember '..')? multiplicityExpressionMember ']';

/* multiplicityExpressionMember :: OwningMembership from KerML-textual-bnf.kebnf:1392:1 */
multiplicityExpressionMember : literalExpression | featureReferenceExpression;

/* metaclass :: Metaclass from KerML-textual-bnf.kebnf:1397:1 */
metaclass : typePrefix 'metaclass' classifierDeclaration typeBody;

/* prefixMetadataAnnotation :: Annotation from KerML-textual-bnf.kebnf:1401:1 */
prefixMetadataAnnotation : '#' prefixMetadataFeature;

/* prefixMetadataMember :: OwningMembership from KerML-textual-bnf.kebnf:1404:1 */
prefixMetadataMember : '#' prefixMetadataFeature;

/* prefixMetadataFeature :: MetadataFeature from KerML-textual-bnf.kebnf:1407:1 */
prefixMetadataFeature : ownedFeatureTyping;

/* metadataFeature :: MetadataFeature from KerML-textual-bnf.kebnf:1410:1 */
metadataFeature : prefixMetadataMember* ('@' | 'metadata') metadataFeatureDeclaration ('about' annotation (',' annotation)*)? metadataBody;

/* metadataFeatureDeclaration :: MetadataFeature from KerML-textual-bnf.kebnf:1419:1 */
metadataFeatureDeclaration : (identification (':' | 'typed' 'by'))? ownedFeatureTyping;

/* metadataBody :: Feature from KerML-textual-bnf.kebnf:1423:1 */
metadataBody : ';' | '{' metadataBodyElement* '}';

/* metadataBodyElement :: Relationship from KerML-textual-bnf.kebnf*:1426:1 */
metadataBodyElement : nonFeatureMember | metadataBodyFeatureMember | aliasMember | import_;

/* metadataBodyFeatureMember :: FeatureMembership from KerML-textual-bnf.kebnf:1432:1 */
metadataBodyFeatureMember : metadataBodyFeature;

/* metadataBodyFeature :: Feature from KerML-textual-bnf.kebnf:1435:1 */
metadataBodyFeature : 'feature'? (':>>' | 'redefines')? ownedRedefinition featureSpecializationPart? valuePart? metadataBody;

/* package :: Package from KerML-textual-bnf.kebnf:1442:1 */
package : prefixMetadataMember* packageDeclaration packageBody;

/* libraryPackage :: LibraryPackage from KerML-textual-bnf.kebnf:1446:1 */
libraryPackage : 'standard' 'library' prefixMetadataMember* packageDeclaration packageBody;

/* packageDeclaration :: Package from KerML-textual-bnf.kebnf:1451:1 */
packageDeclaration : 'package' identification;

/* packageBody :: Package from KerML-textual-bnf.kebnf:1454:1 */
packageBody : ';' | '{' (namespaceBodyElement | elementFilterMember)* '}';

/* elementFilterMember :: ElementFilterMembership from KerML-textual-bnf.kebnf:1461:1 */
elementFilterMember : memberPrefix 'filter' ownedExpression ';';