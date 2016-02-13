package me.sedlar.asm.visitor.flow;

import me.sedlar.asm.ClassMethod;
import me.sedlar.asm.util.Assembly;
import me.sedlar.asm.util.EnvPath;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;

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

    /**
     * Map from instructions to nodes
     */
    public final Map<AbstractInsnNode, ControlFlowNode> nodes;
    public final ClassMethod method;

    private Map<Object, String> nodeIds = null;
    private int nodeId = 1;

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
     * @param initial   usually null, but can point to an existing instance of a
     *                  {@link ControlFlowGraph} in which that graph is reused (but
     *                  populated with new edges)
     * @param method    the method to be analyzed
     * @return a {@link ControlFlowGraph} with nodes for the control flow in the
     * given method
     * @throws AnalyzerException if the underlying bytecode library is unable to
     *                           analyze the method bytecode
     */
    public static ControlFlowGraph create(ControlFlowGraph initial, ClassMethod method) throws AnalyzerException {
        ControlFlowGraph graph = (initial != null ? initial : new ControlFlowGraph(new HashMap<>(), method));
        InsnList instructions = method.instructions();
        Analyzer analyzer = new Analyzer(new BasicInterpreter()) {
            protected void newControlFlowEdge(int insn, int successor) {
                AbstractInsnNode from = instructions.get(insn);
                AbstractInsnNode to = instructions.get(successor);
                graph.add(from, to);
            }
            protected boolean newControlFlowExceptionEdge(int insn, TryCatchBlockNode tcb) {
                graph.exception(tcb);
                return super.newControlFlowExceptionEdge(insn, tcb);
            }
            protected boolean newControlFlowExceptionEdge(int insn, int successor) {
                AbstractInsnNode from = instructions.get(insn);
                AbstractInsnNode to = instructions.get(successor);
                graph.exception(from, to);
                return super.newControlFlowExceptionEdge(insn, successor);
            }
        };
        analyzer.analyze(method.owner.name(), method.method);
        return graph;
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
        return isConnected(nodeFor(from), nodeFor(to));
    }

    /**
     * Adds an exception flow to this graph
     */
    protected void add(AbstractInsnNode from, AbstractInsnNode to) {
        nodeFor(from).addSuccessor(nodeFor(to));
    }

    /**
     * Adds an exception flow to this graph
     */
    protected void exception(AbstractInsnNode from, AbstractInsnNode to) {
        nodeFor(from).addExceptionPath(nodeFor(to));
    }

    /**
     * Adds an exception try block node to this graph
     */
    protected void exception(TryCatchBlockNode tcb) {
        LabelNode start = tcb.start;
        LabelNode end = tcb.end;
        AbstractInsnNode curr = start;
        ControlFlowNode handlerNode = nodeFor(tcb.handler);
        while (curr != end && curr != null) {
            if (curr.getType() == AbstractInsnNode.METHOD_INSN) {
                if (tcb.type == null) {
                    nodeFor(curr).addSuccessor(handlerNode);
                }
                nodeFor(curr).addExceptionPath(handlerNode);
            }
            curr = curr.getNext();
        }
    }

    /**
     * Looks up (and if necessary) creates a graph node for the given instruction
     *
     * @param instruction the instruction
     * @return the control flow graph node corresponding to the given
     * instruction
     */
    public ControlFlowNode nodeFor(AbstractInsnNode instruction) {
        ControlFlowNode node = nodes.get(instruction);
        if (node == null) {
            node = new ControlFlowNode(this, instruction);
            nodes.put(instruction, node);
        }
        return node;
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
        return ExecutionPath.build(this);
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
        if (nodeIds == null) {
            nodeIds = new HashMap<>();
        }
        String id = nodeIds.get(object);
        if (id == null) {
            id = Integer.toString(nodeId++);
            nodeIds.put(object, id);
        }
        return id;
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
    public String toDot(AbstractInsnNode start, Set<ControlFlowNode> highlight) {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph G {\n");
        AbstractInsnNode instruction = (start != null ? start : method.instructions().getFirst());
        builder.append("  start -> ").append(idFor(nodes.get(instruction))).append(";\n");
        builder.append("  start [shape=plaintext];\n");
        while (instruction != null) {
            ControlFlowNode node = nodes.get(instruction);
            if (node != null) {
                for (ControlFlowNode to : node.successors) {
                    builder.append("  ").append(idFor(node)).append(" -> ").append(idFor(to));
                    if (node.instruction instanceof JumpInsnNode) {
                        builder.append(" [label=\"");
                        if (((JumpInsnNode) node.instruction).label == to.instruction) {
                            builder.append("true");
                        } else {
                            builder.append("false");
                        }
                        builder.append("\"]");
                    }
                    builder.append(";\n");
                }
                for (ControlFlowNode to : node.exceptions) {
                    builder.append(idFor(node)).append(" -> ").append(idFor(to));
                    builder.append(" [label=\"exception\"];\n");
                }
            }
            instruction = instruction.getNext();
        }
        builder.append("\n");
        for (ControlFlowNode node : nodes.values()) {
            instruction = node.instruction;
            builder.append("  ").append(idFor(node)).append(" ");
            builder.append("[label=\"").append(dotDescribe(node)).append("\"");
            if (highlight != null && highlight.contains(node)) {
                builder.append(",shape=box,style=filled");
            } else if (instruction instanceof LineNumberNode ||
                    instruction instanceof LabelNode ||
                    instruction instanceof FrameNode) {
                builder.append(",shape=oval,style=dotted");
            } else {
                builder.append(",shape=box");
            }
            builder.append("];\n");
        }
        builder.append("}");
        return builder.toString();
    }

    protected String dotDescribe(ControlFlowNode node) {
        AbstractInsnNode instruction = node.instruction;
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
            return "Call " + cls + "#" + method.name;
        } else if (instruction instanceof FieldInsnNode) {
            FieldInsnNode field = (FieldInsnNode) instruction;
            String cls = field.owner.substring(field.owner.lastIndexOf('/') + 1);
            cls = cls.replace('$', '.');
            return "Field " + cls + "#" + field.name;
        } else if (instruction instanceof TypeInsnNode && instruction.getOpcode() == Opcodes.NEW) {
            return "New " + ((TypeInsnNode) instruction).desc;
        }
        StringBuilder builder = new StringBuilder();
        String opcodeName = Assembly.opname(instruction.getOpcode());
        builder.append(opcodeName);
        if (instruction instanceof IntInsnNode) {
            IntInsnNode iin = (IntInsnNode) instruction;
            builder.append(" ").append(Integer.toString(iin.operand));
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
     * Creates a PNG image of the graph.
     * This requires GraphViz's bin directory to be on the env path.
     *
     * @param start The starting instruction.
     * @param highlight The nodes to highlight.
     * @return A PNG image of the graph.
     * @throws IOException
     */
    public BufferedImage dotImage(AbstractInsnNode start, Set<ControlFlowNode> highlight)
            throws IOException, InterruptedException {
        String dotSource = toDot(start, highlight);
        String tempDir = System.getProperty("java.io.tmpdir");
        Optional<String> graphViz = EnvPath.find(entry -> entry.contains("Graphviz"));
        boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");
        if (!graphViz.isPresent() && windows) {
            throw new IllegalStateException("GraphViz is not on Environment.PATH");
        }
        File dotFile = new File(tempDir, method.key() + ".dot");
        Files.write(Paths.get(dotFile.toURI()), dotSource.getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        File imgFile = new File(tempDir, method.key() + ".png");
        String program = (windows ? new File(graphViz.get(), "dot.exe").getAbsolutePath() : "dot");
        ProcessBuilder builder = new ProcessBuilder(program, "-Tpng", dotFile.getAbsolutePath(),
                "-o", imgFile.getAbsolutePath());
        Process process = builder.start();
        process.waitFor();
        BufferedImage image = ImageIO.read(imgFile);
        Files.delete(Paths.get(imgFile.toURI()));
        return image;
    }
}