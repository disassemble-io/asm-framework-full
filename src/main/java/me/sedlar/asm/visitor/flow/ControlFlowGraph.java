package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.util.Assembly;
import me.sedlar.asm.util.EnvPath;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * A {@linkplain ControlFlowGraph} is a graph containing a node for each
 * instruction in a method, and an edge for each possible control flow; usually
 * just "next" for the instruction following the current instruction, but in the
 * case of a branch such as an "if", multiple edges to each successive location,
 * or with a "goto", a single edge to the jumped-to instruction.
 * <p>
 * It also adds edges for abnormal control flow, such as the possibility of a
 * method call throwing a runtime exception.
 *
 * @author The Android Open Source Project, Tyler Sedlar
 * @since 2/12/2016
 */
public class ControlFlowGraph {

    private static final FlowVisitor VISITOR = new FlowVisitor();

    /**
     * Map from instructions to nodes
     */
    public final Map<AbstractInsnNode, ControlFlowNode> nodes;
    public final ClassMethod method;
    private Map<Object, String> nodeIds = new HashMap<>();
    private int nodeId = 1;
    protected List<BasicBlock> blocks;
    private ExecutionPath execution;

    public ControlFlowGraph(Map<AbstractInsnNode, ControlFlowNode> nodes, ClassMethod method) {
        this.nodes = nodes;
        this.method = method;
    }

    /**
     * Creates a new {@link ControlFlowGraph} and populates it with the flow
     * control for the given method. If the optional {@code initial} parameter is
     * provided with an existing graph, then the graph is simply populated, not
     * created. This allows subclassing of the graph instance, if necessary.
     *
     * @param initial usually null, but can point to an existing instance of a
     *                {@link ControlFlowGraph} in which that graph is reused (but
     *                populated with new edges)
     * @param method  the method to be analyzed
     * @return a {@link ControlFlowGraph} with nodes for the control flow in the
     * given method
     * @throws AnalyzerException if the underlying bytecode library is unable to
     *                           analyze the method bytecode
     */
    public static synchronized ControlFlowGraph create(ControlFlowGraph initial, ClassMethod method)
        throws AnalyzerException {
        ControlFlowGraph graph = (initial != null ? initial : new ControlFlowGraph(new HashMap<>(), method));
        VISITOR.setGraphData(graph, method.instructions().size(), false);
        method.accept(VISITOR);
        return graph;
    }

    /**
     * Obtains a list of this graph's BasicBlocks.
     *
     * @return A list of this graph's BasicBlocks.
     */
    public List<BasicBlock> blocks() {
        return blocks;
    }

    /**
     * Prints out this graph's BasicBlocks.
     */
    public void printBasicBlocks() {
        List<BasicBlock> printed = new ArrayList<>();
        blocks.forEach(block -> block.print(printed));
    }

    /**
     * Gets the execution path for the graph.
     *
     * @param cached Retrieve by cache, if the execution path has been built before.
     * @return The execution path for the graph.
     */
    public ExecutionPath execution(boolean cached) {
        if (cached && execution != null) {
            return execution;
        }
        return (execution = new ExecutionPath(blocks()));
    }

    /**
     * Gets the execution path for the graph.
     *
     * @return The execution path for the graph.
     */
    public ExecutionPath execution() {
        return execution(true);
    }

    /**
     * Checks whether there is a path from the given source node to the given
     * destination node
     */
    private boolean isConnected(ControlFlowNode from, ControlFlowNode to, Set<ControlFlowNode> seen) {
        if (from == to) {
            return true;
        } else if (seen.contains(from)) {
            return false;
        }
        seen.add(from);
        List<ControlFlowNode> successors = from.successors;
        List<ControlFlowNode> exceptions = from.exceptions;
        for (ControlFlowNode successor : exceptions) {
            if (isConnected(successor, to, seen)) {
                return true;
            }
        }
        for (ControlFlowNode successor : successors) {
            if (isConnected(successor, to, seen)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether there is a path from the given source node to the given
     * destination node
     */
    public boolean isConnected(ControlFlowNode from, ControlFlowNode to) {
        return isConnected(from, to, new HashSet<>());
    }

    /**
     * Checks whether there is a path from the given instruction to the given
     * instruction node
     */
    public boolean isConnected(AbstractInsnNode from, AbstractInsnNode to) {
        return isConnected(nodeFor(from, false), nodeFor(to, false));
    }

    /**
     * Adds an instruction flow to this graph
     */
    protected void add(AbstractInsnNode from, AbstractInsnNode to, boolean backwards) {
        ControlFlowNode fromNode = nodeFor(from, false);
        ControlFlowNode toNode = nodeFor(to, backwards);
        fromNode.addSuccessor(toNode);
    }

    /**
     * Adds an exception flow to this graph
     */
    protected void exception(AbstractInsnNode from, AbstractInsnNode to, boolean backwards) {
        ControlFlowNode fromNode = nodeFor(from, false);
        ControlFlowNode toNode = nodeFor(to, backwards);
        fromNode.id = idFor(fromNode);
        toNode.id = idFor(toNode);
        fromNode.addExceptionPath(toNode);
    }

    /**
     * Looks up (and if necessary) creates a graph node for the given instruction
     *
     * @param instruction the instruction
     * @return the control flow graph node corresponding to the given
     * instruction
     */
    public ControlFlowNode nodeFor(AbstractInsnNode instruction, boolean backwards) {
        ControlFlowNode node = nodes.get(instruction);
        if (node == null) {
            node = new ControlFlowNode(this, instruction, backwards);
            node.id = idFor(node);
            nodes.put(instruction, node);
        } else {
            node.backwards = backwards;
        }
        return node;
    }

    /**
     * Creates a human readable version of the graph
     *
     * @param start the starting instruction, or null if not known or to use the
     *              first instruction
     * @return a string version of the graph
     */
    public String toString(ControlFlowNode start) {
        StringBuilder builder = new StringBuilder();
        AbstractInsnNode current;
        if (start != null) {
            current = start.instruction;
        } else {
            if (nodes.isEmpty()) {
                return "<empty>";
            } else {
                current = nodes.keySet().iterator().next();
                while (current.getPrevious() != null) {
                    current = current.getPrevious();
                }
            }
        }
        while (current != null) {
            ControlFlowNode node = nodes.get(current);
            if (node != null) {
                builder.append(node.toString(true));
            }
            current = current.getNext();
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return toString(null);
    }

    /**
     * Obtains the id for the specified object.
     *
     * @param object The object to obtain.
     * @return The id for the specified object.
     */
    public String idFor(Object object) {
        if (!nodeIds.containsKey(object)) {
            String id = Integer.toString(nodeId++);
            nodeIds.put(object, id);
            return id;
        } else {
            return nodeIds.get(object);
        }
    }

    /**
     * Generates dot output of the graph. This can be used with
     * GraphViz to visualize the graph. For example, if you
     * save the output as graph1.gv you can run
     * <pre>
     * $ dot -Tps graph1.gv -o graph1.ps
     * </pre>
     * to generate a postscript file, which you can then view
     * with "gv graph1.ps".
     * <p>
     * (There are also some online web sites where you can
     * paste in dot graphs and see the visualization right
     * there in the browser.)
     *
     * @return a dot description of this control flow graph,
     * useful for debugging
     */
    public String toDot() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph G {\n");
        BasicBlock initial = blocks().get(0);
        builder.append("  start -> ").append(initial.hashCode()).append(";\n");
        builder.append("  start [shape=plaintext];\n");
        List<BasicBlock> iterated = new ArrayList<>();
        for (BasicBlock block : blocks) {
            for (BasicBlock bBlock : block.successors()) {
                if (iterated.contains(bBlock)) {
                    continue;
                }
                iterated.add(bBlock);
                builder.append("  ").append(block.hashCode()).append(" -> ").append(bBlock.hashCode());
                block.instructions.stream().filter(insn -> insn.block.successors.size() > 1 &&
                    insn.insn instanceof JumpInsnNode)
                    .forEach(insn -> {
                        builder.append(" [label=\"");
                        BasicInstruction startInsn = bBlock.startInstruction().orElse(null);
                        if (startInsn != null && ((JumpInsnNode) insn.insn).label == startInsn.insn) {
                            builder.append("true");
                        } else {
                            builder.append("false");
                        }
                        builder.append("\"]");
                    });
                builder.append(";\n");
            }
        }
        builder.append("\n");
        blocks.forEach(bBlock -> {
            builder.append("  ").append(bBlock.hashCode()).append(" ");
            builder.append("[label=\"").append(bBlock.toDotDescribe()).append("\"");
            builder.append(",shape=box");
            builder.append("];\n");
        });
        builder.append("}");
        return builder.toString();
    }

    /**
     * Gets the display information for the given instruction.
     *
     * @param instruction The instruction to describe.
     * @return The display information for the given instruction.
     */
    public String dotDescribe(AbstractInsnNode instruction) {
        String opname = Assembly.opname(instruction.getOpcode());
        if (instruction instanceof LabelNode) {
            return "Label";
        } else if (instruction instanceof LineNumberNode) {
            LineNumberNode lineNode = (LineNumberNode) instruction;
            return "Line " + lineNode.line;
        } else if (instruction instanceof FrameNode) {
            return "Stack Frame";
        } else if (instruction instanceof MethodInsnNode) {
            MethodInsnNode method = (MethodInsnNode) instruction;
            String cls = method.owner.substring(method.owner.lastIndexOf('/') + 1);
            cls = cls.replace('$', '.');
            return (opname + " " + cls + "." + method.name + method.desc);
        } else if (instruction instanceof FieldInsnNode) {
            FieldInsnNode field = (FieldInsnNode) instruction;
            String cls = field.owner.substring(field.owner.lastIndexOf('/') + 1);
            cls = cls.replace('$', '.');
            return (opname + " " + cls + "." + field.name + field.desc);
        } else if (instruction instanceof TypeInsnNode && instruction.getOpcode() == Opcodes.NEW) {
            return ("New " + ((TypeInsnNode) instruction).desc);
        }
        StringBuilder builder = new StringBuilder();
        String opcodeName = Assembly.opname(instruction.getOpcode());
        builder.append(opcodeName);
        if (instruction instanceof IntInsnNode) {
            IntInsnNode iin = (IntInsnNode) instruction;
            builder.append(" ").append(iin.operand);
        } else if (instruction instanceof VarInsnNode) {
            VarInsnNode vin = (VarInsnNode) instruction;
            builder.append(" ").append(vin.var);
        } else if (instruction instanceof LdcInsnNode) {
            LdcInsnNode ldc = (LdcInsnNode) instruction;
            builder.append(" ");
            if (ldc.cst instanceof String) {
                builder.append("\\\"");
            }
            builder.append(ldc.cst);
            if (ldc.cst instanceof String) {
                builder.append("\\\"");
            }
        }
        return builder.toString();
    }

    /**
     * Creates an SVG image of the graph.
     * This requires GraphViz's bin directory to be on the env path.
     */
    public void renderToSVG(File output) {
        try {
            String dotSource = toDot();
            String tempDir = System.getProperty("java.io.tmpdir");
            Optional<String> graphViz = EnvPath.find(entry -> entry.contains("Graphviz"));
            boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
            if (!graphViz.isPresent() && windows) {
                throw new IllegalStateException("GraphViz is not on Environment.PATH");
            }
            String validMethodKey = method.key().replace("<", "").replace(">", "");
            File dotFile = new File(tempDir, validMethodKey + ".dot");
            Files.write(Paths.get(dotFile.toURI()), dotSource.getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
            String program = (windows ? new File(graphViz.get(), "dot.exe").getAbsolutePath() : "dot");
            ProcessBuilder builder = new ProcessBuilder(program, "-Tsvg", dotFile.getAbsolutePath(),
                "-o", output.getAbsolutePath());
            Process process = builder.start();
            process.waitFor();
//            BufferedImage image = ImageIO.read(imgFile);
//            Files.delete(Paths.get(imgFile.toURI()));
//            return image;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
