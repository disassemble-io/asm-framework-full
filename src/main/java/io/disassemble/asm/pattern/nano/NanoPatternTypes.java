package io.disassemble.asm.pattern.nano;

/**
 * @author Tyler Sedlar
 * @since 2/2/16
 */
public interface NanoPatternTypes {

    String NO_PARAMETERS = "NoParameters";
    String NO_RETURN = "NoReturn";
    String CHAINED = "Chained";
    String RECURSIVE = "Recursive";
    String SAME_NAME = "SameName";
    String LEAF = "Leaf";

    String OBJECT_CREATOR = "ObjectCreator";
    String FIELD_READER = "FieldReader";
    String FIELD_WRITER = "FieldWriter";
    String TYPE_MANIPULATOR = "TypeManipulator";

    String STRAIGHT_LINE = "StraightLine";
    String LOOPING = "Looping";
    String DIRECTLY_THROWS_EXCEPTIONS = "DirectlyThrowsException";

    String LOCAL_READER = "LocalReader";
    String LOCAL_WRITER = "LocalWriter";
    String ARRAY_CREATOR = "ArrayCreator";
    String ARRAY_READER = "ArrayReader";
    String ARRAY_WRITER = "ArrayWriter";
}
