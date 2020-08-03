/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.ponciano.lab.knowdip.aee.algorithm.sparql;


import info.ponciano.lab.knowdip.Knowdip;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.memory.Memory;
import info.ponciano.lab.pitools.utility.PiString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PFuncListAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
/**
 *
 * @author Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 */
public abstract class Algorithm extends PFuncListAndList {

    @Override
    public final void build(PropFuncArg s, Node p, PropFuncArg o, ExecutionContext executionContext) {
        if (!o.isList()) {
            throw new QueryBuildException("The object must be a list of key-value formatted arguments."
                    + System.lineSeparator() + "Expected argument list (\"arg1=val1\" \"arg2=val2\" ...).");
        }
        final List<Node> argList = o.getArgList();
        /*
	 * Initializes the parameters of the algorithm found in the argument list, given
	 * by the object of the triple.
         */
        String stringParameter = "";
        for (Node n : argList) {
            // initializing arguments from nodes
            try {
                if (n.isLiteral()) {
                    String litString = n.getLiteral().toString();
                    //set the new parameter
                    if (litString.contains("=")) {
                        stringParameter = litString;
                    } else {
                        stringParameter += litString;
                    }
                } else if (n.isURI()) {
                    stringParameter += n.getURI();
                } else {
                    throw new KnowdipException("Unsupported value in Algorithms:" + n);
                }
                String[] pair = stringParameter.split("=");
                if (pair.length == 2) {
                    PiString ps0 = new PiString(pair[0]);
                    ps0.removeEndingSpace();
                    ps0.removeStartingSpace();

                    String key = ps0.toString();
                    key = key.substring(key.lastIndexOf('#') + 1, key.length());
                    String value = pair[1];
                    Object arg = null;
                    if (Knowdip.get().getMemory().contains(value)) {
                        // argument is a complex object, saved in memory

                        arg = Knowdip.get().getMemory().access(value);
                    } else if (value.contains("http:") && !value.contains("^^")) {
                        if (!value.equals("http://lab.ponciano.info/knowdip#wall") && !value.equals("http://lab.ponciano.info/knowdip#floor")) {
                            throw new KnowdipException(value + "  is a uri that is not referenced in the memory. \n You should take the value corresponding to this uri. ");
                        } else {
                            throw new KnowdipException(value + " is depreciated");
                        }
                    } else {
                        // argument is a simple value

                        String[] split = value.split("\\^\\^");
                        if (split.length < 2) {
                            arg = value;
                        } else {
                            if (split[0].isEmpty()) {
                                arg = null;
                            } else {
                                PiString ps1 = new PiString(split[1]);
                                ps1.remove("<");
                                ps1.remove(">");
                                ps1.remove("xsd:");
                                ps1.removeEndingSpace();
                                ps1.removeStartingSpace();
                                //  final RDFDatatype typeByName = TypeMapper.getInstance().getTypeByName(piString.toString());
                                // Literal literal = ResourceFactory.createTypedLiteral(split[0], typeByName);
                                //                            arg = literal.getValue();
                                String type = ps1.toString();
                                if (type.contains("http://")) {
                                    type = type.substring(type.lastIndexOf('#') + 1, type.length());
                                }

                                switch (type) {
                                    case "double":
                                        arg = Double.parseDouble(split[0]);
                                        break;
                                    case "float":
                                        arg = Float.parseFloat(split[0]);
                                        break;
                                    case "int":
                                        arg = Integer.parseInt(split[0]);
                                        break;
                                    case "integer":
                                        arg = Integer.parseInt(split[0]);
                                        break;
                                    case "string":
                                        arg = split[0];
                                        break;
                                    case "boolean":
                                        arg = Boolean.valueOf(split[0]);
                                        break;
                                    default:
                                        arg = value;
                                        break;
                                }
                            }
                        }
                    }   //test if the key is already set
                    //translation between algorithm and ontology TODO use ontology value
                    if (key.equals("isAvailableOn")) {
                        key = "hasInput";
                    }
                    Object field = FieldUtils.readDeclaredField(Algorithm.this, key, true);
                    if (field instanceof List) {
                        List list = (List) field;
                        list.add(arg);
                    } else {
                        FieldUtils.writeDeclaredField(Algorithm.this, key, arg, true);
                    }
                } // else just wait the second parameter
            } catch (KnowdipException | IllegalAccessException ex) {
                Logger.getLogger(Algorithm.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @Override
    public QueryIterator exec(QueryIterator queryIt, PropFuncArg s, Node p, PropFuncArg o, ExecutionContext executionContext) {
        Iterable<Node> process = null;
        try {
            process = process();
        } catch (KnowdipException ex) {
            Logger.getLogger(Algorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<Binding> bindings
                = new ArrayList<>();
        if (process != null) {
            Iterator<Node> nodeIt
                    = process.iterator();

            while (nodeIt.hasNext()) {
                Node n = nodeIt.next();
                Binding b = BindingFactory.binding(Var.alloc(s.getArg()), n);

                bindings.add(b);

            }
        }
        return new QueryIterPlainWrapper(bindings
                .iterator());
    }

    @Override
    public final QueryIterator
            execEvaluated(Binding binding,
                    PropFuncArg s,
                    Node p,
                    PropFuncArg o,
                    ExecutionContext executionContext
            ) {
        return exec((QueryIterator) null, s,
                p,
                o,
                executionContext
        );

    }

    /**
     * Converts a key-value pair to an accepted algorithm format.
     *
     * @param key is the name of the parameter
     * @param value is the value of the parameter
     * @return a String containing the key and the value in valid argument form
     */
    public static String
            formatArg(String key,
                    String value
            ) {
        return "\"" + key
                + "=" + value
                + "\"";

    }

    /**
     * To be implemented with the set of procedures that generate the results of
     * the algorithm.
     *
     * @return an iterator of the nodes containing the results
     * @throws info.ponciano.lab.knowdip.KnowdipException if something wrong
     * happen.
     */
    protected abstract Iterable<Node> process() throws KnowdipException;
}
