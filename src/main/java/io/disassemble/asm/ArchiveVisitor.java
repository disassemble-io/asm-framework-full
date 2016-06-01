package io.disassemble.asm;

/**
 * @author Christopher Carpenter
 */
public abstract class ArchiveVisitor {
    protected Archive archive;
    public void visit(){
    }

    public void visitClassFactory(ClassFactory cf) {
    }

    public void visitResource(String name, byte[] data){
    }
}
